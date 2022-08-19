package com.wsdy.saasops.modules.operate.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "ActRescueRuleDto", description = "救援金")
public class ActRescueRuleDto {

    @ApiModelProperty(value = "最小派彩")
    private BigDecimal payoutMin;

    @ApiModelProperty(value = "最大派彩")
    private BigDecimal payoutMax;

    @ApiModelProperty(value = "赠送类型 0按比例 1按金额")
    private Integer donateType;

    @ApiModelProperty(value = "赠送比例")
    private BigDecimal donateAmount;

    @ApiModelProperty(value = "赠送金额 or 赠送比例")
    private BigDecimal donateAmountMax;

    @ApiModelProperty(value = "存款金额限制 0不限制 1限制最低要求")
    private Integer depositAmountType;

    @ApiModelProperty(value = "最小存款金额")
    private BigDecimal depositMin;

    @ApiModelProperty(value = "流水倍数")
    private Double multipleWater;

    @JsonIgnore
    @ApiModelProperty(value = "派彩")
    private BigDecimal payout;
    @JsonIgnore
    @ApiModelProperty(value = "存款金额")
    private BigDecimal depositAmount;

}
