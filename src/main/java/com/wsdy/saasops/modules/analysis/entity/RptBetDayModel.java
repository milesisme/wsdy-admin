package com.wsdy.saasops.modules.analysis.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RptBetDayModel {

    private String startday;//统计日期
    private String username;//用户名
    private String platform;//游戏平台
    private String gametype;//游戏类型
    private String catName;//游戏分类
    private BigDecimal bet;
    private BigDecimal validbet;//有效投注
    private BigDecimal payout;//派彩
    private BigDecimal payoff;//彩金
    private BigDecimal payoffbet;//彩金下注
    private BigDecimal winnings;//彩金额
    private BigDecimal tip;//小费
    private BigDecimal deposit;//总存款
    private BigDecimal withdrawal;//总体款
    private BigDecimal rebate;//返水
    private Integer quantity;// 单量
    private String topagent;//总代理
    private Integer counts;//投注人数
    private String agent;//代理

    public RptBetDayModel() {
    }
}
