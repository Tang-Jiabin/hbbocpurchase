package com.zykj.purchase.service;

import com.zykj.purchase.common.BankConfig;
import com.zykj.purchase.common.RedisUtil;
import com.zykj.purchase.common.ServerResponse;
import com.zykj.purchase.pojo.DrawCode;
import com.zykj.purchase.repository.DrawCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 中奖码
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-22
 */
@Slf4j
@Service
public class DrawCodeService {

    private final DrawCodeRepository drawCodeRepository;
    private final RedisUtil redisUtil;
    private final BankConfig bankConfig;

    @Autowired
    public DrawCodeService(DrawCodeRepository drawCodeRepository, RedisUtil redisUtil, BankConfig bankConfig) {
        this.drawCodeRepository = drawCodeRepository;
        this.redisUtil = redisUtil;
        this.bankConfig = bankConfig;
    }

    public int setDrawCode() {


        Pageable pr = PageRequest.of(0, bankConfig.getRedisBuffersNumber());
        List<DrawCode> drawCodeList = drawCodeRepository.findAllByStatus(pr, 1);

        if (drawCodeList.size() <= 0) {
            log.info("数据库可用中奖码不足，改变状态");
            drawCodeList = drawCodeRepository.findAllByStatus(2);
            List<DrawCode> saveCodeList = new ArrayList<>();
            drawCodeList.forEach(drawCode -> {
                drawCode.setStatus(1);
                saveCodeList.add(drawCode);
            });
            drawCodeRepository.saveAll(saveCodeList);
            drawCodeList = drawCodeRepository.findAllByStatus(pr, 1);
        }
        log.info("添加中奖码到Redis");

        return updateUsedDrawCode(drawCodeList);

    }

    public int updateUsedDrawCode(List<DrawCode> drawCodeList) {
        List<String> collect = drawCodeList.stream().map(DrawCode::getCode).collect(Collectors.toList());
        List<DrawCode> saveCodeList = new ArrayList<>();
        drawCodeList.forEach(drawCode -> {
            drawCode.setStatus(2);
            saveCodeList.add(drawCode);
        });
        drawCodeRepository.saveAll(saveCodeList);
        redisUtil.setList("not_used_code", collect);
        return collect.size();
    }

    public List<DrawCode> findAllByStatus(Pageable pr, int i) {
        return drawCodeRepository.findAllByStatus(pr, i);
    }

    public List<DrawCode> findAllByStatus(int i) {
        return drawCodeRepository.findAllByStatus(i);
    }

    public int countByStatus(int i) {
        return drawCodeRepository.countByStatus(i);
    }

    public DrawCode save(DrawCode drawCode) {
        return drawCodeRepository.save(drawCode);
    }

    public DrawCode findByCodeAndStatus(String usedCode, int i) {
        return drawCodeRepository.findByCodeAndStatus(usedCode, i);
    }

    public List<DrawCode> saveAll(List<DrawCode> saveCodeList) {
        return drawCodeRepository.saveAll(saveCodeList);
    }
}
