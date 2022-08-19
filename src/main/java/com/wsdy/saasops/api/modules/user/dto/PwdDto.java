package com.wsdy.saasops.api.modules.user.dto;
import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;



/**
 * 用户
 */
@Setter
@Getter
@ApiModel(value = "会员登陆密码", description = "")
public class PwdDto implements Serializable {
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "会员旧密码!")
	private String lastPwd;
	@ApiModelProperty(value = "会员新密码!")
	private String pwd;
}