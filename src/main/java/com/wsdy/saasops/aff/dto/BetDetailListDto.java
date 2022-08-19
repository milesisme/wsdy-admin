package com.wsdy.saasops.aff.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BetDetailListDto {

    @ApiModelProperty(value = "会员名")
    private String membercode;

    private String betid;

    private String provider;

    private String gametype;

    private BigDecimal betamount;

    private String turnover;

    private String winlose;

    private String betdate;

    private String description;

    private String status;
}
