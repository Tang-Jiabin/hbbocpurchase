package com.zykj.purchase.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.PrivateKey;

import static java.lang.ClassLoader.getSystemResourceAsStream;


/**
 * 加密类
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2021-01-05
 */
@Slf4j
@Component
public class CipherUtil {

    @Autowired
    private BankConfig bankConfig;

    private PrivateKey privateK;

    public String deAES(String mBody, String mKey) {
        JSONObject jsonData;
        String storePath = bankConfig.getStorePath();
        // 证书别名
        String alias = bankConfig.getAlias();
        // 证书库密码
        String storePw = bankConfig.getStorePw();
        // 证书密码
        String keyPw = bankConfig.getKeyPw();

        if (privateK == null) {
            privateK = getPrivateKey(storePath, alias, storePw, keyPw);
        }

        byte[] mkbytes = decryptByPrivateKey(mKey, privateK);
        try {
            mKey = new String(mkbytes, "utf-8");
            mBody = new String(decryptAES(mBody, mKey), "utf-8");
            jsonData = JSON.parseObject(mBody);
            mBody = jsonData.getString("plainText");
            mBody = new String(decodeBase64(mBody), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return mBody;
    }

    private PrivateKey getPrivateKey(String storePath, String alias, String storePw, String keyPw) {

        InputStream inputStream = CipherUtil.class.getResourceAsStream(storePath);

        if (inputStream == null) {
            log.info("inputStream    : null");
            try {
//                /Users/tang/IdeaProjects/purchase/src/main/resources/secret
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

    public byte[] decryptByPrivateKey(String cipherText, PrivateKey privateKey) {

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
}
