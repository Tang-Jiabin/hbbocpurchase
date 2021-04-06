package com.zykj.purchase.repository;

import com.zykj.purchase.pojo.PayOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PayOrderRepository extends JpaRepository<PayOrder, Integer> {
    PayOrder findByOrderNo(String orderNo);


    PayOrder findByPrizeIdAndCustomerId(Integer prizeId, String customerId);


    PayOrder findByCustomerIdAndPrizeId(String customerId,Integer prizeId);
    List<PayOrder> findAllByOrderStatus(int i);



    PayOrder findByCustomerIdAndPrizeIdAndOrderStatus(String customerId, Integer prizeId, int status);

    List<PayOrder> findByCustomerIdAndOrderStatus(String customerId, int status);

    List<PayOrder> findAllByOrderStatusAndPrizeId(int i, int i1);

    List<PayOrder> findAllByPrizeIdAndCreateDateAfter(int prizeId, Date date);

    List<PayOrder> findAllByOrderIdIn(List<Integer> orderIdList);

    List<PayOrder> findAllByCreateDateBetween(Date start,Date end);

}
