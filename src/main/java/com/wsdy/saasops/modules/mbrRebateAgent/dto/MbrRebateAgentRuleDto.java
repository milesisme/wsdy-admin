package com.wsdy.saasops.modules.mbrRebateAgent.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@ApiModel(value = "MbrRebateAgentRuleDto", description = "全民代理")
public class MbrRebateAgentRuleDto {
    @ApiModelProperty(value = "等级活动规则")
    List<MbrRebateAgentRuleLevelDto> ruleScopeDtos;
}
