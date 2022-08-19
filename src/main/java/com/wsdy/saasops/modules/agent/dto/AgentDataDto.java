package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentDataDto {


    @ApiModelProperty(value = "会员id")
    private Integer id;
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "存款金额")
    private BigDecimal depositAmount;






}