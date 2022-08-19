package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "JUpgradeBonusLevelDto", description = "升级礼金")
public class JUpgradeBonusLevelDto {

    @ApiModelProperty(value = "层级id")
    private Integer actLevelId;

    @ApiModelProperty(value = "赠送金额")
    private BigDecimal donateAmount;

    @ApiModelProperty(value = "流水倍数")
    private Double multipleWater;



    @ApiModelProperty(value = "等级前端页面使用")
    private Integer accountLevel;
    @ApiModelProperty(value = "等级前端页面使用")
    private String tierName;
}
