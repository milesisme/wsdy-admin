package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class AgentListDto {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "代理账户")
    private String agyAccount;

    @ApiModelProperty(value = "上级帐号")
    private String agyTopAccount;

    @ApiModelProperty(value = "直属会员数 注册会员数")
    public Integer accountNum;

    @ApiModelProperty(value = "总有效会员数")
    public Integer validAccountNum;

    @ApiModelProperty(value = "佣金余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "注册时间")
    private String registerTime;

    @ApiModelProperty(value = "1开启，0禁用")
    private Integer available;

    @ApiModelProperty(value = "0 拒绝，1 成功 2 待处理")
    private Integer status;
}
