package com.zykj.purchase.repository;


import com.zykj.purchase.pojo.BocUserInfo;
import com.zykj.purchase.pojo.UserDrawCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BocUserInfoRepository extends JpaRepository<BocUserInfo,Integer> {
    BocUserInfo findByCustomerId(String customerId);
}
