package com.wsdy.saasops.api.modules.user.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetTransactionResp {
    private BigDecimal amountPlayableBonus;

    private BigDecimal amountReal;

    private BigDecimal amountReleasedBonus;

    private BigDecimal balancePlayableBonus;

    private BigDecimal balanceReal;

    private BigDecimal balanceReleasedBonus;

    private String dateTime;

    private String gameId;

    private String gameTranId;

    private String platformCode;

    private String platformTranId;

    private String tranType;

    private String transactionId;
}