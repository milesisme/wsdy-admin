package com.wsdy.saasops.modules.analysis.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: Miracle
 * @Description:
 * @Date: 14:05 2017/12/7
 **/
@Data
public class FundReportModel {

    private BigDecimal payout;
    private BigDecimal payoutPercent;
    private BigDecimal memberWithdraw;
    private BigDecimal memberWithdrawPercent;
    private BigDecimal agyWithdraw;
    private BigDecimal agyWithdrawPercent;
    private BigDecimal discount;
    private BigDecimal discountPercent;
    private BigDecimal commission;
    private BigDecimal commissionPercent;
    private BigDecimal profit;
    private BigDecimal profitPercent;
}
