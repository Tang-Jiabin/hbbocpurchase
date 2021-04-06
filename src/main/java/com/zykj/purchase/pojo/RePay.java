package com.zykj.purchase.pojo;

import lombok.Data;

import javax.persistence.*;

/**
 * 退款
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2021-02-22
 */
@Data
@Entity
@Table(name = "boc_hb_re_pay_2")
public class RePay {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Integer money;
}
