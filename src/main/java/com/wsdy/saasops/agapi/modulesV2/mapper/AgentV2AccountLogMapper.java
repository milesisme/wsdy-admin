package com.wsdy.saasops.agapi.modulesV2.mapper;

import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2AccountLogDto;
import com.wsdy.saasops.agapi.modulesV2.entity.AgyAccountLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentV2AccountLogMapper {
	List<AgentV2AccountLogDto>  getAccountLogList(AgentV2AccountLogDto agentV2AccountLogDto);

	List<AgentV2AccountLogDto>  getAccountLogListAgent(AgentV2AccountLogDto agentV2AccountLogDto);
	List<AgentV2AccountLogDto>  getAccountLogListMbr(AgentV2AccountLogDto agentV2AccountLogDto);
	List<AgentV2AccountLogDto>  getAccountLogListOperator(AgentV2AccountLogDto agentV2AccountLogDto);

	int batchInsertAgyAccountLog(List<AgyAccountLog> list);
}
