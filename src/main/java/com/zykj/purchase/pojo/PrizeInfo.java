package com.zykj.purchase.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 奖品信息
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-27
 */
@Data
@Entity
@Table(name = "boc_hb_prize_info_2")
public class PrizeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer prizeId;
    /**
     * 批次号
     */
    private String batch;

    /**
     * 奖品名称
     */
    private String prizeName;

    /**
     * 奖品图片
     */
    private String prizeImg;

    /**
     * 奖品金额
     */
    private String prizeAmount;

    /**
     * 奖品数量
     */
    private Integer prizeNumber;

    /**
     * 参与人数
     */
    private Integer participants;

    /**
     * 创建日期
     */
    private Date createDate;

    /**
     * 开始日期
     */
    private Date startDate;

    /**
     * 开奖日期
     */
    private Date drawDate;

    /**
     * 状态 1-进行中 2-已结束 3-未开始
     */
    private Integer status;

    private String code1;

    private String code2;
}
