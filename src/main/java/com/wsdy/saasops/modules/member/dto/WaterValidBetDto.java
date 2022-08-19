package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "会员列表中的 ,会员余额列表")
public class WaterValidBetDto {

	@ApiModelProperty(value = "平台code")
	private String depotCode;

	@ApiModelProperty(value = "有效投注")
	private BigDecimal validBet;
}
