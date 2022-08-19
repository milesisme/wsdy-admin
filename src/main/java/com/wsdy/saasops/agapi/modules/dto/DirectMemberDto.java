package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Setter
@Getter
public class DirectMemberDto {

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "等级名称")
    private String tierName;

    @ApiModelProperty(value = "存款")
    private BigDecimal depositAmount;

    @ApiModelProperty(value = "取款")
    private BigDecimal withdrawAmount;

    @ApiModelProperty(value = "净输赢 净输赢= 总派彩 - 总红利 - 任务返利 - 总返利")
    private BigDecimal totalProfit;

    @ApiModelProperty(value = "总输赢")
    private BigDecimal payout;

    @ApiModelProperty(value = "真实名称")
    private String realName;

    @ApiModelProperty(value = "联系电话号码")
    private String mobile;

    @ApiModelProperty(value = "最后登录时间")
    private String loginTime;

    @ApiModelProperty(value = "注册时间")
    private String registerTime;

    @ApiModelProperty(value = "编号")
    private String numbering;

    @ApiModelProperty(value = "备注类型  1大客户 2套利客户 3潜力客户 4一般客户 5体育会员 6电竞会员")
    private Integer memoType;

    @ApiModelProperty(value = "红利")
    private BigDecimal bonusAmount;
    
    @ApiModelProperty(value = "所有红利")
    private BigDecimal bonusamountAll;

    @ApiModelProperty(value = "总投注(有效投注)")
    private BigDecimal validBet;
    
    @ApiModelProperty(value = "账户余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "存提差")
    private BigDecimal ctDiffer = BigDecimal.ZERO;

    @ApiModelProperty(value = "总投注")
    private BigDecimal betTotal;
    
    @ApiModelProperty(value = "人工扣减金额")
    private BigDecimal auditAmAmount;
    
    @ApiModelProperty(value = "人工调整金额")
    private BigDecimal calculateProfit;

    @ApiModelProperty(value = "离线时间")
    private String offLineTime;

}
