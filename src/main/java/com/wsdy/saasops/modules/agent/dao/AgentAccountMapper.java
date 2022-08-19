package com.wsdy.saasops.modules.agent.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface AgentAccountMapper extends MyMapper<AgentAccount> {

}