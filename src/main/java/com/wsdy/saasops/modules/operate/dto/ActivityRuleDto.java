package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "AccountGroup", description = "活动规则")
public class ActivityRuleDto {

    @ApiModelProperty(value = "充值金额")
    private BigDecimal amountMin;

    @ApiModelProperty(value = "充值金额")
    private BigDecimal amountMax;

    @ApiModelProperty(value = "赠送类型 0按比例 1按金额")
    private Integer donateType;

    @ApiModelProperty(value = "赠送金额 or 赠送比例")
    private BigDecimal donateAmount;

    @ApiModelProperty(value = "赠送最高金额")
    private BigDecimal donateAmountMax;

    @ApiModelProperty(value = "流水倍数")
    private Double multipleWater;

    @ApiModelProperty(value = "所属存款类型 0全部, 1, 2, 3, 4, 5, 7, 8, 100USDT, 101银行卡转账")
    private Integer paymentType;
}
