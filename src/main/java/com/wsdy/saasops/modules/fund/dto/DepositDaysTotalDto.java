package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DepositDaysTotalDto {
    @ApiModelProperty("会员id")
    private Integer accountId;

    @ApiModelProperty("连续存款天数")
    private Integer dayCount;

    @ApiModelProperty("连续存款天数内存款总额")
    private BigDecimal totalAmount;

    @ApiModelProperty("连续存款天数最后时间 yyyy-mm-dd")
    private String continueDate;

    @ApiModelProperty(value = "最小存款金额")
    private BigDecimal depositMin;

    @ApiModelProperty(value = "最大存款金额")
    private BigDecimal depositMax;

    @ApiModelProperty(value = "审核开始时间 yyyy-MM-dd HH:mm:ss")
    private String auditTimeFrom;

    @ApiModelProperty(value = "审核结束时间 yyyy-MM-dd HH:mm:ss")
    private String auditTimeTo;

}
