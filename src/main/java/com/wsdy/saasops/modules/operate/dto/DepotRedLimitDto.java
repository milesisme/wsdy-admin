package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "平台限红")
public class DepotRedLimitDto {
    @ApiModelProperty(value = "API ID")
    private Integer apiId;
    @ApiModelProperty(value = "平台ID")
    private Integer depotId;
    @ApiModelProperty(value = "平台CODE")
    private String depotCode;
    @ApiModelProperty(value = "站点名称")
    private String siteName;
}
