package com.wsdy.saasops.modules.member.entity;

import java.io.Serializable;


import java.math.BigDecimal;


import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "MbrBillDetail", description = "")
@Table(name = "mbr_bill_detail")
public class MbrBillDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "产生交易记录order")
	private String orderNo;
	
	@ApiModelProperty(value = "产生交易记录orders")
	@Transient
	private String[] orderNos;

	@ApiModelProperty(value = "会员登陆名称")
	private String loginName;

	@ApiModelProperty(value = "会员ID")
	private Integer accountId;

    @ApiModelProperty(value = "会员IDs")
    @Transient
    private Integer[] accountIds;
    
    @ApiModelProperty(value = "会员名称组合")
    @Transient
    private String[] loginNames;
    
	@ApiModelProperty(value = "财务类别代码")
	private String financialCode;

	@ApiModelProperty(value = "操作金额")
	private BigDecimal amount;

	@ApiModelProperty(value = "操作后余额")
	private BigDecimal afterBalance;

	@ApiModelProperty(value = "操作前的余额")
	private BigDecimal beforeBalance;

	@ApiModelProperty(value = "操作类型，0 支出1 收入")
	private Byte opType;

	@ApiModelProperty(value = "生成订单时间")
	private String orderTime;

	@ApiModelProperty(value = "")
	private String memo;

	@ApiModelProperty(value = "平台id")
	private Integer depotId;

	@ApiModelProperty(value = "转出上级代理or接收人代理id")
	private Integer agentId;

	@ApiModelProperty(value = "创建人")
	private String createuser;

	// 订单号前缀
	@Transient
	private String orderPrefix;

	@Transient
	@ApiModelProperty(value = "优惠类型")
	private String codeName;

	@Transient
	@ApiModelProperty(value = "平台操作后余额")
	private BigDecimal depotAfterBalance;

	@Transient
	@ApiModelProperty(value = "平台操作前余额")
	private BigDecimal depotBeforeBalance;

	@Transient
	@ApiModelProperty(value = "financialCodeName")
	private String financialCodeName;

	public interface OpTypeStatus {
		byte expenditure = 0;// 支出
		byte income = 1;// 收入
	}

}