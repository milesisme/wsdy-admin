package com.wsdy.saasops.modules.member.entity;

import java.io.Serializable;

import java.math.BigDecimal;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "MbrDepositCond", description = "")
@Table(name = "mbr_deposit_cond")
public class MbrDepositCond implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "会员组ID")
	private Integer groupId;

	@ApiModelProperty(value = "公司入款 单笔-最低限额")
	private BigDecimal lowQuota;

	@ApiModelProperty(value = "公司入款 单笔-最高限配")
	private BigDecimal topQuota;

	@ApiModelProperty(value = "公司入款 返还手续费(1,还返，0关闭)")
	private Byte feeEnable;

	@ApiModelProperty(value = "线上入款 收取手续费开关")
	private Byte feeAvailable;

	@ApiModelProperty(value = "线上入款 手续费 时限 1：自然日，2：自然周，3：自然月")
	private Integer feeHours;

	@ApiModelProperty(value = "线上入款 手续费 - 限免次数")
	private Integer feeTimes;

	@ApiModelProperty(value = "线上入款 手续费上限金额CNY")
	private BigDecimal feeTop;

	@ApiModelProperty(value = "线上入款 手续费 按比例收费")
	private BigDecimal feeScale;

	@ApiModelProperty(value = "线上入款 固定收费")
	private BigDecimal feeFixed;

	@ApiModelProperty(value = "线上入款 收费的方式(0-按比例收费 1固定收费)")
	private Byte feeWay;

	@ApiModelProperty(value = "是否开启存款姓名")
	private Integer depositName;
}