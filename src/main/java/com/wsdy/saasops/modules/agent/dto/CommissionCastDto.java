package com.wsdy.saasops.modules.agent.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommissionCastDto {

    private BigDecimal validbet;

    private BigDecimal totalPayout;

    private BigDecimal bonusamount;

    private BigDecimal taskBonusamount;

    private Integer userCount;

    private BigDecimal waterBigDecimal;
    
    /**	
     * 	人工调整金额
     */
    private BigDecimal calculateProfit;
}