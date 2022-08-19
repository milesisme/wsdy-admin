package com.wsdy.saasops.modules.mbrRebateAgent.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "等级活动规则", description = "全民代理")
public class MbrRebateAgentRuleLevelDto {

    @ApiModelProperty(value = "层级id")
    private int agyLevelId;
    @ApiModelProperty(value = "奖金百分比")
    private BigDecimal bonusPercent;

    @ApiModelProperty(value = "自身有效输赢提成比例")
    private List<MbrRebateAgentRuleLevelSelfDto> mbrRebateAgentRuleSelfDtos;
    @ApiModelProperty(value = "下级代理会员佣金比例")
    private List<MbrRebateAgentRuleLevelSubDto> mbrRebateAgentRuleSubDtos;
}
