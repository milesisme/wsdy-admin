package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@ApiModel(value = "AgentGroupDto", description = "总代代理组ID")
public class ActivityScopeDto {

    @ApiModelProperty(value = "会员组是否全部 true是 false否")
    private Boolean isAccAll;

    @ApiModelProperty(value = "会员组ID")
    private List<Integer> accIds;

    @ApiModelProperty(value = "总代是否全部 true是 false否")
    private Boolean isAgyTopAll;

    @ApiModelProperty(value = "总代ID")
    private List<Integer> agyTopIds;

    @ApiModelProperty(value = "代理是否全部 true是 false否")
    private Boolean isAgyAll;

    @ApiModelProperty(value = "代理ID")
    private List<Integer> agyIds;
}
