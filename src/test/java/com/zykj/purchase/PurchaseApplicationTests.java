package com.zykj.purchase;

import com.bocnet.common.security.PKCS7Tool;
import com.zykj.purchase.common.*;
import com.zykj.purchase.controller.PayController;
import com.zykj.purchase.pojo.*;
import com.zykj.purchase.repository.*;
import com.zykj.purchase.service.PayOrderService;
import okhttp3.FormBody;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@SpringBootTest
class PurchaseApplicationTests {


    @Autowired
    private PayOrderRepository payOrderRepository;
    @Autowired
    private UserDrawCodeRepository userDrawCodeRepository;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private BankConfig bankConfig;
    @Autowired
    private UserWinningInfoRepository winningInfoRepository;
    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private ExecutorConfig executorConfig;
    @Autowired
    private BocUserInfoRepository userInfoRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PrizeInfoRepository prizeInfoRepository;

    PKCS7Tool tool = null;
    String curCode = "001";


//    @Test
//    void contextLoads() {
//        LocalDateTime dateTime = LocalDateTime.of(2021,2,1,0,0,0);
//        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
//        List<PayOrder> payOrderList = payOrderRepository.findAllByPrizeIdAndCreateDateAfter(2, date);
//
//        payOrderList.forEach(System.out::println);
//        System.out.println(payOrderList.size());
//
//
//        UserDrawCode userDrawCode = new UserDrawCode();
//      List<UserDrawCode> userDrawCodeList = new ArrayList<>();
//
//        for (PayOrder payOrder : payOrderList) {
//
//            UserDrawCode byCustomerIdAndPrizeId = userDrawCodeRepository.findByCustomerIdAndPrizeId(payOrder.getCustomerId(), 3);
//            if (byCustomerIdAndPrizeId == null) {
//                System.out.println("null "+ payOrder.getCustomerId() +" ??????");
//                userDrawCode = new UserDrawCode();
//                String code = redisUtil.getStringRedisTemplate().opsForList().leftPop("not_used_code");
//                userDrawCode.setCode(code);
//                userDrawCode.setOrderId(payOrder.getOrderId());
//                userDrawCode.setStatus(1);
//                userDrawCode.setCustomerId(payOrder.getCustomerId());
//                userDrawCode.setCreateDate(payOrder.getCreateDate());
//                userDrawCode.setUpdateDate(new Date());
//                userDrawCode.setPrizeId(3);
//                userDrawCodeList.add(userDrawCode);
//            }else{
//                System.out.println(" not null "+ payOrder.getCustomerId() +" ");
//            }
//
//        }
//
//        userDrawCodeRepository.saveAll(userDrawCodeList);
//    }

//    @Test
//    void re() throws Exception {
//
//        PayOrder payOrder = payOrderRepository.findByCustomerIdAndPrizeId("247306417", 2);
//        String merchantNo = bankConfig.getMerchantNo();
//        String curCode = "001";
//        String refundAmount = payOrder.getPayAmount();
//        SnowflakeIdFactory factory = new SnowflakeIdFactory(2,2);
//        String mRefundSeq = String.valueOf(factory.nextId());
//        String orderNo = payOrder.getOrderNo();
//        String plainText = merchantNo + "|" + mRefundSeq + "|" + curCode + "|" + refundAmount + "|" + orderNo;
//
//        String keyStorePath = bankConfig.getKeyStorePath();
//        String keystorePwd = bankConfig.getKeystorePwd();
//        PKCS7Tool tool = null;
//        String sign = null;
//
//        InputStream inputStream = PayController.class.getResourceAsStream(keyStorePath);
//        tool = PKCS7Tool.getSigner(inputStream, KeyStore.getDefaultType(), keystorePwd, keystorePwd);
//        sign = tool.sign(plainText.getBytes(StandardCharsets.UTF_8));
//
//
//        String action = "https://ebspay.boc.cn/PGWPortal/CommonRefundOrder.do";
//        // ???????????????????????????????????????
//
//        FormBody body = new FormBody.Builder()
//                .add("merchantNo", merchantNo)
//                .add("mRefundSeq", mRefundSeq)
//                .add("curCode", curCode)
//                .add("refundAmount", refundAmount)
//                .add("orderNo", orderNo)
//                .add("signData", sign)
//                .build();
//        String xml = OkhttpUtil.getInstance().httpPost(action, body);
//
//        Map<String, String> xmlStringValue = XmlToMapUtil.getXMLStringValue(xml);
//        String hdlSts = xmlStringValue.get("hdlSts");
//        String bdFlg = xmlStringValue.get("bdFlg");
//        String rtnCd = xmlStringValue.get("rtnCd");
//        mRefundSeq = xmlStringValue.get("mRefundSeq");
//        System.out.println("?????????"+rtnCd);
//        if ("A".equals(hdlSts) || rtnCd.equals("E00001151")) {
//            payOrder.setRefundNo(mRefundSeq);
//            payOrder.setOrderStatus(3);
//            payOrder.setRefundDate(new Date());
//            payOrderRepository.save(payOrder);
//            System.out.println("??????????????????{} ??????"+ payOrder.getOrderNo());
//        } else {
//            System.out.println("??????????????????{} ??????"+ payOrder.getOrderNo());
//        }
//    }

//    @Test
//    void ??????() {
//        UserDrawCode userDrawCode = new UserDrawCode();
//        String code = redisUtil.getStringRedisTemplate().opsForList().leftPop("not_used_code");
//        userDrawCode.setCode(code);
//        userDrawCode.setOrderId(41370);
//        userDrawCode.setStatus(1);
//        userDrawCode.setCustomerId("151139233");
//        userDrawCode.setCreateDate(new Date());
//        userDrawCode.setUpdateDate(new Date());
//        userDrawCode.setPrizeId(3);
//        userDrawCodeRepository.save(userDrawCode);
//    }

//    @Test
//    void ??????????????????() {
//        Integer prizeId = 6;
//        Integer status = 3;
//        List<UserDrawCode> userDrawCodeList = userDrawCodeRepository.findAllByPrizeIdAndStatus(prizeId, status);
//        List<String> customerIdList = userDrawCodeList.stream().map(UserDrawCode::getCustomerId).collect(Collectors.toList());
//        List<Integer> orderIdList = userDrawCodeList.stream().map(UserDrawCode::getOrderId).collect(Collectors.toList());
//        List<UserWinningInfo> winningInfoList = winningInfoRepository.findAllByCustomerIdIn(customerIdList);
//
//        List<PayOrder> orderList = payOrderRepository.findAllByOrderIdIn(orderIdList);
//
//        Map<String, String> map = new HashMap<>();
//        List<Map<String, String>> list = new ArrayList<>();
//
//        for (UserDrawCode userDrawCode : userDrawCodeList) {
//            map = new HashMap<>();
//            map.put("?????????", userDrawCode.getCustomerId());
//            map.put("?????????", userDrawCode.getCode());
//            map.put("????????????", DateUtil.getFormat(userDrawCode.getCreateDate()));
//            for (UserWinningInfo winningInfo : winningInfoList) {
//                if (userDrawCode.getCustomerId().equals(winningInfo.getCustomerId())) {
//                    map.put("??????", winningInfo.getName());
//                    map.put("?????????", winningInfo.getPhone());
//                    map.put("??????", winningInfo.getAddress());
//                }
//            }
//            for (PayOrder payOrder : orderList) {
//                if(payOrder.getOrderId().equals(userDrawCode.getOrderId())){
//                    map.put("?????????", payOrder.getOrderNo());
//                }
//            }
//
//            list.add(map);
//        }
//        String[] title = {"?????????", "?????????","?????????", "????????????", "??????", "?????????", "??????"};
//        String path = "/Users/tang/Desktop/???" + prizeId + "??? ????????????.xlsx";
//        FileUtil.exportFile(title, list, path);
//    }

//    @Test
//    void ??????(){
//        int i = Runtime.getRuntime().availableProcessors();
//        System.out.println(i);
//    }
//
//    @Test
//    void ??????() throws Exception {
//        //1-?????????  2-?????????  3-?????????
//        int status = 2;
//        int prizeId = 5;
//
//        List<UserDrawCode> userDrawCodeList = userDrawCodeRepository.findAllByStatusAndPrizeId(status, prizeId);
//
//        List<Integer> orderIdList = userDrawCodeList.stream().map(UserDrawCode::getOrderId).collect(Collectors.toList());
//
//        int TEST_THREAD_COUNT = 6;
//
//        List<List<Integer>> lists = averageAssign(orderIdList, TEST_THREAD_COUNT);
//
//        final CountDownLatch cdl = new CountDownLatch(TEST_THREAD_COUNT);//?????????????????????
//        int i = 1;
//        AtomicInteger atomicInteger = new AtomicInteger();
//
//        for (List<Integer> list : lists) {
//
//            Thread t = new Thread(() -> {
//                int a = atomicInteger.incrementAndGet();
//                try {
//                    refund(list, a);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                cdl.countDown();
//            });
//
//            t.setName("Refund-Thread-" + i);
//            t.start();
//            i = i + 1;
//        }
//
//        System.out.println("?????????????????????");
//
//        try {
//            //?????????????????????countDownLatch??????
//            //??????????????????????????????????????????0???????????????????????????
//            cdl.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("??????");
//    }

//    @Test
//    void ??????????????????() {
//        Date start = Date.from(LocalDateTime.of(2021,3,1,0,0,0).atZone(ZoneId.systemDefault()).toInstant());
//        Date end = Date.from(LocalDateTime.of(2021,3,31,23,59,59).atZone(ZoneId.systemDefault()).toInstant());
//        List<PayOrder> payOrderList = payOrderRepository.findAllByCreateDateBetween(start,end);
//        List<BocUserInfo> userInfoList =userInfoRepository.findAll();
//        System.out.println("????????????");
//        Map<String, String> map = new HashMap<>();
//        List<Map<String, String>> list = new ArrayList<>();
//
//        for (PayOrder payOrder : payOrderList) {
//            map = new HashMap<>();
//            map.put("?????????",payOrder.getOrderNo());
//            map.put("?????????",payOrder.getMerchantNo());
//            map.put("????????????",DateUtil.getFormat(payOrder.getCreateDate()));
//            map.put("????????????",payOrder.getPayAmount());
//            for (BocUserInfo userInfo : userInfoList) {
//                if(payOrder.getCustomerId().equals(userInfo.getCustomerId())){
//                    map.put("?????????",userInfo.getCustomerId());
//                    map.put("?????????",userInfo.getBranchId());
//                    break;
//                }
//            }
//            System.out.println(map);
//            list.add(map);
//        }
//        System.out.println(list.size());
//
//        String[] title = {"?????????", "?????????","?????????", "?????????", "????????????", "????????????"};
//        String path = "/Users/tang/Desktop/????????????.xlsx";
//        FileUtil.exportFile(title, list, path);
//    }

    public static <T> List<List<T>> averageAssign(List<T> list, int n) {
        List<List<T>> result = new ArrayList<List<T>>();
        int remaider = list.size() % n;  //(??????????????????)
        int number = list.size() / n;  //????????????
        int offset = 0;//?????????
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remaider > 0) {
                value = list.subList(i * number + offset, (i + 1) * number + offset + 1);
                remaider--;
                offset++;
            } else {
                value = list.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }

    public void refund(List<Integer> orderIdList, int workerId) throws Exception {
        String name = "[" + Thread.currentThread().getName() + "] ";

        System.out.println(name + "??????");
        List<PayOrder> payOrderList = payOrderRepository.findAllById(orderIdList);

        String merchantNo = bankConfig.getMerchantNo();

        List<PayOrder> saveOrderList = new ArrayList<>();
        SnowflakeIdFactory factory = new SnowflakeIdFactory(workerId, 10);
        int i = 0;
        String num = "";
        for (PayOrder payOrder : payOrderList) {
            if (payOrder.getOrderStatus() == 2) {

                i = i + 1;
                num = " [ " + payOrderList.size() + " - " + i + " ] ";
                String refundAmount = payOrder.getPayAmount();
                String mRefundSeq = String.valueOf(factory.nextId());
                String orderNo = payOrder.getOrderNo();
                String plainText = merchantNo + "|" + mRefundSeq + "|" + curCode + "|" + refundAmount + "|" + orderNo;

                if (tool == null) {
                    String keyStorePath = bankConfig.getKeyStorePath();
                    String keystorePwd = bankConfig.getKeystorePwd();
                    InputStream inputStream = PayController.class.getResourceAsStream(keyStorePath);
                    tool = PKCS7Tool.getSigner(inputStream, KeyStore.getDefaultType(), keystorePwd, keystorePwd);
                }

                String sign = tool.sign(plainText.getBytes(StandardCharsets.UTF_8));

                String action = "https://ebspay.boc.cn/PGWPortal/CommonRefundOrder.do";
                // ???????????????????????????????????????

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
                    System.out.println(name + num + "??????????????????" + payOrder.getOrderNo() + " ??????");
                } else {
                    System.out.println(name + num + "??????????????????" + payOrder.getOrderNo() + " ?????? " + rtnCd);
                }
            }
        }

        payOrderRepository.saveAll(saveOrderList);
        System.out.println(name + "????????????");
    }

//    @Test
//    void ????????????() throws Exception {
//
//        SnowflakeIdFactory factory = new SnowflakeIdFactory(1, 10);
//        String refundAmount = "1.0";
//        String mRefundSeq = String.valueOf(factory.nextId());
//        String orderNo = "21356072847064473600";
//        String merchantNo = "104130173720026";
//        String plainText = merchantNo + "|" + mRefundSeq + "|" + curCode + "|" + refundAmount + "|" + orderNo;
//
//        if (tool == null) {
//            String keyStorePath = bankConfig.getKeyStorePath();
//            String keystorePwd = bankConfig.getKeystorePwd();
//            InputStream inputStream = PayController.class.getResourceAsStream(keyStorePath);
//            tool = PKCS7Tool.getSigner(inputStream, KeyStore.getDefaultType(), keystorePwd, keystorePwd);
//        }
//
//        String sign = tool.sign(plainText.getBytes(StandardCharsets.UTF_8));
//
//        String action = "https://ebspay.boc.cn/PGWPortal/CommonRefundOrder.do";
//        // ???????????????????????????????????????
//
//        FormBody body = new FormBody.Builder()
//                .add("merchantNo", merchantNo)
//                .add("mRefundSeq", mRefundSeq)
//                .add("curCode", curCode)
//                .add("refundAmount", refundAmount)
//                .add("orderNo", orderNo)
//                .add("signData", sign)
//                .build();
//        String xml = OkhttpUtil.getInstance().httpPost(action, body);
//        System.out.println(xml);
//    }


//    @Test
//    void ??????() {
//        String code1 = "3523";
//        String code2 = "3113";
//        int prizeId = 6;
//
//        Optional<PrizeInfo> prizeInfoOptional = prizeInfoRepository.findById(prizeId);
//        if (prizeInfoOptional.isPresent()) {
//            PrizeInfo prizeInfo = prizeInfoOptional.get();
//
//            prizeInfo.setCode1(code1);
//            prizeInfo.setCode2(code2);
//            prizeInfo.setStatus(2);
//            prizeInfoRepository.save(prizeInfo);
//
//            List<UserDrawCode> userDrawCodeList = userDrawCodeRepository.findAllByPrizeId(prizeId);
//
//            List<List<UserDrawCode>> lists = averageAssign(userDrawCodeList, 6);
//
//        final CountDownLatch cdl = new CountDownLatch(6);//?????????????????????
//        int i = 1;
//        AtomicInteger atomicInteger = new AtomicInteger();
//
//        for (List<UserDrawCode> list : lists) {
//
//            Thread t = new Thread(() -> {
//                int a = atomicInteger.incrementAndGet();
//                try {
//                   upCode(list,code1,code2);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                cdl.countDown();
//            });
//
//            t.setName("Refund-Thread-" + i);
//            t.start();
//            i = i + 1;
//        }
//
//        System.out.println("?????????????????????");
//
//        try {
//            //?????????????????????countDownLatch??????
//            //??????????????????????????????????????????0???????????????????????????
//            cdl.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//
//
//
//        }
//
//
//        redisUtil.delKey("not_used_code");
//
//        List<DrawCode> drawCodeList = getCode();
//
//        List<String> codeList = drawCodeList.stream().map(DrawCode::getCode).collect(Collectors.toList());
//
//        redisUtil.setList("not_used_code", codeList);
//        System.out.println("??????");

//    }



    private void upCode(List<UserDrawCode> userDrawCodeList,String code1,String code2){
        String name = "[" + Thread.currentThread().getName() + "] ";

        System.out.println(name + "??????");
        for (UserDrawCode userDrawCode : userDrawCodeList) {
            if (userDrawCode.getCode().equals(code1) || userDrawCode.getCode().equals(code2)) {
                userDrawCode.setStatus(3);
                userDrawCode.setUpdateDate(new Date());
            } else {
                userDrawCode.setStatus(2);
                userDrawCode.setUpdateDate(new Date());
            }
        }
        for (UserDrawCode userDrawCode : userDrawCodeList) {
            System.out.println("ID:"+userDrawCode.getUserCodeId()+" CODE:"+ userDrawCode.getCode()+" ST:"+ userDrawCode.getStatus());
        }
        userDrawCodeRepository.saveAll(userDrawCodeList);
        System.out.println(name + "??????");
    }

    private List<DrawCode> getCode() {
        DrawCode drawCode = new DrawCode();
        List<DrawCode> drawCodeList = new ArrayList<>();
        for (int z = 0; z < 8; z++) {
            for (int i = 0; i < 10000; i++) {
                String code = "" + i;
                if (code.length() != 4) {
                    int len = 4 - code.length();
                    for (int j = 0; j < len; j++) {
                        code = "0" + code;
                    }
                }
                drawCode = new DrawCode();
                drawCode.setCode(code);
                drawCode.setStatus(1);
                drawCodeList.add(drawCode);
            }
        }

        return drawCodeList;
    }

}
