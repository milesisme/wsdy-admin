package com.wsdy.saasops.api.modules.user.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "游戏类型平台DTO", description = "游戏类型平台DTO(用于AI推荐)")
public class AiRecommondSevenDto {

    @ApiModelProperty(value = "游戏类型")
    private Integer gameType;

    @ApiModelProperty(value = "平台ID")
    private Integer depotId;

    @ApiModelProperty(value = "有效投注总和")
    private BigDecimal totalBet;

}
