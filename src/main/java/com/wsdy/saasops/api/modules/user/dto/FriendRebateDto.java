package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FriendRebateDto {

    @ApiModelProperty(value = "好友账号")
    private String subLoginName;

    @ApiModelProperty(value = "好友账号ID")
    private Integer subAccountId;

    @ApiModelProperty(value = "首充时间")
    private String firstChargeTime;

    @ApiModelProperty(value = "首充金额")
    private BigDecimal firstCharge;

    @ApiModelProperty(value = "首充收益")
    private BigDecimal firstChargeReward;

    @ApiModelProperty(value = "投注收益")
    private BigDecimal validBetReward;

    @ApiModelProperty(value = "体育投注收益")
    private BigDecimal tyValidBetReward;

    @ApiModelProperty(value = "电子投注收益")
    private BigDecimal dzValidBetReward;

    @ApiModelProperty(value = "棋牌投注收益")
    private BigDecimal qpValidBetReward;

    @ApiModelProperty(value = "彩票投注收益")
    private BigDecimal cpValidBetReward;

    @ApiModelProperty(value = "电竞投注收益")
    private BigDecimal djValidBetReward;

    @ApiModelProperty(value = "真人投注收益")
    private BigDecimal zrValidBetReward;

    @ApiModelProperty(value = "总存款")
    private BigDecimal totalDeposit;

    @ApiModelProperty(value = "最后登录时间")
    private String lastLoginTime;

    @ApiModelProperty(value = "VIP等级")
    private Integer vipLevel;

}
