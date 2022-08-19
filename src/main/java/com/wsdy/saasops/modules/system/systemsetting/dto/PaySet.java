package com.wsdy.saasops.modules.system.systemsetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class PaySet {

    @ApiModelProperty("是否启用自动出款 0禁用 1启用")
    private Integer payAutomatic;
    
    @ApiModelProperty("是否启用支付宝出款出款 0禁用 1启用")
    private Integer alipayEnable;

    @ApiModelProperty("是否启用急速出款 0禁用 1启用")
    private Integer fastWithdrawEnable;
    
    @ApiModelProperty("自动出款单笔最高限额(元)")
    private BigDecimal payMoney;
    
    @ApiModelProperty("是否启用好友转款 0禁用 1启用")
    private Integer friendsTransAntomatic;
    
    @ApiModelProperty("好友转款单笔最高限额(元)")
    private BigDecimal friensTransMaxAmount;
    
    @ApiModelProperty("入款设置--1，真实姓名  2，手机号")
    private List<Integer> depositCondition;
    
    @ApiModelProperty("出款设置--1，真实姓名  2，手机号")
    private List<Integer> withdrawCondition;
    
    @ApiModelProperty("是否未通过稽核出款")
    private Integer isMultipleOpen;

    @ApiModelProperty("提款限时 0禁用  1启用")
    private Integer isWithdrawLimitTimeOpen;

    @ApiModelProperty("限制时间")
    private List<WithdrawLimitTimeDto> withdrawLimitTimeDtoList;
}
