package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommReportDto {

    @ApiModelProperty(value = "当月佣金")
    private BigDecimal commission;

    @ApiModelProperty(value = "活跃用户")
    private Integer active;

    @ApiModelProperty(value = "净输赢")
    private BigDecimal netwinlose;

    @ApiModelProperty(value = "佣金比例")
    private BigDecimal rate;


}
