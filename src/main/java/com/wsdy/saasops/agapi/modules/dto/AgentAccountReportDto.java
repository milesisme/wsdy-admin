package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class AgentAccountReportDto {

    @ApiModelProperty(value = "会员帐号")
    private Integer loginName;

    @ApiModelProperty(value = "存款总额")
    private BigDecimal depositAmount;

    @ApiModelProperty(value = "红利总额")
    private BigDecimal bonusAmount;

    @ApiModelProperty(value = "有效投注额 投注总额")
    private BigDecimal validBet;

    @ApiModelProperty(value = "输赢总额 派彩")
    private BigDecimal payout;

    @ApiModelProperty(value = "彩金总额 奖池")
    private BigDecimal jackpotPayout;

    @ApiModelProperty(value = "提款总额")
    private BigDecimal withdrawAmount;

    /*@ApiModelProperty(value = "手续费总额 扣会员自己的")
    private BigDecimal feeAmount;*/

    @ApiModelProperty(value = "人工调整总额")
    private BigDecimal auditAmount;

    @ApiModelProperty(value = "注册时间")
    private String registerTime;
}
