package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "JWaterRebatesLevelDto", description = "返水优惠层级比例设置")
public class JWaterRebatesLevelListDto {

    @ApiModelProperty(value = "平台id")
    private Integer depotId;

    @ApiModelProperty(value = "分类")
    private Integer catId;

    @ApiModelProperty(value = "返水比例")
    private BigDecimal donateRatio;
}
