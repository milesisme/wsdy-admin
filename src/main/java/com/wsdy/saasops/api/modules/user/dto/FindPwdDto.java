package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "会员找回密码", description = "会员找回密码-选择找回密码方式!")
public class FindPwdDto {
	@ApiModelProperty(value = "会员账号,长度为6~10位!")
	private String userName;
	@ApiModelProperty(value = "验证码!")
	private String captcha;
	@ApiModelProperty(value = "验证码标志")
	private String codeSign;
	@ApiModelProperty(value = "验证码标志")
	private String codeFlag;

	@ApiModelProperty(value = "手机号")
	private String mobile;
	@ApiModelProperty(value = "找回密码：验证码")
	private String kaptcha;
	@ApiModelProperty(value = "区号")
	private String mobileAreaCode;

	@ApiModelProperty(value="行为验证码")
	private String captchaVerification;
}
