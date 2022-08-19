package com.wsdy.saasops.modules.analysis.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RptBetTotalModel {

    /***统计日期**/
    private String startday;
    /***游戏平台***/
    private String platform;
    private Integer platformId;
    /***游戏分类***/
    private String gameCategory;
    private Integer gameCategoryId;
    /***总代***/
    private String topAgentTimes;
    private String topAgent;
    private Integer topAgentId;
    /***代理***/
    private String agentTimes;
    private String agent;
    private Integer agentId;
    /***总投注人数***/
    private Integer times=0;
    /**总投注人数*/
    private Integer userCounts=0;
    private String userName;
    /***总投注额***/
    private BigDecimal betTotal=BigDecimal.ZERO;
    /***总有效投注额***/
    private BigDecimal validBetTotal=BigDecimal.ZERO;
    /***总派彩额***/
    private BigDecimal payoutTotal=BigDecimal.ZERO;
    /***赢利率***/
    private Float winRate;
    /***累积投注***/
    private BigDecimal jackpotBetTotal=BigDecimal.ZERO;
    /***累积派彩***/
    private BigDecimal jackpotPayoutTotal=BigDecimal.ZERO;
    /***累积赢利***/
    private BigDecimal jackpotWinTotal=BigDecimal.ZERO;

    private String minTime;
    private String maxTime;

    /**
     * 下级代理数
     */
    private Integer subCount;
    private Integer isSign;
}
