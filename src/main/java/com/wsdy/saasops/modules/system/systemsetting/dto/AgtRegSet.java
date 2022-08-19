package com.wsdy.saasops.modules.system.systemsetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

//代理注册设置
@Data
@Setter
@Getter
public class AgtRegSet {
	@ApiModelProperty("代理账号")
	private Integer agentAccount;
	@ApiModelProperty("代理登录密码")
	private Integer agentLoginPassword;
	@ApiModelProperty("代理重复密码")
	private Integer agentRepeatedPassword;
	@ApiModelProperty("代理验证码")
	private Integer agentCaptchareg;
	@ApiModelProperty("代理真实姓名")
	private Integer agentRealName;
	@ApiModelProperty("代理手机")
	private Integer agentMobile;
	@ApiModelProperty("代理邮箱")
	private Integer agentEmail;
	@ApiModelProperty("代理qq")
	private Integer agentQQ;
	@ApiModelProperty("代理微信")
	private Integer agentWechat;
	@ApiModelProperty("代理地址")
	private Integer agentAddress;
}
