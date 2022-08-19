package com.wsdy.saasops.modules.analysis.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
@Getter
public class ExportAgentListDto {

    @Excel(name = "日期", width = 20, orderNum = "1")
    private String time;

    @Excel(name = "净盈利", width = 20, orderNum = "2")
    private BigDecimal totalProfit;

    @Excel(name = "资金调整", width = 20, orderNum = "3")
    private BigDecimal fundAdjust;

    @Excel(name = "下注人数", width = 20, orderNum = "4")
    private Integer betNum;

    @Excel(name = "有效活跃", width = 20, orderNum = "5")
    private Integer  activeNum;

    @Excel(name = "注册人数", width = 20, orderNum = "6")
    private Integer registerNum;

    @Excel(name = "首存人数", width = 20, orderNum = "7")
    private Integer firstDepositNum;

    @Excel(name = "存款人数", width = 20, orderNum = "8")
    private BigDecimal totalDepositNum;

    @Excel(name = "存款金额", width = 20, orderNum = "9")
    private BigDecimal totalDepositBalance;

    @Excel(name = "提款人数", width = 20, orderNum = "10")
    private BigDecimal totalDrawNum;

    @Excel(name = "提款金额", width = 20, orderNum = "11")
    private BigDecimal totalDrawAmount;

    @Excel(name = "线上优惠", width = 20, orderNum = "12")
    private BigDecimal totalBonusAmountOnline;

    @Excel(name = "线下优惠", width = 20, orderNum = "13")
    private BigDecimal  totalBonusAmountOffline;

    @Excel(name = "总优惠", width = 20, orderNum = "14")
    private BigDecimal  totalBonusAmount;

    @Excel(name = "总盈亏", width = 20, orderNum = "15")
    private BigDecimal totalPayout;
    @Excel(name = "任务返利", width = 20, orderNum = "16")
    private BigDecimal totalTaskBonus;

    @Excel(name = "平均存款金额", width = 20, orderNum = "17")
    private BigDecimal averageDeposit;


}
