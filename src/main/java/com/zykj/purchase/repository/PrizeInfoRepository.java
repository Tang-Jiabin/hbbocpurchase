package com.zykj.purchase.repository;

import com.zykj.purchase.pojo.PrizeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrizeInfoRepository extends JpaRepository<PrizeInfo, Integer> {
    List<PrizeInfo> findAllByStatus(int i);
}
