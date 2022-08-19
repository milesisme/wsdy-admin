package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "JOtherDto", description = "其它活动")
public class JOtherDto {

    @ApiModelProperty(value = "流水倍数")
    private Integer multipleWater;

    @ApiModelProperty(value = "赠送金额")
    private BigDecimal donateAmount;

}
