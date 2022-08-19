package com.wsdy.saasops.agapi.modulesV2.mapper;

import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2LoginLogDto;
import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2WinLostReportDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentV2WinLoseMapper {
    String findAgyAccountDepth(@Param("agyAccount") String agyAccount);
    List<AgentV2WinLostReportDto> findWinLostReportList(AgentV2WinLostReportDto model);

    AgentV2WinLostReportDto findWinLostReportListSum(AgentV2WinLostReportDto model);

    Integer findWinLostReportListMbrSum(AgentV2WinLostReportDto model);

    List<AgentV2WinLostReportDto> findWinLostListLevel(AgentV2WinLostReportDto model);
    List<AgentV2WinLostReportDto> findWinLostListLevelMbr(AgentV2WinLostReportDto model);


    AgentV2LoginLogDto  getWinLosePayoutAgent(AgentV2LoginLogDto agentV2LoginLogDto);
    AgentV2LoginLogDto  getWinLosePayoutMbr(AgentV2LoginLogDto agentV2LoginLogDto);
}
