package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@ApiModel(value = "好友返利列表", description = "好友返利列表信息")

@Getter
@Setter
public class RebateFriendsDto {

    @ApiModelProperty(value = "会员帐号")
    private String subLoginName;

    @ApiModelProperty(value = "会员ID")
    private String subAccountId;

    @ApiModelProperty(value = "组名")
    private String groupName;

    @ApiModelProperty(value = "会员等级")
    private Integer accountLevel;

    @ApiModelProperty(value = "首充时间")
    private String firstChargeTime;

    @ApiModelProperty(value = "首充金额")
    private String firstCharge;

    @ApiModelProperty(value = "有效下注")
    private BigDecimal validBet;

    @ApiModelProperty(value = "累计首充")
    private BigDecimal totalDeposit;

}
