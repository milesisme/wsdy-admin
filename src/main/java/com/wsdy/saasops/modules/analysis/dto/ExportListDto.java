package com.wsdy.saasops.modules.analysis.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ExportListDto {

    @Excel(name = "日期", width = 20, orderNum = "1")
    private String createTime;   //时间

    @Excel(name = "净盈利", width = 20, orderNum = "2")
    private BigDecimal totalProfit;    //净盈利
    @Excel(name = "总存款", width = 20, orderNum = "3")
    private BigDecimal totalDepositBalance;   //总存款


    @Excel(name = "好友转账出款", width = 20, orderNum = "4")
    private BigDecimal friendsTransAmountTotal;//好友转出
    @Excel(name = "好友转账入款", width = 20, orderNum = "5")
    private BigDecimal friendsRecepitAmountTotal;//好友

    @Excel(name = "资金调整", width = 20, orderNum = "6")
    private BigDecimal fundAdjust;     //资金调整

    @Excel(name = "总提款", width = 20, orderNum = "7")
    private BigDecimal totalDrawAmount;    //总提款

    @Excel(name = "线上红利", width = 20, orderNum = "8")
    private BigDecimal totalBonusAmountOnline;

    @Excel(name = "线下红利", width = 20, orderNum = "9")
    private BigDecimal totalBonusAmountOffline;

    @Excel(name = "好友返利", width = 20, orderNum = "10")
    private BigDecimal totalActualReward; // 好友返利

    @Excel(name = "总红利", width = 20, orderNum = "11")
    private BigDecimal totalBonusAmount;    //总红利

    @Excel(name = "总返利", width = 20, orderNum = "12")
    private BigDecimal totalRebate;     //总返点

    @Excel(name = "总赢亏", width = 20, orderNum = "13")
    private BigDecimal totalPayout;      //总派彩

    @Excel(name = "总任务返利", width = 20, orderNum = "14")
    private BigDecimal totalTaskBonus;  // 总任务返利

    @Excel(name = "奖池派彩", width = 20, orderNum = "15")
    private BigDecimal totalJackpotPayout;    //奖池彩金
}
