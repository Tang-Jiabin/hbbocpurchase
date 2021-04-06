package com.zykj.purchase.controller;

import com.alibaba.fastjson.JSONObject;
import com.zykj.purchase.common.BankConfig;
import com.zykj.purchase.common.DateUtil;
import com.zykj.purchase.common.RedisUtil;
import com.zykj.purchase.common.ServerResponse;
import com.zykj.purchase.pojo.*;
import com.zykj.purchase.repository.DrawCodeRepository;
import com.zykj.purchase.repository.PrizeInfoRepository;
import com.zykj.purchase.repository.UserWinningInfoRepository;
import com.zykj.purchase.service.DrawCodeService;
import com.zykj.purchase.service.PayOrderService;
import com.zykj.purchase.service.PrizeInfoService;
import com.zykj.purchase.service.UserDrawCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 抽奖
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-22
 */
@Slf4j
@RestController
@RequestMapping(value = "luckDraw")
public class LuckDrawController {

    private final RedisUtil redisUtil;
    private final DrawCodeRepository drawCodeRepository;
    private final DrawCodeService drawCodeService;
    private final PayOrderService payOrderService;
    private final UserDrawCodeService userDrawCodeService;
    private final PrizeInfoService prizeInfoService;
    private final UserWinningInfoRepository userWinningInfoRepository;
    private final BankConfig bankConfig;
    private final PrizeInfoRepository prizeInfoRepository;


    public LuckDrawController(DrawCodeRepository drawCodeRepository, RedisUtil redisUtil, DrawCodeService drawCodeService, PayOrderService payOrderService, UserDrawCodeService userDrawCodeService, PrizeInfoService prizeInfoService, UserWinningInfoRepository userWinningInfoRepository, BankConfig bankConfig, PrizeInfoRepository prizeInfoRepository) {
        this.drawCodeRepository = drawCodeRepository;
        this.redisUtil = redisUtil;
        this.drawCodeService = drawCodeService;
        this.payOrderService = payOrderService;
        this.userDrawCodeService = userDrawCodeService;
        this.prizeInfoService = prizeInfoService;
        this.userWinningInfoRepository = userWinningInfoRepository;
        this.bankConfig = bankConfig;
        this.prizeInfoRepository = prizeInfoRepository;
    }

    @GetMapping(value = "getDrawCode")
    public ServerResponse<JSONObject> getDrawCode(Integer prizeId, String customerId) {

        UserDrawCode userDrawCode = userDrawCodeService.findByCustomerIdAndPrizeId(customerId, prizeId);
        Optional<PrizeInfo> prizeInfoOptional = prizeInfoService.findOne(prizeId);
        if (!prizeInfoOptional.isPresent()) {
            return ServerResponse.createMessage(414, "中奖码读取错误");
        }
        JSONObject jsonObject = new JSONObject();
        if (userDrawCode != null) {
            jsonObject.put("code", userDrawCode.getCode());
            jsonObject.put("batch", prizeInfoOptional.get().getBatch());
            jsonObject.put("drawDate", prizeInfoOptional.get().getDrawDate());
            return ServerResponse.createSuccess(jsonObject);
        }
        return ServerResponse.createMessage(414, "中奖码读取错误");
    }


    @GetMapping(value = "getWinningList")
    public ServerResponse<JSONObject> getWinningList(String customerId) {

        List<PrizeInfo> prizeInfoList = prizeInfoService.findAll();
        List<UserDrawCode> userDrawCodeList = userDrawCodeService.findAllByCustomerId(customerId);
        JSONObject info = new JSONObject();
        List<JSONObject> jsonObjectList = new ArrayList<>();
        JSONObject jsonPrizeInfo;
        for (PrizeInfo prizeInfo : prizeInfoList) {
            for (UserDrawCode userDrawCode : userDrawCodeList) {
                if (userDrawCode.getPrizeId().equals(prizeInfo.getPrizeId())) {
                    jsonPrizeInfo = new JSONObject();
                    jsonPrizeInfo.put("batch", prizeInfo.getBatch());
                    jsonPrizeInfo.put("name", prizeInfo.getPrizeName());
                    jsonPrizeInfo.put("prizeId", prizeInfo.getPrizeId());
                    jsonPrizeInfo.put("drawDate", DateUtil.getFormat(prizeInfo.getDrawDate(),"MM月dd日"));
                    if (prizeInfo.getStatus() == 1) {
                        jsonPrizeInfo.put("status", "进行中");
                    } else if (prizeInfo.getStatus() == 2) {
                        jsonPrizeInfo.put("status", "未中奖");
                        if (userDrawCode.getStatus() == 3) {
                            jsonPrizeInfo.put("status", "已中奖领奖");
                        }
                        if (userDrawCode.getStatus() == 4) {
                            jsonPrizeInfo.put("status", "已弃奖");
                        }
                    }
                    jsonObjectList.add(jsonPrizeInfo);
                }
            }
        }
        info.put("me", jsonObjectList);
        jsonObjectList = new ArrayList<>();
        for (PrizeInfo prizeInfo : prizeInfoList) {
            jsonPrizeInfo = new JSONObject();
            jsonPrizeInfo.put("batch", prizeInfo.getBatch());
            jsonPrizeInfo.put("name", prizeInfo.getPrizeName());
            jsonPrizeInfo.put("prizeId", prizeInfo.getPrizeId());
            jsonPrizeInfo.put("drawDate", DateUtil.getFormat(prizeInfo.getDrawDate(),"MM月dd日"));
            if (prizeInfo.getStatus() == 1) {
                jsonPrizeInfo.put("status", "进行中");
            } else if (prizeInfo.getStatus() == 2) {
                jsonPrizeInfo.put("status", "查看中奖名单");
            } else if (prizeInfo.getStatus() == 3) {
                jsonPrizeInfo.put("status", "未开始");
            }
            jsonObjectList.add(jsonPrizeInfo);
        }
        info.put("whole", jsonObjectList);

        return ServerResponse.createSuccess(info);
    }

    @GetMapping(value = "getPrizeInfo")
    public ServerResponse<JSONObject> getPrizeInfo(String customerId, Integer prizeId) {
        Optional<PrizeInfo> optionalPrizeInfo = prizeInfoService.findOne(prizeId);
        if (optionalPrizeInfo.isPresent()) {
            PrizeInfo prizeInfo = optionalPrizeInfo.get();
            UserDrawCode userDrawCode = userDrawCodeService.findByCustomerIdAndPrizeId(customerId, prizeId);
            if (userDrawCode == null) {
                return ServerResponse.createMessage(411, "奖品信息有误");
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("batch", prizeInfo.getBatch());
            jsonObject.put("code1", prizeInfo.getCode1());
            jsonObject.put("code2", prizeInfo.getCode2());
            jsonObject.put("myCode", userDrawCode.getCode());
            jsonObject.put("status", userDrawCode.getStatus());
            return ServerResponse.createSuccess(jsonObject);

        } else {
            return ServerResponse.createMessage(411, "奖品信息有误");
        }

    }

    @GetMapping(value = "submitPrizeInfo")
    public ServerResponse<String> submitPrizeInfo(Integer prizeId, String customerId, String name, String phone, String address) {
        UserDrawCode userDrawCode = userDrawCodeService.findByCustomerIdAndPrizeId(customerId, prizeId);
        if (userDrawCode == null) {
            return ServerResponse.createMessage(411, "未查询到中奖信息");
        }

        UserWinningInfo userWinningInfo = userWinningInfoRepository.findByCustomerId(customerId);
        if (userWinningInfo == null) {
            userWinningInfo = new UserWinningInfo();
        }
        userWinningInfo.setCustomerId(customerId);
        userWinningInfo.setName(StringUtils.hasLength(name) ? name : "");
        userWinningInfo.setPhone(StringUtils.hasLength(phone) ? phone : "");
        userWinningInfo.setAddress(StringUtils.hasLength(address) ? address : "");
        userWinningInfo.setCreateDate(new Date());
        userWinningInfoRepository.save(userWinningInfo);
        return ServerResponse.createSuccess("");
    }


    @GetMapping(value = "getWinningDetails")
    public ServerResponse getWinningDetails(Integer prizeId, String customerId) {
        if (prizeId == null) {
            return ServerResponse.createMessage(411, "未查询到奖品信息");
        }
        Optional<PrizeInfo> optional = prizeInfoService.findOne(prizeId);
        if (!optional.isPresent()) {
            return ServerResponse.createMessage(411, "未查询到奖品信息");
        }
        PrizeInfo prizeInfo = optional.get();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("batch", prizeInfo.getBatch());
        jsonObject.put("prizeName", prizeInfo.getPrizeName());
        jsonObject.put("prizeImg", prizeInfo.getPrizeImg());
        jsonObject.put("prizeNumber", prizeInfo.getPrizeNumber());
        jsonObject.put("code1", prizeInfo.getCode1());
        jsonObject.put("code2", prizeInfo.getCode2());
        jsonObject.put("participants", prizeInfo.getParticipants());

        List<UserDrawCode> userDrawCodeList = userDrawCodeService.findAllByPrizeIdAndStatus(prizeId, 3);

//        List<String> customerIdList = userDrawCodeList.stream().map(UserDrawCode::getCustomerId).collect(Collectors.toList());

//        List<UserWinningInfo> userWinningInfoList = userWinningInfoRepository.findAllByCustomerIdIn(customerIdList);
        JSONObject userDrawJson = new JSONObject();
        List<JSONObject> jsonList = new ArrayList<>();
        for (UserDrawCode userDrawCode : userDrawCodeList) {
//            for (UserWinningInfo userWinningInfo : userWinningInfoList) {
//                if (userDrawCode.getCustomerId().equals(userWinningInfo.getCustomerId())) {
                    userDrawJson = new JSONObject();
                    userDrawJson.put("code", userDrawCode.getCode());
//                    userDrawJson.put("customerId", userWinningInfo.getPhone().substring(0, 3) + "****" + userWinningInfo.getPhone().substring(7));
                    userDrawJson.put("customerId", userDrawCode.getCustomerId());
                    jsonList.add(userDrawJson);
//                }
//            }

        }
        jsonObject.put("userDrawJsonList", jsonList);

        return ServerResponse.createSuccess(jsonObject);
    }


    @GetMapping(value = "test")
    public void test() {
//        String code1 = "0259";
//        String code2 = "9260";
//        int prizeId = 4;
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
//            List<UserDrawCode> userDrawCodeList = userDrawCodeService.findAllByPrizeId(prizeId);
//
//            for (UserDrawCode userDrawCode : userDrawCodeList) {
//                if(userDrawCode.getCode().equals(code1)||userDrawCode.getCode().equals(code2)){
//                    userDrawCode.setStatus(3);
//                    userDrawCode.setUpdateDate(new Date());
//                }else {
//                    userDrawCode.setStatus(2);
//                    userDrawCode.setUpdateDate(new Date());
//                }
//            }
//            for (UserDrawCode userDrawCode : userDrawCodeList) {
//                log.info("ID:{} CODE:{} ST:{}",userDrawCode.getUserCodeId(),userDrawCode.getCode(),userDrawCode.getStatus());
//            }
//            userDrawCodeService.saveAll(userDrawCodeList);
//        }
//        System.out.println("结束");
//
//
//        redisUtil.delKey("not_used_code");
//
//        List<DrawCode> drawCodeList = getCode();
//
//        List<String> codeList = drawCodeList.stream().map(DrawCode::getCode).collect(Collectors.toList());
//
//        redisUtil.setList("not_used_code",codeList);

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


    @Scheduled(cron = "0 */5 * * * ?")
    @GetMapping(value = "timing")
    public void timing() {
        log.info("定时器: {} ", DateUtil.getFormat(new Date(), "yyyy-MM-dd HH:mm:ss"));
        List<PayOrder> payOrderList = payOrderService.findAllByOrderStatus(1);

        Date date = new Date();
        List<PayOrder> saveOrderList = new ArrayList<>();
        for (PayOrder payOrder : payOrderList) {
            log.info(DateUtil.getFormat(payOrder.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
            int min = DateUtil.difference(date, payOrder.getCreateDate(), ChronoUnit.MINUTES);
            if (min >= 20) {

                if (StringUtils.hasLength(payOrder.getCode())) {

                    log.info("超时支付 中奖码恢复：{}", payOrder.getCode());

                    redisUtil.getStringRedisTemplate().opsForList().rightPush("not_used_code", payOrder.getCode());
                }

                saveOrderList.add(payOrder);
            }
        }
        if (saveOrderList.size() >= 1) {
            payOrderService.deleteAll(saveOrderList);
        }

    }


}
