package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class FriendRebatePersonalRewardSummaryDto {

    @ApiModelProperty(value = "账号ID")
    private Integer accountId;

    @ApiModelProperty(value = "首充返利")
    private BigDecimal firstChargeReward;

    @ApiModelProperty(value = "有效下注返利")
    private BigDecimal validBetReward;

}
