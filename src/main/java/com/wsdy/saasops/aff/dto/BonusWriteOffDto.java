package com.wsdy.saasops.aff.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "优惠冲销数据",description = "优惠冲销数据")
public class BonusWriteOffDto {

	@ApiModelProperty(value = "会员名")
	private String membercode;

	@ApiModelProperty(value = "amount")
	private BigDecimal amount;

	@ApiModelProperty(value = "audittime")
	private String auditTime;

	@ApiModelProperty(value = "orderNo")
	private String orderNo;
}
