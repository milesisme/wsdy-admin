package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsDto {
	private String account;//用户名
	private String password;//密码
	private String mobile;//手机号码
	private String content;//内容
	//private String url;//短信网关
}
