package com.wsdy.saasops.modules.analysis.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RptBetRcdDayDto {

    private Integer id;

    private String startday;

    private String username;

    private String platform;

    private String gametype;

    private BigDecimal bet;

    private BigDecimal validbet;

    private BigDecimal payout;

    private BigDecimal rate;

    private Integer israte;

    private String gamecategory;
}
