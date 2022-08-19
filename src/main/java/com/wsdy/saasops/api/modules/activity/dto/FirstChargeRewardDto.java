package com.wsdy.saasops.api.modules.activity.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class FirstChargeRewardDto {
    private BigDecimal amount;

    private String giveTime;

    private Integer status = 1;

    private String subUserName;
}
