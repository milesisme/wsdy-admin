package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class SubAgentListDto {

    @ApiModelProperty(value = "代理id")
    private Integer id;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "下级代理")
    private Integer subAgentNum;

    @ApiModelProperty(value = "直属会员")
    private Integer accountNum;

    @ApiModelProperty(value = "活跃会员")
    private Integer activeAccountNum;

    @ApiModelProperty(value = "投注金额(有效投注)")
    private BigDecimal validBet;

    @ApiModelProperty(value = "净盈利 更改总输赢")
    private BigDecimal totalProfit;

    @ApiModelProperty(value = "总输赢")
    private BigDecimal payout;
    @ApiModelProperty(value = "总红利")
    private BigDecimal totalBonusAmount;


    @ApiModelProperty(value = "注册时间")
    private String registerTime;
    
    @ApiModelProperty(value = "佣金上限")
    private BigDecimal commissioncap;
    
    @ApiModelProperty(value = "首存人数：时间范围内首存人数")
    private BigDecimal totalNewDeposits = BigDecimal.ZERO;
    
    @ApiModelProperty(value = "首存金额")
    private BigDecimal totalNewDepositAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "净输赢")
    private BigDecimal netwinlose = BigDecimal.ZERO;

    @ApiModelProperty(value = "结算模式")
    private Integer feeModel;
    
    @ApiModelProperty(value = "额外平台费")
    private BigDecimal additionalServicerate = BigDecimal.ZERO;

    @ApiModelProperty(value = "服务费")
    private BigDecimal serviceCost = BigDecimal.ZERO;

    @ApiModelProperty(value = "平台费")
    private BigDecimal cost = BigDecimal.ZERO;

    @ApiModelProperty(value = "存提差")
    private BigDecimal ctDiffer = BigDecimal.ZERO;
    @ApiModelProperty(value = "存款")
    private BigDecimal depositBalance = BigDecimal.ZERO;
    @ApiModelProperty(value = "提款")
    private BigDecimal drawAmount = BigDecimal.ZERO;
    @ApiModelProperty(value = "上月佣金")
    private BigDecimal commission = BigDecimal.ZERO;
    @ApiModelProperty(value = "总投注")
    private BigDecimal betTotal;
    
    @ApiModelProperty(value = "人工扣减")
    private BigDecimal auditAmAmount;
    
    @ApiModelProperty(value = "人工调整金额")
    private BigDecimal calculateProfit;
}
