package com.zykj.purchase.service;

import com.bocnet.common.security.PKCS7Tool;
import com.zykj.purchase.common.BankConfig;
import com.zykj.purchase.common.OkhttpUtil;
import com.zykj.purchase.common.SnowflakeIdFactory;
import com.zykj.purchase.common.XmlToMapUtil;
import com.zykj.purchase.controller.PayController;
import com.zykj.purchase.pojo.PayOrder;
import com.zykj.purchase.repository.PayOrderRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;

/**
 * 支付订单
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-25
 */
@Slf4j
@Service
public class PayOrderService {
    private final PayOrderRepository payOrderRepository;
    private final BankConfig bankConfig;

    PKCS7Tool tool = null;
    String curCode = "001";

    @Autowired
    public PayOrderService(PayOrderRepository payOrderRepository, BankConfig bankConfig) {
        this.payOrderRepository = payOrderRepository;
        this.bankConfig = bankConfig;
    }

    public PayOrder save(PayOrder order) {
        return payOrderRepository.save(order);
    }

    public PayOrder findByOrderNo(String orderNo) {
        return payOrderRepository.findByOrderNo(orderNo);
    }


    public Optional<PayOrder> findById(Integer orderId) {
        return payOrderRepository.findById(orderId);
    }


    public List<PayOrder> findAllByOrderStatus(int i) {
        return payOrderRepository.findAllByOrderStatus(i);
    }

    public void deleteAll(List<PayOrder> saveOrderList) {
        payOrderRepository.deleteAll(saveOrderList);
    }

    public PayOrder findByCustomerIdAndPrizeIdAndOrderStatus(String customerId, Integer prizeId, int status) {
        return payOrderRepository.findByCustomerIdAndPrizeIdAndOrderStatus(customerId, prizeId, status);
    }

    public List<PayOrder> findAllByCustomerIdAndOrderStatus(String customerId, int status) {
        return payOrderRepository.findByCustomerIdAndOrderStatus(customerId, status);
    }

    public void delete(PayOrder payOrder) {
        payOrderRepository.delete(payOrder);
    }

    @Async("asyncServiceExecutor")
    public void refund(List<Integer> orderIdList) throws Exception {

        log.info("开始");
        List<PayOrder> payOrderList = payOrderRepository.findAllById(orderIdList);
        log.info("size:{}",payOrderList.size());

        String merchantNo = bankConfig.getMerchantNo();

        List<PayOrder> saveOrderList = new ArrayList<>();
        SnowflakeIdFactory factory = new SnowflakeIdFactory(10, 10);

        for (PayOrder payOrder : payOrderList) {

            String refundAmount = payOrder.getPayAmount();
            String mRefundSeq = String.valueOf(factory.nextId());
            String orderNo = payOrder.getOrderNo();
            String plainText = merchantNo + "|" + mRefundSeq + "|" + curCode + "|" + refundAmount + "|" + orderNo;

            if(tool == null){
                String keyStorePath = bankConfig.getKeyStorePath();
                String keystorePwd = bankConfig.getKeystorePwd();
                InputStream inputStream = PayController.class.getResourceAsStream(keyStorePath);
                tool = PKCS7Tool.getSigner(inputStream, KeyStore.getDefaultType(), keystorePwd, keystorePwd);
            }

            String sign = tool.sign(plainText.getBytes(StandardCharsets.UTF_8));

            String action = "https://ebspay.boc.cn/PGWPortal/CommonRefundOrder.do";
            // 发送退款请求并获取反馈结果

            FormBody body = new FormBody.Builder()
                    .add("merchantNo", merchantNo)
                    .add("mRefundSeq", mRefundSeq)
                    .add("curCode", curCode)
                    .add("refundAmount", refundAmount)
                    .add("orderNo", orderNo)
                    .add("signData", sign)
                    .build();
            String xml = OkhttpUtil.getInstance().httpPost(action, body);

            Map<String, String> xmlStringValue = XmlToMapUtil.getXMLStringValue(xml);
            String hdlSts = xmlStringValue.get("hdlSts");
            String bdFlg = xmlStringValue.get("bdFlg");
            String rtnCd = xmlStringValue.get("rtnCd");
            mRefundSeq = xmlStringValue.get("mRefundSeq");

            if ("A".equals(hdlSts) || rtnCd.equals("E00001151")) {
                payOrder.setRefundNo(mRefundSeq);
                payOrder.setOrderStatus(3);
                payOrder.setRefundDate(new Date());
                saveOrderList.add(payOrder);
                log.info("退款订单号：{} 成功"+ payOrder.getOrderNo());
            } else {
                log.info("退款："+rtnCd);
                log.info("退款订单号：{} 失败"+ payOrder.getOrderNo());
            }
            break;
        }

        payOrderRepository.saveAll(saveOrderList);
        log.info("退款完成");
    }

    public PayOrder findByCustomerIdAndPrizeId(String customerId, Integer prizeId) {

        return payOrderRepository.findByCustomerIdAndPrizeId(customerId, prizeId);
    }

    public List<PayOrder> findAllByOrderStatusAndPrizeId(int status, int prizeId) {
        return payOrderRepository.findAllByOrderStatusAndPrizeId(status, prizeId);
    }

    public List<PayOrder> findAllByOrderIdIn(List<Integer> orderIdList) {
        return payOrderRepository.findAllByOrderIdIn(orderIdList);
    }
}
