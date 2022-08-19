package com.wsdy.saasops.aff.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@ApiModel(value = "会员投注",description = "会员投注")
public class AccountBetResponseDto {

	@ApiModelProperty(value = "会员名")
	private String membercode;

	@ApiModelProperty(value = "代理推广码")
	private String affiliatecode;

	@ApiModelProperty(value = "联系号码")
	private String phoneNumber;

	@ApiModelProperty(value = "注册日期")
	private String registerDate;

	@ApiModelProperty(value = "总存款")
	private BigDecimal totaldp;

	@ApiModelProperty(value = "总提款")
	private BigDecimal totalwd;

	@ApiModelProperty(value = "总投注")
	private BigDecimal totalTurnover;

	@ApiModelProperty(value = "最后存款时间")
	private String lastDepositDate;

	@ApiModelProperty(value = "最后提款时间")
	private String lastWdDate;

	@ApiModelProperty(value = "最后投注时间")
	private String lastBetDate;
}
