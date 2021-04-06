package com.zykj.purchase.service;

import com.alibaba.fastjson.JSONObject;
import com.zykj.purchase.pojo.BocUserInfo;
import com.zykj.purchase.pojo.UserDrawCode;
import com.zykj.purchase.repository.BocUserInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-23
 */
@Slf4j
@Service
public class BocUserService {

    private final BocUserInfoRepository userRepository;

    @Autowired
    public BocUserService(BocUserInfoRepository userRepository) {
        this.userRepository = userRepository;
    }

    public synchronized BocUserInfo save(JSONObject jsonData) {

        String customerId = jsonData.getString("customerId");
        if (!StringUtils.hasText(customerId)) {
            return null;
        }
        BocUserInfo userInfo = userRepository.findByCustomerId(customerId);

        if (userInfo == null) {
            userInfo = new BocUserInfo();
            userInfo = jsonData.toJavaObject(BocUserInfo.class);
            userInfo = userRepository.save(userInfo);
        }

        return userInfo;
    }


    public BocUserInfo findByCustomerId(String customerId) {
        return userRepository.findByCustomerId(customerId);
    }


}
