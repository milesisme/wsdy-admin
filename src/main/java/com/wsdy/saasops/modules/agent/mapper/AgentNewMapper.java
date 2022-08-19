package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.modules.agent.entity.*;
import com.wsdy.saasops.modules.sys.dto.ColumnAuthTreeDto;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface AgentNewMapper {

    List<AgentAccount> newfindAgyAccountListPage(AgentAccount agentAccount);

    List<AgentAccount> totalAgentList(AgentAccount agentAccount);

    List<Map<String, Object>> agent0verview(@Param("startTime") String startTime,
                            @Param("endTime") String endTime,
                            @Param("agentId") Integer agentId,
                            @Param("subcagencyId") Integer subcagencyId);

	AgentAccount viewOtherAccount(@Param("columnSets") Set<String> columnSets, @Param("agyId") Integer agyId);

}
