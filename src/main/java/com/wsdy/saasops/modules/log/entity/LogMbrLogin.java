package com.wsdy.saasops.modules.log.entity;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "LogMbrlogin", description = "")
@Table(name = "log_mbrlogin")
public class LogMbrLogin implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "会员Id")
	private Integer accountId;

	@ApiModelProperty(value = "会员名")
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

	@ApiModelProperty(value = "IP风险提示 0低风险 1高风险 2中风险")
	private String checkip;

	@ApiModelProperty(value = "登录类型:0 PC、1 wap、2 移动端-IOS、3移动端-Android")
	private String loginType;

	// 在线时长字符串
	@Transient
	private String onlineTimeStr;

	@ApiModelProperty(value = "设备唯一标识")
	private String deviceUuid;
}