package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentUnionDto {


    @ApiModelProperty(value = "会员id")
    private Integer id;
    @ApiModelProperty(value = "存款金额")

    private BigDecimal depositAmount;
    @ApiModelProperty(value = "提款金额")

    private BigDecimal drawingAmount;
    @ApiModelProperty(value = "奖励红利")

    private BigDecimal bonusAmount;
    @ApiModelProperty(value = "派彩")
    private BigDecimal payout;
    @ApiModelProperty(value = "有效投注")
    private BigDecimal validbet;

    @ApiModelProperty(value = "新增会员标记")
    private Integer newMbrs;

    @ApiModelProperty(value = "首存标记")
    private  Integer newDeposits;

    @ApiModelProperty(value = "活跃人数标记")
    private Integer activeMbrs;


}