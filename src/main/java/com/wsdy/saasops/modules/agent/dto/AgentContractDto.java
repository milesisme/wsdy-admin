package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class AgentContractDto {

    @ApiModelProperty(value = "佣金比例")
    private BigDecimal commissionRate;

    @ApiModelProperty(value = "净盈利金额")
    private BigDecimal netprofitAmount;

    @ApiModelProperty(value = "活跃人数")
    private Integer activenumber;

    @ApiModelProperty(value = "有效投注")
    private BigDecimal validBet;
}
