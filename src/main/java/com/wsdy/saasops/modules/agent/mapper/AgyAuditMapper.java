package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.modules.agent.entity.AgentAudit;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgyAuditMapper {

    List<AgentAudit> auditList(AgentAudit agentAudit);

}
