package com.wsdy.saasops.modules.analysis.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RptWinLostModel {

    private String startday;
    /***总代***/
    private String topAgent;
    private Integer topAgentId;
    /***代理***/
    private String agent;
    private Integer agentId;
    /***会员组***/
    private String mbrGroup;
    /***会员***/
    private String userName;
    private Integer userId;
    /***投注总人数***/
    private Integer betCounts=0;
    /***存款总人数***/
    private Integer depositCounts=0;
    /***存款总次数***/
    private Integer depositTimes=0;
    /***总存款***/
    private BigDecimal deposits=BigDecimal.ZERO;
    /***总提款***/
    private BigDecimal withdraws=BigDecimal.ZERO;
    /***提款总次数***/
    private Integer withdrawTimes=0;
    /***总盈亏***/
    private BigDecimal earnings=BigDecimal.ZERO;
    /***总红利***/
    private BigDecimal profits=BigDecimal.ZERO;
    /***红利次数***/
    private Integer profitTimes=0;
    /***红利数量***/
    private Integer profitCounts=0;
}
