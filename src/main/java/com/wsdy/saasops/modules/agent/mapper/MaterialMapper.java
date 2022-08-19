package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.modules.agent.entity.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MaterialMapper {

    List<AgentMaterial> materialList(AgentMaterial material);

    List<AgentMaterialDetail> materialDetailList(AgentMaterialDetail material);

}
