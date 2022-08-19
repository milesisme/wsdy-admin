package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "WaterDepotDto", description = "WaterDepotDto")
public class WaterDepotDto {

    @ApiModelProperty(value = "平台id")
    private Integer depotId;

    @ApiModelProperty(value = "分类")
    private Integer catId;

    @ApiModelProperty(value = "分类")
    private String catName;

    @ApiModelProperty(value = "depotName")
    private String depotName;

    @ApiModelProperty(value = "depotcode")
    private String depotCode;

    @ApiModelProperty(value = "已经结算有效投注")
    private BigDecimal validBet;

    @ApiModelProperty(value = "当前未计算有效投注")
    private BigDecimal currentValidBet;

    @ApiModelProperty(value = "返水比例")
    private BigDecimal donateRatio;

    @ApiModelProperty(value = "返水金额")
    private BigDecimal amount;
}
