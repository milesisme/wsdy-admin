package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommCatDetailsDto {

    @ApiModelProperty(value = "分类id")
    private Integer catId;

    @ApiModelProperty(value = "分类name")
    private String catName;

    @ApiModelProperty(value = "佣金方式 0有效投注额 1净盈利总额")
    private Integer commissionType;

    @ApiModelProperty(value = "百分比")
    private BigDecimal ratio;
}
