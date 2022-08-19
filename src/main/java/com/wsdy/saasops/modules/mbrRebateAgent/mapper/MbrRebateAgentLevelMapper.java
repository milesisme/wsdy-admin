package com.wsdy.saasops.modules.mbrRebateAgent.mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentLevel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MbrRebateAgentLevelMapper extends MyMapper<MbrRebateAgentLevel>{
    List<MbrRebateAgentLevel> getMbrAgentLevelList();
}
