package com.wsdy.saasops.aff.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "会员数据", description = "会员数据")
public class AccountListResponseDto {

    @ApiModelProperty(value = "会员名")
    private String membercode;

    @ApiModelProperty(value = "状态 1开启，0禁用,2余额冻结")
    private Integer status;

    @ApiModelProperty(value = "email")
    private String email;

    @ApiModelProperty(value = "真实姓名")
    private String fullname;

    @ApiModelProperty(value = "电话")
    private String contact;

    @ApiModelProperty(value = "registerip")
    private String registerip;

    @ApiModelProperty(value = "lastlogindate")
    private String lastlogindate;

    @ApiModelProperty(value = "加人时间")
    private String joineddate;

    @ApiModelProperty(value = "所属代理")
    private String affiliatemembercode;

    @ApiModelProperty(value = "countsamefp 暂无")
    private Integer countsamefp;

	@ApiModelProperty(value = "countsameip")
	private Integer countsameip;

    @ApiModelProperty(value = "登录时间")
	private String loginTime;

}
