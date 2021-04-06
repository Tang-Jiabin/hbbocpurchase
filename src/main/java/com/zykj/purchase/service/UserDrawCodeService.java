package com.zykj.purchase.service;

import com.zykj.purchase.pojo.UserDrawCode;
import com.zykj.purchase.repository.UserDrawCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 用户中间吗
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-27
 */
@Service
public class UserDrawCodeService {

    private final UserDrawCodeRepository userDrawCodeRepository;

    @Autowired
    public UserDrawCodeService(UserDrawCodeRepository userDrawCodeRepository) {
        this.userDrawCodeRepository = userDrawCodeRepository;

    }

    public UserDrawCode save(UserDrawCode userDrawCode) {
        return userDrawCodeRepository.saveAndFlush(userDrawCode);
    }

    public UserDrawCode findByCustomerIdAndPrizeId(String customerId, Integer prizeId) {
        return userDrawCodeRepository.findByCustomerIdAndPrizeId(customerId,prizeId);
    }

    public List<UserDrawCode> findAllByCustomerId(String customerId) {
        return userDrawCodeRepository.findAllByCustomerId(customerId);
    }

    public List<UserDrawCode> findAllByPrizeIdAndStatus(Integer prizeId, int status) {
        return userDrawCodeRepository.findAllByPrizeIdAndStatus(prizeId,status);
    }

    public List<UserDrawCode> findAllByStatus(int status) {
        return userDrawCodeRepository.findAllByStatus(status);
    }

    public List<UserDrawCode> findAll() {
        return userDrawCodeRepository.findAll();
    }

    public List<UserDrawCode> findAllByCode(String code) {
        return userDrawCodeRepository.findAllByCode(code);
    }

    public List<UserDrawCode> saveAll(List<UserDrawCode> saveList) {
        return userDrawCodeRepository.saveAll(saveList);
    }

    public List<UserDrawCode> findAllByPrizeId(int i) {
        return userDrawCodeRepository.findAllByPrizeId(i);
    }

    public List<UserDrawCode> findAllByCustomerIdAndCreateDate(String customerId, Date date) {
        return userDrawCodeRepository.findAllByCustomerIdAndCreateDateAfter(customerId,date);
    }
}
