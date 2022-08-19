package com.wsdy.saasops.modules.member.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.wsdy.saasops.modules.base.entity.BaseAuth;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "MbrBillManage", description = "")
@Table(name = "mbr_bill_manage")
public class MbrBillManage implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "产生交易记录order")
	private String orderNo;

	@ApiModelProperty(value = "会员登陆名称")
	private String loginName;

	@ApiModelProperty(value = "会员ID")
	private Integer accountId;

	@ApiModelProperty(value = "操作金额")
	private BigDecimal amount;

	@ApiModelProperty(value = "操作后余额")
	private BigDecimal afterBalance;

	@ApiModelProperty(value = "操作前余额")
	private BigDecimal beforeBalance;

	@ApiModelProperty(value = "操作类型，0 支出1 收入")
	private Integer opType;

	@ApiModelProperty(value = "平台ID")
	private Integer depotId;

	@ApiModelProperty(value = "0冻结,1成功,2失败")
	private Integer status;

	@ApiModelProperty(value = "createTime")
	private String createTime;

	@ApiModelProperty(value = "modifyTime")
	private String modifyTime;

	@ApiModelProperty(value = "modifyUser")
	private String modifyUser;

	@ApiModelProperty(value = "memo")
	private String memo;

	@ApiModelProperty(value = "logId")
	private Integer logId;
	
    @ApiModelProperty(value = "转账的登陆端来源")
    private Byte transferSource;

	@ApiModelProperty(value = "红利id")
	private Integer bonusId;

	@ApiModelProperty(value = "分类id")
	private Integer catId;

	@ApiModelProperty(value = "操作人")
	private String username;

	@ApiModelProperty(value = "操作时间")
	private String operatingTime;

	@Transient
	@ApiModelProperty(value = "代理账号")
	private String agyAccount;

	@Transient
	@ApiModelProperty(value = "上级代理")
	private String topAgyAccount;

	@Transient
	@ApiModelProperty(value = "总代理id")
	private Integer tagencyId;

	@Transient
	@ApiModelProperty(value = "代理id")
	private Integer cagencyId;

	@Transient
	@ApiModelProperty(value = "真实姓名")
	private String realName;

	@Transient
	@ApiModelProperty(value = "游戏平台名字")
	private String depotName;

	@Transient
	@ApiModelProperty(value = "申请时间开始")
	private String createTimeFrom;

	@Transient
	@ApiModelProperty(value = "申请时间结束")
	private String createTimeTo;

	@Transient
	@ApiModelProperty(value = "订单是否在一个时间段内 1为现在时间已超过 订单下单时间加某一个时间，0未超过")
	private Integer isTimeOut;

	@Transient
	@ApiModelProperty(value = "ip")
	private String ip;
	@Transient
	private BaseAuth baseAuth;

	@Transient
	@ApiModelProperty(value = "登录用户的名字")
	private String loginSysUserName;

	@ApiModelProperty(value = "平台操作后余额")
	private BigDecimal depotAfterBalance;

	@ApiModelProperty(value = "平台操作前余额")
	private BigDecimal depotBeforeBalance;

	@Transient
	@ApiModelProperty(value = "总代iD 查询使用")
	private List<Integer> tagencyIds;

	@Transient
	@ApiModelProperty(value = "直属代理iD 查询使用")
	private List<Integer> cagencyIds;

	@Transient
	@ApiModelProperty(value = "游戏平台 查询使用")
	private List<Integer> depotIds;

	@Transient
	@ApiModelProperty(value = "转账类型 查询使用")
	private List<Integer> opTypes;

	@Transient
	@ApiModelProperty(value = "状态 查询使用")
	private List<Integer> statuss;

	@Transient
	@ApiModelProperty(value = "登陆来源：0 PC，3 H5")
	private String transferSourceList;
}