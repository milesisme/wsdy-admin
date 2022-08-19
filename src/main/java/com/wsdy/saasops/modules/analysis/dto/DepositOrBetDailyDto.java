package com.wsdy.saasops.modules.analysis.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 每日充值或者查询实体类
 */
@Data
public class DepositOrBetDailyDto {
    private BigDecimal amountSum;
    private String userName;
    private String date;
}
