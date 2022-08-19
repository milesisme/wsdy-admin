package com.wsdy.saasops.modules.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class DepotCatDto {
    @ApiModelProperty(value = "游戏大类表id")
    private Integer catId;
    @ApiModelProperty(value = "大类名称")
    private String catName;
    @ApiModelProperty(value = "大类编码")
    private String catCode;
    @ApiModelProperty(value = "平台id")
    private Integer depotId;
    @ApiModelProperty(value = "平台名称")
    private String depotName;
    @ApiModelProperty(value = "平台编码")
    private String depotCode;
}