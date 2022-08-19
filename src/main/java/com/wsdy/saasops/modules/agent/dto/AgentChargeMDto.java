package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentChargeMDto {

    @ApiModelProperty(value = "会员账号")
    private String loginName;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "部门名称")
    private String departmentName;

    @ApiModelProperty(value = "存款")
    private BigDecimal depositAmount;

    @ApiModelProperty(value = "取款")
    private BigDecimal withdrawAmount;

    @ApiModelProperty(value = "存提和")
    private BigDecimal sumDepositAndWithdrawal;

    @ApiModelProperty(value = "服务费")
    private BigDecimal cost;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "开始时间开始")
    private String startTime;

    @ApiModelProperty(value = "开始时间结束")
    private String endTime;
}