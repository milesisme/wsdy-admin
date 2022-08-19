package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletFlowParamDto {

    @ApiModelProperty(value = "开始时间开始")
    private String startTime;

    @ApiModelProperty(value = "开始时间结束")
    private String endTime;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "状态")
    private Integer agentId;
}
