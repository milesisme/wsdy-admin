package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OpusSbResp {
    private String statusCode;
    private String statusText;
    private BigDecimal userBalance;
    private String currency;
    private Boolean isOnline;
    private String status;
    private String memberStatus;
}
