package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferResp {
	private java.math.BigDecimal balancePlayableBonus;

	private java.math.BigDecimal balanceReal;

	private java.math.BigDecimal balanceReleasedBonus;

	private java.math.BigDecimal balanceSecondary;

	private java.math.BigDecimal balanceTotal;

	private java.math.BigDecimal balanceWithdrawable;

	private GetTransactionResp resp;

	public TransferResp() {
		this.resp = new GetTransactionResp();
	}
}