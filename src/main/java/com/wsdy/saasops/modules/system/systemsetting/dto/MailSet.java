package com.wsdy.saasops.modules.system.systemsetting.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MailSet {

	// 邮件发送服务器
	private String mailSendServer;
	//邮件发送端口
	private String mailSendPort;
	//邮件发送账号
	private String mailSendAccount;
	//账号密码
	private String mailPassword;
	//是否使用SSL
	private String wetherSsl;
	//字符集
	private String characterSet;
	//邮件接收者
	private String mailReceiver;
}
