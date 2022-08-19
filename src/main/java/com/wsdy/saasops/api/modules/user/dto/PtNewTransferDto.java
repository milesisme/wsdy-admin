package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(value = "新PT转账")
@Getter
@Setter
public class PtNewTransferDto {
	@ApiModelProperty(value = "会员账号")
	private String playerCode;
	@ApiModelProperty(value = "当前币种")
	private String currency;
	@ApiModelProperty(value = "转账金额")
	private Double amount;
	@ApiModelProperty(value = "转账订单号")
	private String extTrxId;
}
