package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(value = "JWaterRebatesDto", description = "返水优惠")
public class JWaterRebatesNeDto {

    @ApiModelProperty(value = "0 平台活动规则范围")
    private List<AuditCat> auditCats;

    @ApiModelProperty(value = "0 活动规则")
    private List<WaterRebatesRuleListDto> ruleListDtos;

    @ApiModelProperty(value = "1 层级返水规则")
    private List<JWaterRebatesLevelDto> levelDtoList;

    @ApiModelProperty(value = "代理层级返水规则")
    private List<JWaterRebatesAgentDto> agentDtoList;

}
