package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "RedDrawScopeDto", description = "领取次数（范围）")
public class RedDrawScopeDto {

    @ApiModelProperty(value = "区间范围")
    private BigDecimal scopeMoney;

    @ApiModelProperty(value = "领取次数")
    private String drawNumber;
}
