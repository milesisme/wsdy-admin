package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(value = "JPreferentialDto", description = "首存送")
public class JPreferentialDto {

    @ApiModelProperty(value = "会员范围 0全部会员 1层级会员")
    private Integer scope;

    @ApiModelProperty(value = "流水范围")
    private List<AuditCat> auditCats;

    @ApiModelProperty(value = "层级 活动规则")
    private List<RuleScopeDto> ruleScopeDtos;
}
