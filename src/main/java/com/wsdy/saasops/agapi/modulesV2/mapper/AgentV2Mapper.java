package com.wsdy.saasops.agapi.modulesV2.mapper;


import com.wsdy.saasops.agapi.modules.dto.AgentAccountDto;
import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2ListDto;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentAccountOther;
import com.wsdy.saasops.modules.agent.entity.AgyTree;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAccountOther;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentV2Mapper  extends MyMapper<AgentAccount> {
    AgentAccount getAgentInfo(AgentAccount agentAccount);
    AgentAccountDto findAccountInfo(@Param("accountId") Integer accountId);
    List<AgentV2ListDto> getSubAgentList(AgentAccount agentAccount);
    List<AgentV2ListDto> getSubAccountList(AgentAccount agentAccount);
    AgyTree getAgentByDepth(@Param("childNodeId") Integer childNodeId, @Param("depth") Integer depth);
    Integer updateMbrBettingStatus(MbrAccountOther mbrAccountOther);
    Integer updateMbrBettingStatusByAgent(AgentAccount agentAccount);
    List<MbrAccountOther> getMbrBettingStatusByAgentList(AgentAccount agentAccount);
    Integer updateAgentBettingStatus(AgentAccount agentAccount);
    List<AgentAccountOther> getAgentBettingStatusList(AgentAccount agentAccount);
    AgentAccountOther selectByAgent(AgentAccount agentAccount);
    MbrAccountOther selectByMbr(MbrAccount mbrAccount);

    List<AgentV2ListDto> getSearchUser(AgentAccount agentAccount);
    List<AgentV2ListDto> getSearchUserMbr(AgentAccount agentAccount);
    List<AgentV2ListDto> getSearchUserAgent(AgentAccount agentAccount);
    List<AgentV2ListDto> judgeMbrOrAgent(AgentAccount agentAccount);

}
