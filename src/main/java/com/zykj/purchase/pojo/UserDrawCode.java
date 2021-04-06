package com.zykj.purchase.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 用户中奖码
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-27
 */
@Data
@Entity
@Table(name = "boc_hb_user_draw_code_2")
public class UserDrawCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userCodeId;

    private String code;

    private Integer orderId;

    /**
     * 1-未开奖 2-未中奖 3-已中奖 4-已弃奖
     */
    private Integer status;

    private String customerId;

    private Date createDate;

    private Date updateDate;

    private Integer prizeId;





}
