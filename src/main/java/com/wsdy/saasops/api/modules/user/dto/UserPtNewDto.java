package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "登陆第三方参数")
public class UserPtNewDto {
	@ApiModelProperty(value = "代理账号登陆安全码")
	private String secretKey;
	@ApiModelProperty(value = "代理账号")
	private String username;
	@ApiModelProperty(value = "代理账号密码")
	private String password;
}
