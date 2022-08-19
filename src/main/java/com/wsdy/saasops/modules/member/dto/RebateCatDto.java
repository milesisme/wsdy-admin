package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "会员操作内容")
public class RebateCatDto {

    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validBet;

    @ApiModelProperty(value = "上级返点")
    private BigDecimal topRebate;

    @ApiModelProperty(value = "分类id")
    private Integer catId;

    @ApiModelProperty(value = "分类名称")
    private String catName;
}
