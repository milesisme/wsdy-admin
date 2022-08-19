package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class FriendRebateRewardDto {

    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty(value = "邀请数量")
    private Integer num;

    @ApiModelProperty(value = "首存收益")
    private BigDecimal firstChargeReward;

    @ApiModelProperty(value = "投注收益")
    private BigDecimal validBetReward;

    @ApiModelProperty(value = "产生奖励数量")
    private Integer rnum;
}
