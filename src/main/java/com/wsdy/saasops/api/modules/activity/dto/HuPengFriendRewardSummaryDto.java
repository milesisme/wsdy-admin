package com.wsdy.saasops.api.modules.activity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Setter
@Getter
public class HuPengFriendRewardSummaryDto {

    private Integer betNum = 0;

    private BigDecimal amount = BigDecimal.ZERO;

    private BigDecimal reward = BigDecimal.ZERO;

    @ApiModelProperty(value = "存款")
    private BigDecimal deposit = BigDecimal.ZERO ;

    @ApiModelProperty(value = "提款")
    private BigDecimal withdrawal = BigDecimal.ZERO;

    @ApiModelProperty(value = "优惠")
    private BigDecimal discount = BigDecimal.ZERO;

    @ApiModelProperty(value = "资金调整")
    private BigDecimal fundAdjust = BigDecimal.ZERO;


}
