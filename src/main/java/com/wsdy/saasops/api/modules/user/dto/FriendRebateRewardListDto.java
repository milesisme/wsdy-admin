package com.wsdy.saasops.api.modules.user.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class FriendRebateRewardListDto {

    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "奖励")
    private BigDecimal reward;
}
