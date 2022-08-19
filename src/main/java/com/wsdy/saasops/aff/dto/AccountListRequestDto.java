package com.wsdy.saasops.aff.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "会员数据", description = "会员数据")
public class AccountListRequestDto {

    @ApiModelProperty(value = "代理名")
    private String agentAccount;

    @ApiModelProperty(value = "注册开始时间")
    private String registerStartTime;

    @ApiModelProperty(value = "registerEndTime")
    private String registerEndTime;

    private String siteCode;

    @ApiModelProperty(value = "会员修改开始时间")
    private String updatedStartTime;

    @ApiModelProperty(value = "会员修改结束时间")
    private String updatedEndTime;


    @ApiModelProperty(value = "登录开始")
    private String loginStartTime;

    @ApiModelProperty(value = "登录结束")
    private String loginEndTime;
}
