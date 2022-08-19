package com.wsdy.saasops.agapi.modulesV2.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;

@Setter
@Getter
@ApiModel(value = "LogAgyLogin", description = "代理登入日志")
@Table(name = "log_agylogin")
public class LogAgyLogin implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "代理Id")
	private Integer accountId;

	@ApiModelProperty(value = "代理名")
	private String loginName;

	@ApiModelProperty(value = "本次登陆时间")
	private String loginTime;

	@ApiModelProperty(value = "本次登陆IP")
	private String loginIp;

	@ApiModelProperty(value = "本次区域")
	private String loginArea;

	@ApiModelProperty(value = "本次登入线路")
	private String loginUrl;

	@ApiModelProperty(value = "登出时间")
	private String logoutTime;

	@ApiModelProperty(value = "在线时长 以分钟计算")
	private BigInteger onlineTime;

	// 在线时长字符串
	@Transient
	private String onlineTimeStr;
}