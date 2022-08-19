package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class RebateFirstChargeDto {
    @ApiModelProperty(value = "最小充值")
    private BigDecimal minCharge;

    @ApiModelProperty(value = "最大充值")
    private BigDecimal maxCharge;

    @ApiModelProperty(value = "推荐人返利")
    private BigDecimal referrer;

    @ApiModelProperty(value = "被推荐人返利")
    private BigDecimal referee;
}
