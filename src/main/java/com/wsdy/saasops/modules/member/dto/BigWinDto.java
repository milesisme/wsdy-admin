package com.wsdy.saasops.modules.member.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class BigWinDto {

    private BigDecimal payout1 = BigDecimal.ZERO;

    private BigDecimal payout2 = BigDecimal.ZERO;
}
