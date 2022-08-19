package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class AgentReportDto {

    @ApiModelProperty(value = "日期")
    private String date;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "0位 总代 其他为代理")
    private Integer parentId;

    @ApiModelProperty(value = "下线代理数")
    private Integer offlineAgentCount;

    @ApiModelProperty(value = "下线会员数")
    private Integer offlineMemberCount;

    @ApiModelProperty(value = "投注人数")
    private Integer betCount;

    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validBet;

    @ApiModelProperty(value = "派彩总额")
    private BigDecimal payOut;

    @ApiModelProperty(value = "手续费总额")
    private BigDecimal feeAmount;

    @ApiModelProperty(value = "红利总额")
    private BigDecimal bonusAmount;

    @ApiModelProperty(value = "直属会员数")
    public Integer accountNum;
}
