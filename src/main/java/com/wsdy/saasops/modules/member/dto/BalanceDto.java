package com.wsdy.saasops.modules.member.dto;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "会员列表中的 ,会员余额列表")
public class BalanceDto {
	@ApiModelProperty(value = "平台名称")
	private String depotName;
	@ApiModelProperty(value = "会员余额")
	private BigDecimal balance;
}
