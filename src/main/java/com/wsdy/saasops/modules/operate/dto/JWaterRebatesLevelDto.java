package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(value = "JWaterRebatesLevelDto", description = "返水优惠层级比例设置")
public class JWaterRebatesLevelDto {

    @ApiModelProperty(value = "等级id")
    private Integer accountLevel;

    @ApiModelProperty(value = "1 流水倍数")
    private Double multipleWater;

    @ApiModelProperty(value = "分类")
    private List<JWaterRebatesLevelListDto> rebatesLevelListDtos;

}
