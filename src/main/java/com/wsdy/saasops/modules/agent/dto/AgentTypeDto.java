package com.wsdy.saasops.modules.agent.dto;

import io.swagger.models.auth.In;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentTypeDto {

    private Integer agentType;

    private Integer agentId;

    private Integer parentId;

}
