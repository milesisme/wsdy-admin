package com.wsdy.saasops.api.modules.user.dto;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "会员取款申请JSON", description = "会员取款申请JSON")
public class MbrWdApplyDto {
	
	@ApiModelProperty(value = "会员提款银行卡信息(bankcardId)")
	private Integer bankCardId;

	@ApiModelProperty(value = "会员提款加密货币钱包信息(MbrCryptoCurrencies id)")
	private Integer cryptoCurrenciesId;

	@ApiModelProperty(value = "提款金额")
	private BigDecimal drawingAmount;

	@ApiModelProperty(value = "参考汇率")
	private BigDecimal exchangeRate;

	@ApiModelProperty(value = "提现方式：null/0银行卡， 1加密货币钱包，2支付宝")
	private Integer methodType;

	@ApiModelProperty(value = "会员提款密码，必填")
	private String pwd;
}
