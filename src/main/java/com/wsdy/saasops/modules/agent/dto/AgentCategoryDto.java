package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentCategoryDto {


    @ApiModelProperty(value = "会员id")
    private Integer id;
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "总代id")
    private Integer tagencyId;
    @ApiModelProperty(value = "代理id")
    private Integer agyId;
    @ApiModelProperty(value = "代理名称")
    private String agyAccount;
    @ApiModelProperty(value = "部门id")
    private Integer departmentid;
    @ApiModelProperty(value = "上级代理id")
    private Integer parentId;




}