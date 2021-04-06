package com.zykj.purchase.service;

import com.zykj.purchase.pojo.PrizeInfo;
import com.zykj.purchase.repository.PrizeInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 奖品信息
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-27
 */
@Service
public class PrizeInfoService {

    private final PrizeInfoRepository prizeInfoRepository;

    @Autowired
    public PrizeInfoService(PrizeInfoRepository prizeInfoRepository) {
        this.prizeInfoRepository = prizeInfoRepository;
    }

    public PrizeInfo findByActive() {
        List<PrizeInfo> prizeInfoList = prizeInfoRepository.findAllByStatus(1);
        if (prizeInfoList.size()>0) {
            return prizeInfoList.get(0);
        }
        return null;
    }

    public Optional<PrizeInfo> findOne(int prizeId) {

        return prizeInfoRepository.findById(prizeId);
    }

    public List<PrizeInfo> findAll() {
        return prizeInfoRepository.findAll();
    }

    public PrizeInfo save(PrizeInfo prizeInfo) {
        return prizeInfoRepository.saveAndFlush(prizeInfo);
    }
}
