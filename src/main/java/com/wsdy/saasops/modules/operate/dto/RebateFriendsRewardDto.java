package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "好友返利", description = "好友返利奖励查询结果")
public class RebateFriendsRewardDto {

    @ApiModelProperty(value = "账号ID")
    private Integer accountId;

    @ApiModelProperty(value = "会员帐号")
    private String loginName;

    @ApiModelProperty(value = "会员组")
    private  String groupName;

    @ApiModelProperty(value = "邀请数量")
    private Integer inviteNum;

    @ApiModelProperty(value = "首存收益")
    private BigDecimal firstChargeReward;

    @ApiModelProperty(value = "有效投注收益")
    private BigDecimal validBetReward;

    @ApiModelProperty(value = "VIP升级收益")
    private BigDecimal vipUpgradeReward;

    @ApiModelProperty(value = "充值收益")
    private  BigDecimal chargeReward;

    @ApiModelProperty(value = "自身返利")
    private BigDecimal selfReward;

    @ApiModelProperty(value = "好友返利")
    private BigDecimal friendReward;

    @ApiModelProperty(value = "实际返利")
    private BigDecimal actualReward;

}
