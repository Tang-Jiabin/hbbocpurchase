package com.zykj.purchase.pojo;

import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;

import javax.persistence.*;

/**
 * 中奖码
 *
 * @author J.Tang
 * @version V1.0
 * @email seven_tjb@163.com
 * @date 2020-12-22
 */

@Data
@Entity
@Table(name = "boc_hb_draw_code_2")
public class DrawCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codeId;

    private String code;

    private Integer status;

}
