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
@ApiModel(value = "邮箱或手机验证DTO", description = "")
public class VfyMailOrMobDto implements Serializable {
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "会员邮箱!")
	private String email;
	@ApiModelProperty(value="手机区号：86中国，886台湾")
	private String mobileAreaCode;
	@ApiModelProperty(value = "会员手机号!")
	private String mobile;
	@ApiModelProperty(value = "验证code!")
	private String code;

	@ApiModelProperty(value = "验证码标志")
	private String codeSign;
	@ApiModelProperty(value="图形验证码 可选")
	private String kaptcha;

	@ApiModelProperty(value="行为验证码 可选")
	private String captchaVerification;
	@ApiModelProperty(value = "登录名")
	private String loginName;
}