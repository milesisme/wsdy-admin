package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JWaterRebatesAgentDto {
    @ApiModelProperty(value = "代理id")
    private Integer agentId;

    @ApiModelProperty(value = "代理账号")
    private String agentAccount;

    @ApiModelProperty(value = "层级返水规则")
    private List<JWaterRebatesLevelDto> levelDtoList;

}
