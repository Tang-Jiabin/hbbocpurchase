package com.zykj.purchase.repository;

import com.zykj.purchase.pojo.UserWinningInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWinningInfoRepository extends JpaRepository<UserWinningInfo,Integer> {
    List<UserWinningInfo> findAllByCustomerIdIn(List<String> customerIdList);

    UserWinningInfo findByCustomerId(String customerId);
}
