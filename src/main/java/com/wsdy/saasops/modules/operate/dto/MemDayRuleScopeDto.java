package com.wsdy.saasops.modules.operate.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "活动规则 跟范围", description = "会员日活动")
public class MemDayRuleScopeDto {

    @ApiModelProperty(value = "层级id")
    private int actLevelId;

    @ApiModelProperty(value = "存款金额限制 0不限制 1限制最低要求")
    private Integer depositAmountType;

    @ApiModelProperty(value = "最小存款金额")
    private BigDecimal depositMin;

    @ApiModelProperty(value = "上月存款不低于多少")
    private BigDecimal lastDepositMin;
    
    @ApiModelProperty(value = "上月存款次数不低于多少次")
    private Integer lastDepositMinTimes;

    @ApiModelProperty(value = "投注金额限制 0不限制 1限制最低要求")
    private Integer validBetType;
    
    @ApiModelProperty(value = "最小投注额")
    private BigDecimal validBetMin;

    @ApiModelProperty(value = "上月最小投注额")
    private BigDecimal lastValidBetMin;

    @ApiModelProperty(value = "赠送金额")
    private BigDecimal donateAmount;

    @ApiModelProperty(value = "流水倍数")
    private Double multipleWater;

    @JsonIgnore
    @ApiModelProperty(value = "有效投注")
    private BigDecimal validBet;
    @JsonIgnore
    @ApiModelProperty(value = "存款金额")
    private BigDecimal depositAmount;

    @ApiModelProperty(value = "等级前端页面使用")
    private Integer accountLevel;
    @ApiModelProperty(value = "等级前端页面使用")
    private String tierName;

}