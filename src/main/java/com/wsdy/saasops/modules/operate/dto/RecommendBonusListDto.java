package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "RecommendBonusListDto", description = "基础有效投注额")
public class RecommendBonusListDto {

    @ApiModelProperty(value = "有效投注人")
    private Integer betNumber;

    @ApiModelProperty(value = "红利比例(%)")
    private BigDecimal bonusRatio;

}
