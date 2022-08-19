package com.wsdy.saasops.api.modules.user.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetBalanceResp{
	
    private BigDecimal playableBonus;
    private BigDecimal real;
    private BigDecimal releasedBonus;
    private BigDecimal secondaryBalance;
    private BigDecimal total;
    private BigDecimal withdrawable;
    private String currency;
    private String ticket;
    private String transactionId;
}