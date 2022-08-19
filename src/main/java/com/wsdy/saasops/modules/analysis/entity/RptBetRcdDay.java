package com.wsdy.saasops.modules.analysis.entity;

import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class RptBetRcdDay {
    private Integer id;
    private String startday;
    private String username;
    private String platform;
    private String gametype;
    private BigDecimal bet;
    private BigDecimal validbet;
    private BigDecimal payout;
    private BigDecimal jackpotbet;
    private BigDecimal jackpotpayout;
    private BigDecimal tip;
    private BigDecimal deposit;
    private BigDecimal withdrawal;
    private BigDecimal rebate;
    private Integer quantity;
    private Integer devsource;
    private String gamecategory;
    private Date createtime;
    private Integer israte;
    private BigDecimal cost;
    private BigDecimal waterrate;
}
