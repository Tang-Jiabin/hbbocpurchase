package com.zykj.purchase.controller;

import com.alibaba.fastjson.JSONObject;
import com.bocnet.common.security.PKCS7Tool;
import com.zykj.purchase.common.*;
import com.zykj.purchase.pojo.*;
import com.zykj.purchase.repository.RePayRepository;
import com.zykj.purchase.repository.UserDrawCodeRepository;
import com.zykj.purchase.service.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支付
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-24
 */
@Slf4j
@RestController
@RequestMapping(value = "pay")
public class PayController {

    private final BankConfig bankConfig;
    private final RedisUtil redisUtil;
    private final PayOrderService payOrderService;
    private final DrawCodeService drawCodeService;
    private final PrizeInfoService prizeInfoService;
    private final BocUserService userService;
    private final UserDrawCodeService userDrawCodeService;
    private final UserDrawCodeRepository userDrawCodeRepository;
    private final RePayRepository rePayRepository;


    @Resource
    private HttpServletRequest request;

    @Autowired
    public PayController(BankConfig bankConfig, RedisUtil redisUtil, PayOrderService payOrderService, DrawCodeService drawCodeService, PrizeInfoService prizeInfoService, BocUserService userService, UserDrawCodeService userDrawCodeService, UserDrawCodeRepository userDrawCodeRepository, RePayRepository rePayRepository) {
        this.bankConfig = bankConfig;
        this.redisUtil = redisUtil;
        this.payOrderService = payOrderService;
        this.drawCodeService = drawCodeService;
        this.prizeInfoService = prizeInfoService;
        this.userService = userService;
        this.userDrawCodeService = userDrawCodeService;
        this.userDrawCodeRepository = userDrawCodeRepository;
        this.rePayRepository = rePayRepository;
    }


    @PostMapping(value = "getPayParam")
    public synchronized String getPayParam() throws Exception {

        LocalDateTime dateTime = LocalDateTime.of(2021, 4, 11, 20, 0, 0);
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        Date now = new Date();
        if(!now.before(date)){
            return "";
        }
        int participants = Integer.parseInt(redisUtil.getString("participants"));
        if (participants >= 80000) {
            return "";
        }

        String customerId = request.getParameter("customerId");
        Integer prizeId = Integer.valueOf(request.getParameter("prizeId"));

        SnowflakeIdFactory idFactory = new SnowflakeIdFactory(10, 10);

        Map<String, String> map = new HashMap<>(12);
        Double d = 1.00d;

        String orderNo = String.valueOf("1"+idFactory.nextId());
        String paymount = BigDecimalUtil.format(d);
        String requestTime = DateUtil.getFormat(new Date(), "yyyyMMddHHmmss");
        String merchantNo = bankConfig.getMerchantNo();
        String orderUrl = bankConfig.getOrderUrl();
        String version = "1.0.1";
        String messageId = "0000212";
        String security = "P7";
        String custBackFlag = "1";

        //支付参数
        map.put("orderNo", orderNo);
        map.put("curCode", "001");
        map.put("orderAmount", paymount);
        map.put("orderTime", requestTime);
        map.put("orderNote", "众耘科技");
        map.put("subMerchantName", "众耘科技");
        map.put("orderUrl", orderUrl);
        map.put("subMerchantCode", "OF0001");
        map.put("subMerchantClass", "0001");
        map.put("subMerchantZone", "cz340000");
        String xml = map2xml(map);

        String message = Base64Utils.encodeToString(xml.getBytes(StandardCharsets.UTF_8));

        String keyStorePath = bankConfig.getKeyStorePath();
        String keystorePwd = bankConfig.getKeystorePwd();
        PKCS7Tool tool = null;
        String sign = null;

        InputStream inputStream = PayController.class.getResourceAsStream(keyStorePath);
        tool = PKCS7Tool.getSigner(inputStream, KeyStore.getDefaultType(), keystorePwd, keystorePwd);
        sign = tool.sign(xml.getBytes(StandardCharsets.UTF_8));

        Map<String, String> rmap = new HashMap<>(8);
        rmap.put("merchantNo", Base64Utils.encodeToString(merchantNo.getBytes(StandardCharsets.UTF_8)));
        rmap.put("version", Base64Utils.encodeToString(version.getBytes(StandardCharsets.UTF_8)));
        rmap.put("messageId", Base64Utils.encodeToString(messageId.getBytes(StandardCharsets.UTF_8)));
        rmap.put("security", Base64Utils.encodeToString(security.getBytes(StandardCharsets.UTF_8)));
        rmap.put("custBackFlag", custBackFlag);
        rmap.put("message", message);
        rmap.put("signature", sign);
        Object obj = JSONObject.toJSON(rmap);

        String payParam = obj.toString();


        if (StringUtils.hasLength(payParam)) {

            Optional<PrizeInfo> prizeInfoOptional = prizeInfoService.findOne(prizeId);
            if (!prizeInfoOptional.isPresent()) {
                log.error("活动不存在： {}", prizeId);
            }

            // 创建订单
            PayOrder order = payOrderService.findByCustomerIdAndPrizeId(customerId, prizeId);
            if (order == null) {
                order = new PayOrder();
            }
            order.setMerchantNo(merchantNo);
            order.setOrderNo(orderNo);
            order.setCreateDate(new Date());
            order.setOrderStatus(1);
            order.setPayAmount(paymount);
            order.setCustomerId(customerId);
            order.setPrizeId(prizeId);

            if (!StringUtils.hasLength(order.getCode())) {


                int codeLength = redisUtil.getListLength("not_used_code").intValue();
                if (codeLength <= 0) {
                    return "";
                }

                String code = redisUtil.getStringRedisTemplate().opsForList().leftPop("not_used_code");

                if(!StringUtils.hasText(code)){
                    return "";
                }

                log.info("创建订单 取出中奖码:{} {}", code,customerId);

                order.setCode(code);
            }


            order = payOrderService.save(order);


            return payParam;
        }

        return "";
    }

    @PostMapping(value = "getPayParam1")
    public synchronized String getPayParam1() throws Exception {

        SnowflakeIdFactory idFactory = new SnowflakeIdFactory(10, 10);

        Map<String, String> map = new HashMap<>(12);
        Optional<RePay> rePayOptional = rePayRepository.findById(1);
        Double d = 0d;
        if (rePayOptional.isPresent()) {
            d = Double.valueOf(rePayOptional.get().getMoney());
        }

        String orderNo = String.valueOf("4"+idFactory.nextId());
        String paymount = BigDecimalUtil.format(d);
        String requestTime = DateUtil.getFormat(new Date(), "yyyyMMddHHmmss");
        String merchantNo = bankConfig.getMerchantNo();
        String orderUrl = bankConfig.getOrderUrl();
        String version = "1.0.1";
        String messageId = "0000212";
        String security = "P7";
        String custBackFlag = "1";

        //支付参数
        map.put("orderNo", orderNo);
        map.put("curCode", "001");
        map.put("orderAmount", paymount);
        map.put("orderTime", requestTime);
        map.put("orderNote", "众耘科技");
        map.put("subMerchantName", "众耘科技");
        map.put("orderUrl", orderUrl+"/pay");
        map.put("subMerchantCode", "OF0001");
        map.put("subMerchantClass", "0001");
        map.put("subMerchantZone", "cz340000");
        String xml = map2xml(map);

        String message = Base64Utils.encodeToString(xml.getBytes(StandardCharsets.UTF_8));

        String keyStorePath = bankConfig.getKeyStorePath();
        String keystorePwd = bankConfig.getKeystorePwd();
        PKCS7Tool tool = null;
        String sign = null;

        InputStream inputStream = PayController.class.getResourceAsStream(keyStorePath);
        tool = PKCS7Tool.getSigner(inputStream, KeyStore.getDefaultType(), keystorePwd, keystorePwd);
        sign = tool.sign(xml.getBytes(StandardCharsets.UTF_8));

        Map<String, String> rmap = new HashMap<>(8);
        rmap.put("merchantNo", Base64Utils.encodeToString(merchantNo.getBytes(StandardCharsets.UTF_8)));
        rmap.put("version", Base64Utils.encodeToString(version.getBytes(StandardCharsets.UTF_8)));
        rmap.put("messageId", Base64Utils.encodeToString(messageId.getBytes(StandardCharsets.UTF_8)));
        rmap.put("security", Base64Utils.encodeToString(security.getBytes(StandardCharsets.UTF_8)));
        rmap.put("custBackFlag", custBackFlag);
        rmap.put("message", message);
        rmap.put("signature", sign);
        Object obj = JSONObject.toJSON(rmap);

        String payParam = obj.toString();

        if (StringUtils.hasLength(payParam)) {

            // 创建订单
            PayOrder order = payOrderService.findByCustomerIdAndPrizeId("1", 1);
            if (order == null) {
                order = new PayOrder();
            }
            order.setMerchantNo(merchantNo);
            order.setOrderNo(orderNo);
            order.setCreateDate(new Date());
            order.setOrderStatus(1);
            order.setPayAmount(paymount);
            order.setCustomerId("1");
            order.setPrizeId(1);
            order = payOrderService.save(order);


            return payParam;
        }

        return "";
    }



    @RequestMapping("/payRsps")
    public String payRsps(HttpServletRequest request) throws Exception {

        String merchantNo = request.getParameter("merchantNo");
        String orderNo = request.getParameter("orderNo");
        String orderSeq = request.getParameter("orderSeq");
        String cardTyp = request.getParameter("cardTyp");
        String payTime = request.getParameter("payTime");
        String orderStatus = request.getParameter("orderStatus");
        String payAmount = request.getParameter("payAmount");
        String signData = request.getParameter("signData");
        log.info("orderNo###接收订单回调,订单编号：" + orderNo);
        String plainText = merchantNo + "|" + orderNo + "|" + orderSeq + "|" + cardTyp + "|" + payTime + "|" + orderStatus + "|" + payAmount;
        //获取验签根证书，对P7验签使用二级根证书
        String certificatePath = bankConfig.getCertificatePath();
        InputStream resourceAsStream = PayController.class.getClassLoader().getResourceAsStream(certificatePath);
        PKCS7Tool tool = PKCS7Tool.getVerifier(resourceAsStream, "DER");

        tool.verify(signData, plainText.getBytes(StandardCharsets.UTF_8), null);


        //1-支付成功
        if (("1".equals(orderStatus))) {
            PayOrder payOrder = payOrderService.findByOrderNo(orderNo);

            if (payOrder == null) {
                log.error("支付结果为查询到：{}", plainText);
                return "";
            }
            //屏蔽重复通知
            if (payOrder.getOrderStatus() == 2) {
                return "";
            }

            payOrder.setOrderStatus(2);
            payOrder.setPayTime(payTime);
            payOrder.setCardTyp(cardTyp);
            payOrder.setOrderSeq(orderSeq);
            payOrderService.save(payOrder);
            addUserDrawCode(payOrder.getCustomerId(), payOrder.getPrizeId(), payOrder);
            return "";
        } else {
            log.error("订单状态错误：{}", plainText);
        }
        return "";
    }


    @GetMapping(value = "getPayStatus")
    public ServerResponse<JSONObject> getPayStatus(String customerId, Integer prizeId) {

        LocalDateTime dateTime = LocalDateTime.of(2021, 4, 11, 20, 0, 0);
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        Date now = new Date();
        if(!now.before(date)){
            return ServerResponse.createMessage(411, "活动已结束");
        }


        BocUserInfo userInfo = userService.findByCustomerId(customerId);
        if (userInfo == null) {
            return ServerResponse.createMessage(401, "请重新授权");
        }

        if (!"40740".equals(userInfo.getIbknum())) {
            return ServerResponse.createMessage(413, "您不是河北地区用户");
        }
        dateTime = LocalDateTime.of(2021, 3, 1, 0, 0, 0);
        date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        List<UserDrawCode> userDrawCodeList = userDrawCodeService.findAllByCustomerIdAndCreateDate(customerId,date);
        JSONObject jsonObject = new JSONObject();
        if (userDrawCodeList.size() > 0) {
            UserDrawCode userDrawCode = userDrawCodeList.get(userDrawCodeList.size() - 1);
            jsonObject.put("status", userDrawCode.getStatus());
            jsonObject.put("prizeId", userDrawCode.getPrizeId());
            return ServerResponse.createSuccess(jsonObject);

        } else {
            jsonObject.put("status", 0);
            jsonObject.put("prizeId", 0);
            PrizeInfo prizeInfo = prizeInfoService.findByActive();
            if (prizeInfo == null) {
                return ServerResponse.createMessage(411, "活动暂未开始");
            }
            if (prizeInfo.getParticipants() >= 80000) {
                return ServerResponse.createMessage(415, "当前活动参与人数已满");
            }
            int participants = Integer.parseInt(redisUtil.getString("participants"));
            if (participants >= 80000) {
                return ServerResponse.createMessage(415, "当前活动参与人数已满");
            }
        }

        List<PayOrder> payOrderList = payOrderService.findAllByCustomerIdAndOrderStatus(customerId, 2);

        for (PayOrder order : payOrderList) {
            LocalDate payDate = order.getCreateDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate nowDate = LocalDate.now();
            if (payDate.getMonthValue() == nowDate.getMonthValue()) {
                return ServerResponse.createMessage(414, "本月已参加过活动");
            }
        }

        return ServerResponse.createSuccess(jsonObject);
    }


    @GetMapping(value = "cancelPay")
    public void cancelPay(String customerId, Integer prizeId) {

        PayOrder payOrder = payOrderService.findByCustomerIdAndPrizeIdAndOrderStatus(customerId, prizeId, 1);

        if (StringUtils.hasLength(payOrder.getCode())) {

            log.info("取消支付 中奖码恢复：{} {}", payOrder.getCode(),customerId);

            redisUtil.getStringRedisTemplate().opsForList().leftPush("not_used_code", payOrder.getCode());
        }

        if (payOrder != null) {
            payOrderService.delete(payOrder);
        }

    }

    @GetMapping("/selectOrderStatus")
    public ServerResponse<String> selectOrderStatus(String customerId, Integer prizeId) throws Exception {


        PayOrder payOrder = payOrderService.findByCustomerIdAndPrizeId(customerId, prizeId);

        if (payOrder == null) {
            payOrder = payOrderService.findByCustomerIdAndPrizeIdAndOrderStatus(customerId, prizeId, 1);

            if (payOrder == null) {
                return ServerResponse.createMessage(411, "未查询到支付信息");
            } else {
                return ServerResponse.createMessage(412, "请继续等待");
            }
        }

        if (payOrder.getOrderStatus() == 2) {
            return ServerResponse.createSuccess(payOrder.getCode());
        }

        String action = "https://ebspay.boc.cn/PGWPortal/QueryOrderTrans.do";

        String plainText = bankConfig.getMerchantNo() + ":" + payOrder.getOrderNo();

        // 签名
        byte[] plainTextByte = plainText.getBytes("UTF-8");

        InputStream inputStream = PayController.class.getResourceAsStream(bankConfig.getKeyStorePath());
        PKCS7Tool tool = PKCS7Tool.getSigner(inputStream, KeyStore.getDefaultType(), bankConfig.getKeystorePwd(), bankConfig.getKeystorePwd());

        String signData = tool.sign(plainTextByte);

        FormBody body = new FormBody.Builder()
                .add("merchantNo", bankConfig.getMerchantNo())
                .add("orderNo", payOrder.getOrderNo())
                .add("signData", signData).build();

        String s = OkhttpUtil.getInstance().httpPost(action, body);

        String tranStatus = XmlToMapUtil.getXMLStringValue(s).get("tranStatus");

        if ("1".equals(tranStatus)) {
            payOrder.setOrderStatus(2);
            payOrderService.save(payOrder);

            addUserDrawCode(customerId, prizeId, payOrder);

            return ServerResponse.createSuccess(payOrder.getCode());
        } else {
            return ServerResponse.createMessage(412, "支付失败");
        }
    }

    private synchronized void addUserDrawCode(String customerId, Integer prizeId, PayOrder payOrder) {
        UserDrawCode userDrawCode = userDrawCodeService.findByCustomerIdAndPrizeId(customerId, prizeId);
        if (userDrawCode == null) {
            userDrawCode = new UserDrawCode();
            userDrawCode.setCode(payOrder.getCode());
            userDrawCode.setStatus(1);
            userDrawCode.setCustomerId(customerId);
            userDrawCode.setOrderId(payOrder.getOrderId());
            userDrawCode.setPrizeId(payOrder.getPrizeId());
            userDrawCode.setCreateDate(new Date());
            userDrawCode.setUpdateDate(new Date());
            userDrawCode = userDrawCodeService.save(userDrawCode);

            redisUtil.incr("participants");

            Optional<PrizeInfo> prizeInfoOptional = prizeInfoService.findOne(prizeId);
            if (prizeInfoOptional.isPresent()) {
                PrizeInfo prizeInfo = prizeInfoOptional.get();
                prizeInfo.setParticipants(prizeInfo.getParticipants() + 1);
                prizeInfoService.save(prizeInfo);
            } else {
                log.error("支付-主动查询-活动id错误");
            }

        }
    }


    public String map2xml(Map map) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");// 设置日期格式
        String requestTime = df.format(new Date());// new Date()为获取当前系统时间
        String xmlStr = "<request><head><requestTime>" + requestTime + "</requestTime></head><body>";
        Iterator entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            xmlStr += "<" + entry.getKey() + ">" + entry.getValue() + "</" + entry.getKey() + ">";
        }
        xmlStr += "</body></request>";
        return xmlStr;
    }



}
