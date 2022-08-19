package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="会员注册")
public class PtNewPlayerDataDto {
	@ApiModelProperty(value="会员账号 32位")
	private String code;
	@ApiModelProperty(value="会员名称名 32")
	private String firstName;
	@ApiModelProperty(value="会员名称姓 32")
	private String lastName;
	@ApiModelProperty(value="会员邮箱 64")
	private String email;
	@ApiModelProperty(value="国家 2")
	private String country;
	@ApiModelProperty(value="币种 3")
	private String currency;
	@ApiModelProperty(value="是否为测试账号")
	private boolean isTest;
	@ApiModelProperty(value="语系 2")
	private String language;
	@ApiModelProperty(value="状态 16")
	private String status;
	@ApiModelProperty(value="会员密码 64")
	private String password;
	@ApiModelProperty(value="会员组 32")
	private String gameGroup;
}
