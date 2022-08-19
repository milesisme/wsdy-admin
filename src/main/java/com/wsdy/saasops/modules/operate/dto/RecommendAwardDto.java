package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "RecommendAwardDto", description = "单次推荐奖励")
public class RecommendAwardDto {

    @ApiModelProperty(value = "是否勾选 true是 false否")
    private Boolean isAward;

    @ApiModelProperty(value = "奖励方式 0双方奖励 1推荐人奖励 2被推荐人奖励")
    private Integer awardType;

    @ApiModelProperty(value = "奖励基础被推荐玩家存款金额 >=")
    private BigDecimal awardBasics;

    @ApiModelProperty(value = "奖励金额")
    private BigDecimal awardMoney;

    @ApiModelProperty(value = "流水倍数")
    private Integer multipleWater;
}
