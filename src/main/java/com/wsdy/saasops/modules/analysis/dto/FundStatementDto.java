package com.wsdy.saasops.modules.analysis.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class FundStatementDto {

    private String time;

    private BigDecimal totalProfit;

    private BigDecimal fundAdjust;

    private BigDecimal totalDepositBalance;

    private BigDecimal totalDepositNum;

    private BigDecimal totalDrawAmount;

    private BigDecimal totalDrawNum;

    private BigDecimal totalBonusAmountOnline;

    private BigDecimal  totalBonusAmountOffline;

    private BigDecimal totalPayout;

    private BigDecimal totalTaskBonus;

    private BigDecimal  totalBonusAmount;

    private Integer registerNum;

    private Integer firstDepositNum;

    private Integer betNum;

    private Integer activeNum;

    private BigDecimal averageDeposit;



}
