package com.wsdy.saasops.api.modules.activity.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class HuPengSummaryDto {

    private BigDecimal yDReward;

    private Integer yDBetnum;

    private BigDecimal totalReward;

    private BigDecimal balance;

    private Integer num;
}
