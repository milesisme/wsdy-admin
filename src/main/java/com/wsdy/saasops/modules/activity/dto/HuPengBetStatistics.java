package com.wsdy.saasops.modules.activity.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class HuPengBetStatistics {

    /**
     * 下注数两
     */
    private Integer betNum = 0;

    /**
     * 中奖注单
     */
    private Integer rewardBetNum = 0;

    /**
     *中奖金额
     */
    private BigDecimal amount = BigDecimal.ZERO;
}
