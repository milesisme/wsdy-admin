package com.wsdy.saasops.modules.member.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value ="会员存取款，优惠， 人工调整 合计")
public class MbrFundTotal {
    @ApiModelProperty(value = "线上支付实际到账")
    private BigDecimal onlineactualArrivals;
    @ApiModelProperty(value = "线上支付申请金额")
    private BigDecimal onlinedepositAmounts;
    @ApiModelProperty(value = "线上支付充值笔数")
    private Integer onlinedepositNum;
    @ApiModelProperty(value = "公司入款实际到账金额")
    private BigDecimal offlineactualArrivals;
    @ApiModelProperty(value = "公司入款申请总金额")
    private BigDecimal offlinedepositAmounts;
    @ApiModelProperty(value = "公司入款充值笔数")
    private Integer offlinedepositNum;
    @ApiModelProperty(value = "取款实际到账金额")
    private BigDecimal withdrawactualArrivals;
    @ApiModelProperty(value = "取款申请金额")
    private BigDecimal withdrawdrawingAmounts;
    @ApiModelProperty(value = "取款笔数")
    private Integer withDrawNum;
    @ApiModelProperty(value = "优惠总金额")
    private BigDecimal bonusAmounts;
    @ApiModelProperty(value = "优惠笔数")
    private Integer bonusNum;
    @ApiModelProperty(value = "人工调整总金额")
    private BigDecimal amounts;
    @ApiModelProperty(value = "人工调整笔数")
    private Integer adjustNum;
    @ApiModelProperty(value = "任务返利金额")
    private BigDecimal bonusAmountTotal;
    @ApiModelProperty(value = "任务返利次数")
    private Integer bonusAmountNum;

    @ApiModelProperty(value = "邀请好友数量")
    private Integer friendsNum;

    @ApiModelProperty(value = "好友奖励")
    private BigDecimal friendsReward;

    @ApiModelProperty(value = "呼朋邀请好友数量")
    private Integer huPengNum;

    @ApiModelProperty(value = "呼朋好友奖励")
    private BigDecimal huPengReward;
    
    @ApiModelProperty(value = "核对调整 + bonusAmounts + bonusAmountTotal + amounts + offlinedepositAmounts + onlinedepositAmounts + payout - withdrawdrawingAmounts")
    private BigDecimal adjustment;
}
