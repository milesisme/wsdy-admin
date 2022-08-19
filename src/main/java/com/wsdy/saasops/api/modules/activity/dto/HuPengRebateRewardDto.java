package com.wsdy.saasops.api.modules.activity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class HuPengRebateRewardDto {

    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty(value = "邀请数量")
    private Integer num;

    @ApiModelProperty(value = "注单数量")
    private Integer betNum;

    @ApiModelProperty(value = "注单金额")
    private BigDecimal betAmount;

    @ApiModelProperty(value = "返佣金额")
    private BigDecimal reward;
}
