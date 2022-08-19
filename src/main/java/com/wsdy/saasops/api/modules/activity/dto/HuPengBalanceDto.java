package com.wsdy.saasops.api.modules.activity.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class HuPengBalanceDto {

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 最小金额
     */
    private BigDecimal minAmount;
}
