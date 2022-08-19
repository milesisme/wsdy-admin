package com.wsdy.saasops.agapi.modulesV2.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class AgentFundDto {

    @ApiModelProperty(value = "用户")
    private String loginName;

    @ApiModelProperty(value = "用户id")
    private Integer accountId;

    @ApiModelProperty(value = "代理数")
    private Integer agentNum;

    @ApiModelProperty(value = "代理会员数")
    private Integer accountNum;

    @ApiModelProperty(value = "点数")
    private BigDecimal balance;

    @ApiModelProperty(value = "别名")
    private String realName;



    @ApiModelProperty(value = "代理id")
    private Integer agentId;

}
