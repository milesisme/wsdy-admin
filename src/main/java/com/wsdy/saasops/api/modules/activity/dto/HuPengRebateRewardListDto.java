package com.wsdy.saasops.api.modules.activity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class HuPengRebateRewardListDto {
    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "奖励")
    private BigDecimal reward;

}
