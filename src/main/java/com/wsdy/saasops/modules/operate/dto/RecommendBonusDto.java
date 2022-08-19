package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "RecommendBonusDto", description = "推荐红利(奖励推荐人)")
public class RecommendBonusDto {

    @ApiModelProperty(value = "是否勾选 true是 false否")
    private Boolean isBonus;

    @ApiModelProperty(value = "基础有效投注额")
    private BigDecimal betMoney;

    @ApiModelProperty(value = "基础有效投注额区间")
    private List<RecommendBonusListDto> bonusListDtos;

    @ApiModelProperty(value = "推荐红利上限")
    private BigDecimal bonusMax;

    @ApiModelProperty(value = "流水倍数")
    private Integer multipleWater;
}
