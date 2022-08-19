package com.wsdy.saasops.aff.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AddBalanceDto {

    @ApiModelProperty(value = "会员名")
    private String membercode;

    @ApiModelProperty(value = "充值金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "备注")
    private String remarks;

    private String siteCode;
}
