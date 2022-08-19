package com.wsdy.saasops.modules.member.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class HighDiscountDto {

    private BigDecimal deposit = BigDecimal.ZERO;

    private BigDecimal discount = BigDecimal.ZERO;
}
