package com.wsdy.saasops.api.modules.activity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class HuPengLevelRewardDto {

    @ApiModelProperty(value = "等级")
    private Integer level;

    @ApiModelProperty(value = "最大奖励")
    private BigDecimal maxReward;

    @ApiModelProperty(value = "返佣比率")
    private BigDecimal rate;

}
