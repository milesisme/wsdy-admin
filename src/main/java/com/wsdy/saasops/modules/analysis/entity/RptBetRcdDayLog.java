package com.wsdy.saasops.modules.analysis.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class RptBetRcdDayLog implements Serializable {

    private static final long serialVersionUID=1L;

    private Integer id;

    private String startday;

    private String username;

    /**
     * 游戏平台
     */
    private String platform;

    /**
     * 游戏类型
     */
    private String gametype;

    private BigDecimal bet;

    private BigDecimal validbet;

    /**
     * 派彩金额
     */
    private BigDecimal payout;

    private BigDecimal jackpotbet;

    private BigDecimal jackpotpayout;

    private BigDecimal tip;

    private BigDecimal deposit;

    private BigDecimal withdrawal;

    private BigDecimal rebate;

    /**
     * 单量
     */
    private Integer quantity;

    private Integer devsource;

    /**
     * rpt类别：Sport Live Slot Hunter Lottery Chess Esport Tip Activity Unknown
     */
    private String gamecategory;

    private String createtime;

    private Long israte;

    private BigDecimal cost;

    private BigDecimal waterrate;
    
    private Integer pageNo;
    private Integer pageSize;
    
    private String startTime;
    private String endTime;
    private String updatetime;

}
