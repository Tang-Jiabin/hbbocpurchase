package com.zykj.purchase.pojo;

import lombok.Data;

import javax.persistence.*;

/**
 * 银行用户信息
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-23
 */
@Data
@Entity
@Table(name = "boc_hb_user_info")
public class BocUserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    /**
     * 折扣券
     */
    private String discountCoupon;
    /**
     * 版本号
     */
    private String versionNo;
    /**
     * 客户名称
     **/
    private String customerName;
    /**
     * 分支机构Id
     */
    private String branchId;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 创建日期
     */
    private String createDate;
    /**
     *
     */
    private String cifNumber;
    /**
     * 身份证号
     */
    private String identityNumber;
    /**
     * 客户号
     */
    private String customerId;
    /**
     * 身份类型
     */
    private String identityType;
    /**
     * 地区编号 40740河北
     */
    private String ibknum;
    /**
     * 本地类型
     */
    private String localType;
    /**
     * 组织Id
     */
    private String orgId;
    /**
     *
     */
    private String merId;
    /**
     * 性别
     */
    private String gender;


}
