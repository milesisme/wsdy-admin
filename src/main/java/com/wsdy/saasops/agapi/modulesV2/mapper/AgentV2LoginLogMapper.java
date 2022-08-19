package com.wsdy.saasops.agapi.modulesV2.mapper;

import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2LoginLogDto;
import com.wsdy.saasops.agapi.modulesV2.entity.LogAgyLogin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentV2LoginLogMapper {
	LogAgyLogin findMemberLoginLastOne(String loginName);
	int updateLoginTime(@Param("id") Integer id);
	List<AgentV2LoginLogDto>  getLoginLogListAgent(AgentV2LoginLogDto agentV2LoginLogDto);
	List<AgentV2LoginLogDto>  getLoginLogListMbr(AgentV2LoginLogDto agentV2LoginLogDto);
}
