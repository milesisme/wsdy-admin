package com.wsdy.saasops.modules.agent.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.agent.dto.AgentComReportDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface AgentComReportExtendMapper {

    List<AgentComReportDto> tagencyTotalFromReport(AgentComReportDto model);

    List<AgentComReportDto> categoryTotalFromReport(AgentComReportDto model);

    List<AgentComReportDto> memberTotalFromReport(AgentComReportDto model);

    List<AgentComReportDto> cagencyMemberTotalFromReport(AgentComReportDto model);

    List<AgentComReportDto> countAgentLine(@Param("agyAccount") String agyAccount, @Param("agentLevel") List<Integer> agentLevel);

}
