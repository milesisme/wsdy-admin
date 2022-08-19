package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "会员修改密码", description = "会员修改密码!")
public class ModPwdDto {
	@ApiModelProperty(value = "验证码,仅为数字")
	private String code;
	@ApiModelProperty(value = "密码!")
	private String password;
	@ApiModelProperty(value = "用户名")
	private String loginName;
}
