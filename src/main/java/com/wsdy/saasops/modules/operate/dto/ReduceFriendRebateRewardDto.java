package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ReduceFriendRebateRewardDto {

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "返利类型")
    private Integer rewardType;

    @ApiModelProperty(value = "稽核是否清楚，0不清除，1清")
    private Integer audit;

    @ApiModelProperty(value = "备注")
    private String memo;
}
