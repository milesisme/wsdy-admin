package com.wsdy.saasops.modules.member.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class HighWinDto {

   private   BigDecimal bet = BigDecimal.ZERO;

   private BigDecimal payout = BigDecimal.ZERO;
}
