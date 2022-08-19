package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SettlementValidBetDto {

    @ApiModelProperty(value = "注单唯一编号，平台方获取")
    private String id;

    @ApiModelProperty(value = "有效投注")
    private BigDecimal validBet;

    @ApiModelProperty(value = "platform")
    private String platform;
}
