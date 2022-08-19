package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
public class ContractDto {

    @ApiModelProperty(value = "佣金比例字符串")
    private List<AgentContractDto> contractDtos;
}
