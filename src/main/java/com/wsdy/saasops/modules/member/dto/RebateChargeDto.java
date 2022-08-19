package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RebateChargeDto {

    @ApiModelProperty(value = "邀请数量")
    private Integer num;

    @ApiModelProperty(value = "最小充值")
    private BigDecimal minCharge;


    private String name;

    @ApiModelProperty(value = "奖励")
    private BigDecimal award;

    @ApiModelProperty(value = "流水倍数")
    private BigDecimal multiple;


}
