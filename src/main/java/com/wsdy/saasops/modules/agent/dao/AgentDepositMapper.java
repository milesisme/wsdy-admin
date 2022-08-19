package com.wsdy.saasops.modules.agent.dao;

import com.wsdy.saasops.modules.agent.entity.AgentDeposit;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface AgentDepositMapper extends MyMapper<AgentDeposit> {

}