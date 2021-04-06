package com.zykj.purchase.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zykj.purchase.common.BankConfig;
import com.zykj.purchase.common.DateUtil;
import com.zykj.purchase.common.ServerResponse;
import com.zykj.purchase.pojo.BocUserInfo;
import com.zykj.purchase.pojo.PrizeInfo;
import com.zykj.purchase.pojo.UserDrawCode;
import com.zykj.purchase.pojo.UserWinningInfo;
import com.zykj.purchase.repository.UserWinningInfoRepository;
import com.zykj.purchase.service.BocUserService;
import com.zykj.purchase.service.PrizeInfoService;
import com.zykj.purchase.service.UserDrawCodeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 用户
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-23
 */
@Slf4j
@RestController
@RequestMapping(value = "user")
public class UserController {

    @Resource
    private HttpServletRequest request;

    private final BankConfig bankConfig;
    private final BocUserService userService;
    private final PrizeInfoService prizeInfoService;
    private final UserDrawCodeService userDrawCodeService;
    private final UserWinningInfoRepository userWinningInfoRepository;

    private PrivateKey privateK;

    public UserController(BankConfig bankConfig, BocUserService userService, PrizeInfoService prizeInfoService, UserDrawCodeService userDrawCodeService, UserWinningInfoRepository userWinningInfoRepository) {
        this.bankConfig = bankConfig;
        this.userService = userService;
        this.prizeInfoService = prizeInfoService;
        this.userDrawCodeService = userDrawCodeService;
        this.userWinningInfoRepository = userWinningInfoRepository;
    }

    @PostMapping(value = "dealCusInfo")
    public ServerResponse<JSONObject> dealCusInfo() {
        String data = request.getParameter("data");
        if (!StringUtils.hasText(data)) {
            return ServerResponse.createMessage(410, "请重新授权");
        }
        JSONObject jsonData = JSONObject.parseObject(data);
        if (jsonData == null) {
            return ServerResponse.createMessage(410, "请重新授权");
        }
        String cipherText = jsonData.getString("cipherText");
        if (!StringUtils.hasText(cipherText)) {
            return ServerResponse.createMessage(410, "请重新授权");
        }
        JSONObject jsb = JSONObject.parseObject(cipherText);
        if (jsb == null) {
            return ServerResponse.createMessage(410, "请重新授权");
        }
        String mbody = jsb.getString("body");
        String mskey = jsb.getString("skey");

        if (privateK == null) {
            privateK = getPrivateKey();
        }

        byte[] mkbytes = decryptByPrivateKey(mskey, privateK);

        mskey = new String(mkbytes, StandardCharsets.UTF_8);
        mbody = new String(decryptAES(mbody, mskey), StandardCharsets.UTF_8);
        jsonData = JSON.parseObject(mbody);
        mbody = jsonData.getString("plainText");
        mbody = new String(decodeBase64(mbody), StandardCharsets.UTF_8);

        jsonData = JSONObject.parseObject(mbody);

        BocUserInfo userInfo = userService.save(jsonData);
        if (userInfo == null) {
            return ServerResponse.createMessage(410, "请重新授权");
        }
        if (!"40740".equals(userInfo.getIbknum())) {
            return ServerResponse.createMessage(411, "您不是河北地区用户");
        }

        PrizeInfo prizeInfo = prizeInfoService.findByActive();

        jsonData = new JSONObject();
        jsonData.put("customerId", userInfo.getCustomerId());
        LocalDateTime dateTime = LocalDateTime.of(2021, 3, 1, 0, 0, 0);
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        List<UserDrawCode> userDrawCodeList = userDrawCodeService.findAllByCustomerIdAndCreateDate(userInfo.getCustomerId(), date);
        String code = "0";
        Integer prizeId = 0;
        if (userDrawCodeList.size() > 0) {
            UserDrawCode userDrawCode = userDrawCodeList.get(userDrawCodeList.size() - 1);
            code = userDrawCode.getCode();
            prizeId = userDrawCode.getPrizeId();
        }

        if (prizeInfo == null) {
            jsonData.put("prizeId", prizeId);
            jsonData.put("code", code);
            jsonData.put("prizeName", "本期活动已结束");
            jsonData.put("prizeImg", "奖品图片");
            jsonData.put("drawDate", "本期活动已结束");
            jsonData.put("participants", "0");
        } else {
            if (prizeId == 0) {
                prizeId = prizeInfo.getPrizeId();
            }
            UserDrawCode drawCode = userDrawCodeService.findByCustomerIdAndPrizeId(userInfo.getCustomerId(), prizeInfo.getPrizeId());
            Optional<PrizeInfo> infoOptional = prizeInfoService.findOne(prizeId);
            if (infoOptional.isPresent()) {
                jsonData.put("codeMsg", infoOptional.get().getBatch());
            }
            jsonData.put("code", code);
            if (drawCode != null) {
                jsonData.put("code", drawCode.getCode());
            }
            jsonData.put("prizeId", prizeId);
            jsonData.put("prizeName", prizeInfo.getPrizeName());
            jsonData.put("prizeImg", prizeInfo.getPrizeImg());
            long second = DateUtil.difference(prizeInfo.getDrawDate(), new Date(), ChronoUnit.SECONDS);
            long hour = second / (60 * 60);
            long minute = second / 60;
            String dateStr = DateUtil.getFormat(prizeInfo.getDrawDate(),"MM月dd日HH:mm");

            String drawDate = "据"+dateStr+"开奖还有 <strong id='hour'>" + hour + "</strong>:<strong id='min'>" + (minute - (hour * 60)) + "</strong>:<strong id='sec'>" + (second - (minute * 60)) + "</strong>";

            jsonData.put("drawDate", drawDate);
            jsonData.put("participants", prizeInfo.getParticipants());
        }

        return ServerResponse.createMessage(ServerResponse.OK, "成功", jsonData);
    }

    private PrivateKey getPrivateKey() {
        String storePath = bankConfig.getStorePath();
        // 证书别名
        String alias = bankConfig.getAlias();
        // 证书库密码
        String storePw = bankConfig.getStorePw();
        // 证书密码
        String keyPw = bankConfig.getKeyPw();

        PrivateKey privateK = getPrivateKey(storePath, alias, storePw, keyPw);
        return privateK;
    }


    private PrivateKey getPrivateKey(String storePath, String alias, String storePw, String keyPw) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(storePath);

        if (inputStream == null) {
            log.info("inputStream    : null");
            try {
                inputStream = new FileInputStream(new File("/Users/tang/IdeaProjects/purchase/src/main/resources" + storePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        KeyStore ks = null;
        PrivateKey key = null;
        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(inputStream, storePw.toCharArray());
            inputStream.close();
            key = (PrivateKey) ks.getKey(alias, keyPw.toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return key;
    }

    public byte[] decryptByPrivateKey(String cipherText,
                                      PrivateKey privateKey) {

        byte[] sKey = decodeBase64(cipherText.replaceAll("[\\n\\r]", ""));

        Cipher CIPHER = null;
        byte[] bytes = null;
        try {
            CIPHER = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            CIPHER.init(Cipher.DECRYPT_MODE, privateKey);
            bytes = CIPHER.doFinal(sKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static byte[] decodeBase64(String conent) {
        byte[] bytes = null;
        try {
            bytes = conent.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            bytes = conent.getBytes(Charset.defaultCharset());
        }
        bytes = Base64.decodeBase64(bytes);
        return bytes;
    }

    public static byte[] decryptAES(String content, String key) {
        byte[] data = null;
        try {
            content = content.replaceAll("[\\n\\r]", "");
            data = decodeBase64(content);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(key.getBytes("utf-8"), "AES"),
                    createIVSpec("6DA0557C5119454A"));
            byte[] result = cipher.doFinal(data);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static IvParameterSpec createIVSpec(String ivStr) {
        byte[] data = null;
        if (ivStr == null) {
            ivStr = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ivStr);
        while (sb.length() < 16) {
            sb.append("0");
        }
        if (sb.length() > 16) {
            sb.setLength(16);
        }

        try {
            data = sb.toString().getBytes("utf-8");
        } catch (Exception ex) {
            return null;
        }
        return new IvParameterSpec(data);
    }

    @GetMapping(value = "getUserWinningInfo")
    public ServerResponse<JSONObject> getUserWinningInfo(Integer prizeId, String customerId) {

        UserWinningInfo winningInfo = userWinningInfoRepository.findByCustomerId(customerId);
        if (winningInfo == null) {
            return ServerResponse.createMessage(411, "信息为空");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phone", winningInfo.getPhone());
        jsonObject.put("name", winningInfo.getName());
        jsonObject.put("address", winningInfo.getAddress());
        return ServerResponse.createSuccess(jsonObject);
    }
}
