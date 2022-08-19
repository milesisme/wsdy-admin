package com.wsdy.saasops.agapi.modules.mapper;

import com.wsdy.saasops.agapi.modules.dto.AgentListDto;
import com.wsdy.saasops.agapi.modules.dto.AgentWinLostReportDto;
import com.wsdy.saasops.agapi.modules.dto.AgentWinLostReportModelDto;
import com.wsdy.saasops.modules.analysis.dto.WinLostReportDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentWinLoseMapper {
    //代理top
    List<AgentWinLostReportDto> findWinLostReportList(AgentWinLostReportModelDto model);

    //代理统计
    AgentWinLostReportDto findWinLostSum(AgentWinLostReportModelDto model);

    String findAgyAccountDepth(@Param("agyAccount") String agyAccount);

    //代理
    List<AgentWinLostReportDto> findWinLostListLevelAgyAccount(AgentWinLostReportModelDto model);

    Integer findAgyAccountDepthLevel(@Param("agyAccount") String agyAccount);


    List<AgentWinLostReportDto> findWinLostLoginName(AgentWinLostReportModelDto model);

    //代理-》会员
    List<AgentWinLostReportDto> findAgyAccountLevelLoginName(AgentWinLostReportModelDto model);

    //会员下级
    List<AgentWinLostReportDto> findWinLostListLevelLoginName(AgentWinLostReportModelDto model);

    //会员sum+下级
    AgentWinLostReportDto findWinLostListSumByLoginName(AgentWinLostReportModelDto model);

    //会员自己sum
    AgentWinLostReportDto findWinLostListSumLoginName(AgentWinLostReportModelDto model);

    //会员top
    List<AgentWinLostReportDto> findWinLostReportListByLoginName(AgentWinLostReportModelDto model);

    List<AgentWinLostReportDto> findWinLostAccount(AgentWinLostReportModelDto model);

    List<AgentListDto>  selectAgentByParentIdList(@Param("agyAccountId") Integer agyAccountId);

    List<String>selectMbrAccountByAgrIdAndLoginName(@Param("agyAccountId") Integer agyAccountId, @Param("loginName") String loginName);

    WinLostReportDto findWinLostReportView(AgentWinLostReportModelDto model);
    List<WinLostReportDto> findWinLostReportViewAgent(AgentWinLostReportModelDto model);
}
