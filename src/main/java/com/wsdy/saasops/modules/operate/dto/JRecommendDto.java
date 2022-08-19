package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "JRecommendDto", description = "推荐送")
public class JRecommendDto {

    @ApiModelProperty(value = "活动范围")
    private ActivityScopeDto scopeDto;

    @ApiModelProperty(value = "是否审核 true是 false 否")
    private Boolean isAudit;

    @ApiModelProperty(value = "单次推荐奖励")
    private RecommendAwardDto award;

    @ApiModelProperty(value = "推荐红利(奖励推荐人)")
    private RecommendBonusDto bonus;
}
