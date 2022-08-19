package com.wsdy.saasops.modules.activity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class RebateHuPengRewardDto {

    @ApiModelProperty(value = "账号ID")
    private Integer accountId;

    @ApiModelProperty(value = "会员帐号")
    private String loginName;

    @ApiModelProperty(value = "会员组")
    private  String groupName;

    @ApiModelProperty(value = "奖励注单数量")
    private Integer rewardNum;

    @ApiModelProperty(value = "注单数量")
    private Integer betNum;

    @ApiModelProperty(value = "下级数量")
    private Integer num;

    @ApiModelProperty(value = "中奖金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "返佣")
    private BigDecimal reward;

    @ApiModelProperty(value = "存款")
    private BigDecimal deposit = BigDecimal.ZERO ;

    @ApiModelProperty(value = "提款")
    private BigDecimal withdrawal = BigDecimal.ZERO;

    @ApiModelProperty(value = "优惠")
    private BigDecimal discount = BigDecimal.ZERO;

    @ApiModelProperty(value = "资金调整")
    private BigDecimal fundAdjust = BigDecimal.ZERO;

}
