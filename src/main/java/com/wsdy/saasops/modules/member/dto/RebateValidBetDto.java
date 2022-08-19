package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RebateValidBetDto {

	@ApiModelProperty(value = "id")
	private String id;

	@ApiModelProperty(value = "gameCategory")
	private String gameCategory;

	@ApiModelProperty(value = "有效投注")
	private BigDecimal validBet;
}
