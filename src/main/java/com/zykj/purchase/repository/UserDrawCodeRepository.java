package com.zykj.purchase.repository;

import com.zykj.purchase.pojo.UserDrawCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserDrawCodeRepository extends JpaRepository<UserDrawCode,Integer> {
    UserDrawCode findByCustomerIdAndPrizeId(String customerId, Integer prizeId);

    List<UserDrawCode> findAllByCustomerId(String customerId);

    List<UserDrawCode> findAllByPrizeIdAndStatus(Integer prizeId, int status);

    List<UserDrawCode> findAllByStatus(int status);

    List<UserDrawCode> findAllByCode(String code);

    List<UserDrawCode> findAllByPrizeId(int i);
    List<UserDrawCode> findAllByCustomerIdAndCreateDateAfter(String customerId, Date date);

    UserDrawCode findByCustomerIdAndPrizeId(String customerId, int i);

    List<UserDrawCode> findAllByStatusAndPrizeId(int i, int i1);
}
