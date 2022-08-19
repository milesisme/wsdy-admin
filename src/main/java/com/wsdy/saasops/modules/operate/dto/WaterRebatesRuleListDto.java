package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "JWaterRebatesRuleListDto", description = "返水优惠活动规则")
public class WaterRebatesRuleListDto {

    @ApiModelProperty(value = "有效投注")
    private BigDecimal validAmountMin;

    @ApiModelProperty(value = "有效投注")
    private BigDecimal validAmountMax;

    @ApiModelProperty(value = "返水比例")
    private BigDecimal donateRatio;

    @ApiModelProperty(value = "流水倍数")
    private Double multipleWater;
}
