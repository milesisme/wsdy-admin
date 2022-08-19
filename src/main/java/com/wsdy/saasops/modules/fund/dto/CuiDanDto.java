package com.wsdy.saasops.modules.fund.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class CuiDanDto {

    /**
     * ID
     */
    private Integer id;


    /**
     * 用户名
     */
    private String loginName;


    /**
     * 金额
     */
    private BigDecimal amount;

}
