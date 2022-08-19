package com.wsdy.saasops.aff.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "修改会员代理",description = "修改会员代理")
public class AccountAgentDto {

	@ApiModelProperty(value = "会员名")
	private String membercode;

	@ApiModelProperty(value = "代理名")
	private String parentmembercode;

	private String siteCode;
}
