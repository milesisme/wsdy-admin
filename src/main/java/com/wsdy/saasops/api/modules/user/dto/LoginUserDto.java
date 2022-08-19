package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Transient;

@Getter
@Setter
@ApiModel(value = "会员登陆", description = "当密码错误次数达到3次以上必须传送验证码!")
public class LoginUserDto {
	@ApiParam("会员账号")
	private String loginName;
	@ApiParam("会员密码")
	private String password;
	@ApiParam("手机区号：86中国，886台湾")
	private String mobileAreaCode;
	@ApiParam("手机号")
	private String mobile;
	@ApiParam("手机验证码")
	private String mobileCaptcha;

	@ApiParam("图形验证码")
	private String captcha;
	@ApiParam(value = "图形验证码标志")
	private String codeSign;
	@ApiParam(value = "谷歌验证")
	private String responseToken;
	@ApiParam(value = "谷歌验证分数")
	private Double score;

	@ApiModelProperty(value="行为验证码")
	private String captchaVerification;

	@ApiParam(value = "外围系统前端标志 1表示外围系统前端登录")
	private String agentV2Sign;
	@ApiParam(value = "登录类型:0 PC、1 wap、2 移动端-IOS、3移动端-Android")
	private String loginType;

	@ApiParam(value = "登录设备号")
	private String loginDevice;

	@ApiParam(value = "注册设备号")
	@Transient
	private String registerDevice;

	@ApiModelProperty(value="注册域名")
	@Transient
	private String mainDomain;
}
