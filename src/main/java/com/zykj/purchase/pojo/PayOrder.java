package com.zykj.purchase.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 支付订单
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-25
 */

@Data
@Entity
@Table(name = "boc_hb_pay_order_2")
public class PayOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    private String merchantNo;
    private String orderNo;
    private String orderSeq;
    private String cardTyp;
    private String payTime;
    /**
     * 1-未支付 2-已支付 3-已退款
     **/
    private Integer orderStatus;

    private String payAmount;

    private Date createDate;

    private String customerId;

    private Integer prizeId;

    private String code;

    private String refundNo;

    private Date refundDate;

}
