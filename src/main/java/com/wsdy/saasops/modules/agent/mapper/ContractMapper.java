package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.modules.agent.entity.AgentContract;
import com.wsdy.saasops.modules.agent.entity.AgentMaterial;
import com.wsdy.saasops.modules.agent.entity.AgentMaterialDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ContractMapper {

    List<AgentContract> contractList();

}
