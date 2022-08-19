package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ActBonusAuditDto {

    @ApiModelProperty(value = "状态 0 拒绝 1通过")
    private Integer status;

    @ApiModelProperty(value = "ids")
    private List<BonusAuditListDto> bonusAuditListDtos;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "活动名称")
    private String activityName;

    @ApiModelProperty(value = "分类id")
    private Integer catId;

}
