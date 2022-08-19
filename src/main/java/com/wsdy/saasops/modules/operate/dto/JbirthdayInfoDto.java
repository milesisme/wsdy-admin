package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "JbirthdayDto", description = "生日礼金")
public class JbirthdayInfoDto {

    @ApiModelProperty(value = "层级id")
    private Integer actLevelId;

    @ApiModelProperty(value = "当日最低投注")
    private BigDecimal validbetMin;

    @ApiModelProperty(value = "当日最低存款")
    private BigDecimal depositMin;

    @ApiModelProperty(value = "赠送金额")
    private BigDecimal donateAmount;

    @ApiModelProperty(value = "流水倍数")
    private Double multipleWater;



    private BigDecimal depositAmount;
    private BigDecimal validBet;

    @ApiModelProperty(value = "等级前端页面使用")
    private Integer accountLevel;
    @ApiModelProperty(value = "等级前端页面使用")
    private String tierName;

}
