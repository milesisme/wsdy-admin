package com.wsdy.saasops.aff.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "创建会员",description = "创建会员")
public class CreateUserDto {

	@ApiModelProperty(value = "密码")
	private String pass;

	@ApiModelProperty(value = "会员名")
	private String membercode;

	@ApiModelProperty(value = "代理推广码")
	private String affiliatecode;

    @ApiModelProperty(value = "上级代理")
    private String superiorAgent;

	@ApiModelProperty(value = "域名")
	private String domain;

	@ApiModelProperty(value = "备注")
	private String remarks;

	@ApiModelProperty(value = "类别")
	private String category;

	private String siteCode;
}
