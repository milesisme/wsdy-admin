package com.wsdy.saasops.modules.agent.dao;

import com.wsdy.saasops.modules.agent.entity.AgentAudit;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface AgentAuditMapper extends MyMapper<AgentAudit> {

}