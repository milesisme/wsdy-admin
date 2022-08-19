package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@ApiModel(value = "JDepositSentDto", description = "存就送")
public class JDepositSentDto {

    @ApiModelProperty(value = "会员范围 0全部会员 1层级会员")
    private Integer scope;

    @ApiModelProperty(value = "流水范围")
    private List<AuditCat> auditCats;

    @ApiModelProperty(value = "流水范围跟层级 活动规则")
    List<RuleScopeDto> ruleScopeDtos;
}
