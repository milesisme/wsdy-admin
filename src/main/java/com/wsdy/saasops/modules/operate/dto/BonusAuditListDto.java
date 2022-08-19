package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BonusAuditListDto {

    @ApiModelProperty(value = "id")
    private Integer bonuseId;

    @ApiModelProperty(value = "活动分类编号")
    private String tmplCode;

}
