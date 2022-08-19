package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class CalcRebateValidBetDto {

    @ApiModelProperty(value = "子账号ID")
    private Integer  subAccountId;

    @ApiModelProperty(value = "子账号")
    private String subLoginName;

    @ApiModelProperty(value = "账号ID")
    private Integer accountId;

    @ApiModelProperty(value = "登录名")
    private String loginName;

    @ApiModelProperty(value = "活动等级ID")
    private Integer actLevelId;

    @ApiModelProperty(value = "目录ID")
    private String  gameCategory;

    @ApiModelProperty(value = "有效投注")
    private BigDecimal validBet;
}
