package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FriendRebateSumDto {

    private Integer type;

    private BigDecimal actualReward;

    private BigDecimal firstChargeReward;

    private BigDecimal validBetReward;

}
