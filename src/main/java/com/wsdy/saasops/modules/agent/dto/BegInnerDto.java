package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class BegInnerDto {

    @ApiModelProperty(value = "代理平台佣金")
    private List<ReportDepotDto> reportDepotDtos;

    @ApiModelProperty(value = "上级平台佣金")
    private List<ReportDepotDto> topReportDepotDtos;

    private Integer stage;

    private String condition;
}
