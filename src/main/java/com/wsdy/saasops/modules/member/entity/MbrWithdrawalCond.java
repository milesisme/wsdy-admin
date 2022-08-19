package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "MbrWithdrawalCond", description = "")
@Table(name = "mbr_withdrawal_cond")
public class MbrWithdrawalCond implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;
	@ApiModelProperty(value = "会员组ID")
	private Integer groupId;

	// 单笔取款限制
	@ApiModelProperty(value = "最低限额CNY")
	private BigDecimal lowQuota;
	@ApiModelProperty(value = "最高限额CNY")
	private BigDecimal topQuota;

	@ApiModelProperty(value = "最低限额支付宝CNY")
	private BigDecimal lowAlipayQuota;
	@ApiModelProperty(value = "最高限额支付宝CNY")
	private BigDecimal topAlipayQuota;

	@ApiModelProperty(value = "最低限额USDT")
	private BigDecimal lowUsdt;
	@ApiModelProperty(value = "最高限额USDT")
	private BigDecimal topUsdt;

	// 手续费设置
	@ApiModelProperty(value = "是否收取手续费开关 1 收取 0不收取 ")
	private Byte chargeFeeAvailable;
	@ApiModelProperty(value = "手续费-时限 1：自然日，2：自然周，3：自然月")
	private Integer feeHours;
	@ApiModelProperty(value = "手续费-限免次数")
	private Integer feeTimes;
	@ApiModelProperty(value = "手续上限金额CNY")
	private BigDecimal feeTop;
	@ApiModelProperty(value = "手续费 比例")
	private BigDecimal feeScale;
	@ApiModelProperty(value = "手续费固定收费费用")
	private BigDecimal feeFixed;
	@ApiModelProperty(value = "收取手续费方式(按比例0、固定1收费)")
	private Byte feeWay;

	@ApiModelProperty(value = "极速取款保证金收取比例")
	private BigDecimal fastWithdrawFeePercent;
	@ApiModelProperty(value = "极速取款保证金收取固定金额")
	private BigDecimal fastWithdrawFeeAmount;

	// 每日取款限额:废弃， 使用等级的每日取款限额
    @ApiModelProperty(value = "取款限制")
    private Byte feeAvailable;
	@ApiModelProperty(value = "每日充许取款次数")
	private Integer withDrawalTimes;
	@ApiModelProperty(value = "每日取款限额")
	private BigDecimal withDrawalQuota;

	// 取款稽核设置
	@ApiModelProperty(value = "存款稽核倍数")
	private Integer withDrawalAudit;
	@ApiModelProperty(value = "放宽额度CNY ")
	private BigDecimal overFee;

	@ApiModelProperty(value = "管理费、行政费")
	private Integer manageFee;
	
	@ApiModelProperty(value = "返利钱包最低提现额度")
	private BigDecimal rebateMinimum;

	public interface FeeWayVal
	{
		byte scale=0;//比率
		byte fixed=1;//固定
	}

	@Transient
	@ApiModelProperty(value = "会员当日已取款次数")
	private Integer withDrawalTimesMbr;
	@Transient
	@ApiModelProperty(value = "会员当日已取款限额")
	private BigDecimal withDrawalQuotaMbr;
	
	@Transient
	@ApiModelProperty(value = "是否展示手续费金额")
	private Boolean isShowFee;
}