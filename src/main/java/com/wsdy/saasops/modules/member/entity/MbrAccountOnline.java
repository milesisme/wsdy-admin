package com.wsdy.saasops.modules.member.entity;

import java.math.BigDecimal;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.Table;
import javax.persistence.Transient;

import com.wsdy.saasops.modules.base.entity.BaseAuth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "MbrAccount", description = "")
@Table(name = "mbr_account")
public class MbrAccountOnline {

	@ApiModelProperty(value = "会员Id")
	private Integer id;

	@ApiModelProperty(value = "会员名称")
	private String loginName;

	@ApiModelProperty(value = "会员组Id")
	private Integer groupId;

	@ApiModelProperty(value = "会员组名称")
	private String groupName;

	@ApiModelProperty(value = "代理账号")
	private String agyAccount;

	@ApiModelProperty(value = "总金额")
	private BigDecimal totalBalance;

	@ApiModelProperty(value = "本次登陆时间")
	private String loginTime;

	@ApiModelProperty(value = "登陆时间2")
	private String loginTimeEnd;

	@ApiModelProperty(value = "本次登陆IP")
	private String loginIp;

	@ApiModelProperty(value = "本次区域")
	private String loginArea;

	@ApiModelProperty(value = "本次登入线路")
	private String loginUrl;

	@ApiModelProperty(value = "在线时长 以分钟计算")
	private BigInteger onlineTime;

	@ApiModelProperty(value = "选择在线时长 以分钟计算")
	private BigInteger selectOnlineTime;

	@ApiModelProperty(value = "选择在线时长 以分钟计算")
	private String selectOnlineTimeStr;

	@ApiModelProperty(value = "在线时长 转成字符串")
	private String onlineTimeStr;

	@ApiModelProperty(value = "是否在线")
	private Byte isOnline;

	@ApiModelProperty(value = "登陆来源 0pc 3h5")
	private String loginSourceList;
	
	@Transient
	private BaseAuth baseAuth;

	@Transient
	@ApiModelProperty(value = "是否在线")
	private Integer tagencyId;

	@Transient
	@ApiModelProperty(value = "是否在线")
	private Integer cagencyId;

	@Transient
	@ApiModelProperty(value = "总代查询")
	private String tagencyIdList;

	@Transient
	@ApiModelProperty(value = "代理查询")
	private String cagencyIdList;

	@Transient
	@ApiModelProperty(value = "分组查询")
	private List<Integer> groupIdList;

	@Transient
	@ApiModelProperty(value = "开始时间")
	private String startTime;

	@Transient
	@ApiModelProperty(value = "结束时间")
	private String endTime;
}
