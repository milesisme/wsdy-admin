package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class CommDepotListDto {

    @ApiModelProperty(value = "平台id")
    private Integer depotId;

    @ApiModelProperty(value = "平台code")
    private String depotCode;

    @ApiModelProperty(value = "平台Name")
    private String depotName;

    private List<CommCatDetailsDto> detailsDtoList;
}
