package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "JSignInDto", description = "签到规则")
public class SignInRuleDto {

    @ApiModelProperty(value = "有效投注 or 充值金额")
    private BigDecimal validAmountMin;

    @ApiModelProperty(value = "有效投注 or 充值金额")
    private BigDecimal validAmountMax;

    @ApiModelProperty(value = "赠送类型 0按比例 1按金额")
    private Integer donateType;

    @ApiModelProperty(value = "赠送金额")
    private BigDecimal donateAmount;

    @ApiModelProperty(value = "赠送比例")
    private BigDecimal donateRatio;

    @ApiModelProperty(value = "赠送最高金额")
    private BigDecimal donateAmountMax;

    @ApiModelProperty(value = "签到额外奖励天数 0无")
    private Integer signInNumber;

    @ApiModelProperty(value = "送现金")
    private BigDecimal cash;

    @ApiModelProperty(value = "稽核流水类型 0按倍数 1按金额")
    private Integer multipleWaterType;

    @ApiModelProperty(value = "稽核流水倍数")
    private Double multipleWater;

    @ApiModelProperty(value = "稽核流水金额")
    private BigDecimal multipleWaterMoney;
}
