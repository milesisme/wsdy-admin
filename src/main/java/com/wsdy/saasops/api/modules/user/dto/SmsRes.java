package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsRes {
	private String code;// 是否成功 2为成功
	private String msg; // 反回文字信息
	private String smsid;// 成功反回消息Id

	// 启瑞云
	private boolean success;	// 是否成功 true成功，false失败
	private String id;		// 短信平台id
	private String r;		// 短信平台错误码

	// 网梦云
	private String result;	// 是否成功 0成功 其他异常
	private String msgid;		// 短信平台id
	private String custid;		// 用户自定义流水编号
}
