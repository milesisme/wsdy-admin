package com.wsdy.saasops.modules.activity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "活动规则", description = "首存送-返上级")
public class FirstChargeActivityRuleDto {

    @ApiModelProperty(value = "充值金额")
    private BigDecimal amountMin;

    @ApiModelProperty(value = "充值金额")
    private BigDecimal amountMax;

    @ApiModelProperty(value = "赠送金额")
    private BigDecimal donateAmount;

    @ApiModelProperty(value = "流水倍数")
    private Double multipleWater;

}
