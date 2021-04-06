package com.zykj.purchase.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 用户中奖信息
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-27
 */

@Entity
@Data
@Table(name = "boc_hb_user_winning_info_2")
public class UserWinningInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer winId;

    private String customerId;

    private String name;

    private String phone;

    private String address;

    private Date createDate;
}
