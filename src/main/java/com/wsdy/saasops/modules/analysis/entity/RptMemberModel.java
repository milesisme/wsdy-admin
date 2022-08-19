package com.wsdy.saasops.modules.analysis.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RptMemberModel {

    @ApiModelProperty("总存款")
    private BigDecimal deposits=BigDecimal.ZERO;
    @ApiModelProperty("总提款")
    private BigDecimal withdraws=BigDecimal.ZERO;

    @ApiModelProperty("总存款人数")
    private Integer depositCount = 0;
    @ApiModelProperty("总提款人数")
    private Integer withdrawCount  = 0 ;
    @ApiModelProperty("总存款订单数")
    private Integer depositOrderCount  = 0;
    @ApiModelProperty("总提款款订单数")
    private Integer withdrawOrderCount  = 0;


    @ApiModelProperty("新会员总提款人数")
    private Integer newWithdrawCount;

    @ApiModelProperty("新会员总存款订单数")
    private Integer newDepositOrderCount  = 0 ;
    @ApiModelProperty("新会员总提款订单数")
    private Integer newWithdrawOrderCount  = 0;
    @ApiModelProperty("新会员总存款")
    private BigDecimal newDepositAmount=BigDecimal.ZERO;
    @ApiModelProperty("新会员总提款")
    private BigDecimal newWithdrawAmount=BigDecimal.ZERO;

    @ApiModelProperty("统计日期")
    private String startday;
    @ApiModelProperty("新增会员")
    private Integer newMbrs;
    @ApiModelProperty("新增会员并存款")
    private Integer newDeposits;
    @ApiModelProperty("活跃会员")
    private Integer activeMbrs;
    @ApiModelProperty("总会员")
    private Integer totalMbrs;
    @ApiModelProperty("总派彩")
    private BigDecimal payouts=BigDecimal.ZERO;
    @ApiModelProperty("总有效投注")
    private BigDecimal validBets=BigDecimal.ZERO;
}