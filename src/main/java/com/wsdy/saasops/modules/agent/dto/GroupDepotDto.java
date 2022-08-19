package com.wsdy.saasops.modules.agent.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GroupDepotDto {

    private String platform;

    private Integer depotId;

    private BigDecimal payout;

    private Integer type;

    private String gamecategory;

    private BigDecimal waterCost;

    private BigDecimal validbet;

    private BigDecimal waterrate;

}