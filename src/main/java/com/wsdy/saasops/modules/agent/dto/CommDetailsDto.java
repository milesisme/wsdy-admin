package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class CommDetailsDto {

    @ApiModelProperty(value = "上级代理")
    private String agyAccount;

    @ApiModelProperty(value = "佣金比例")
    private BigDecimal rate;

    @ApiModelProperty(value = "佣金发放额")
    private BigDecimal commission;
}