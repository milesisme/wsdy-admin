package com.wsdy.saasops.modules.member.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Setter
@Getter
public class MbrWarningDiscountInfoDto {


    private String userName;

    private BigDecimal deposit;

    private BigDecimal discount ;
}
