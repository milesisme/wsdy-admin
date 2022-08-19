package com.wsdy.saasops.modules.agent.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.wsdy.saasops.modules.agent.dto.AgentCategoryDto;
import com.wsdy.saasops.modules.agent.dto.AgentComReportDto;
import com.wsdy.saasops.modules.agent.dto.AgentUnionDto;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface AgentComReportMapper {
    AgentComReportDto totalInfo(AgentComReportDto model);
    AgentComReportDto totalInfoFromReport(AgentComReportDto model);
    List<AgentComReportDto> totalListByDay(AgentComReportDto model);
    List<AgentComReportDto> totalListByDayFromReport(AgentComReportDto model);
    List<AgentComReportDto> tagencyListFromReport(AgentComReportDto model);
    List<AgentComReportDto> categoryListFromReport(AgentComReportDto model);
    List<AgentComReportDto> memberListFromReport(AgentComReportDto model);

    List<AgentCategoryDto> categoryDtoList(AgentComReportDto model);

    List<AgentUnionDto> selectFundDepositListByTime(AgentComReportDto model);
    List<AgentUnionDto> selectAccWithdrawListByTime(AgentComReportDto model);
    List<AgentUnionDto> selectOprActBonusByTime(AgentComReportDto model);
    List<AgentUnionDto> selectBetRcdDayDtoByTime(AgentComReportDto model);
    List<AgentUnionDto> selectNewAddAccountListByTime(AgentComReportDto model);
    List<AgentUnionDto> selectNewDepositListListByTime(AgentComReportDto model);
    List<AgentUnionDto> selectActiveAccountListByTime(AgentComReportDto model);
    List<AgentComReportDto> getTotalSubAgentNum(@Param("id")Integer id,@Param("paramList")List<String> paramList);
    List<AgentComReportDto> getTotalSubMbrNumList(@Param("id")Integer id,@Param("paramList")List<String> paramList);

}
