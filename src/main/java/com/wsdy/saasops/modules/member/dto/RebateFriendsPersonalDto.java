package com.wsdy.saasops.modules.member.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class RebateFriendsPersonalDto {

    @ApiModelProperty(value = "账单好")
    private String orderNo;

    @ApiModelProperty(value = "奖励")
    private BigDecimal reward;

    @ApiModelProperty(value = "添加时间")
    private String createTime;

    @ApiModelProperty(value = "添加者")
    private String creater;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "返利类型, 3 首充, 4 投注, 5 VIP, 6 充值")
    private Integer rewardType;

}
