package com.wsdy.saasops.modules.member.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class MbrWarningBetInfoDto {

    private BigDecimal bet;

    private BigDecimal payout;

    private String startDay;

    private String userName;
}
