package com.wsdy.saasops.modules.member.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RebateFriendsDetailsDto {

    @ApiModelProperty(value = "统计时间")
    private String createTime;

    @ApiModelProperty(value = "发放时间")
    private String giveOutTime;

    @ApiModelProperty(value = "发放金额")
    private BigDecimal actualReward;

    @ApiModelProperty(value = "类型 3 首充, 4 有效下注, 5 VIP, 6 充值" )
    private Integer type;

    @ApiModelProperty(value = "状态")
    private Integer status;  // 1成功 0 失败
}
