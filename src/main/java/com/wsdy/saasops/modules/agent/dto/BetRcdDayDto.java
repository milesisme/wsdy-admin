package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BetRcdDayDto {


    @ApiModelProperty(value = "会员id")
    private Integer id;
    @ApiModelProperty(value = "派彩")
    private BigDecimal payout;
    @ApiModelProperty(value = "有效投注")
    private BigDecimal validbet;



}