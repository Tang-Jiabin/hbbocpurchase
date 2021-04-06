package com.zykj.purchase.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 银行文件配置
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-23
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bank")
public class BankConfig {

    private String merchantNo;

    private String orderUrl;

    private String keyStorePath;

    private String keystorePwd;

    private String certificatePath;

    private String storePath;

    private String alias;

    private String storePw;

    private String keyPw;

    private Integer redisBuffersNumber;

}
