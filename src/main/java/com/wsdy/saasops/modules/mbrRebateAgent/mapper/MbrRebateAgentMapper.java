package com.wsdy.saasops.modules.mbrRebateAgent.mapper;

import com.wsdy.saasops.modules.mbrRebateAgent.dto.*;
import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentBonus;
import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentMonth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;


@Mapper
public interface MbrRebateAgentMapper {
    // 前端查询
    Integer qryMbrDepth(@Param("accountId") Integer accountId);
    BigDecimal qryDepositSum(@Param("accountId") Integer accountId);
    BigDecimal qryDepositSumFromChild(@Param("accountId") Integer accountId);
    List<MbrRebateAgentRespChildListDto> getChildList(MbrRebateAgentQryDto dto);

    // 计算
    // 日表月表公共
    List<MbrRebateAgentQryDto> qryMbrRebateAgentDepthList(MbrRebateAgentQryDto dto);
    List<MbrRebateAgentQryDto> qryMbrRebateAgentDepthListEx(MbrRebateAgentQryDto dto);
    BigDecimal qryMbrBonus(MbrRebateAgentQryDto dto);
    MbrRebateAgentQryDto getValidPayout(MbrRebateAgentQryDto dto);
    // 日表计算
    BigDecimal getValidPayoutFromChildMember(MbrRebateAgentQryDto dto);
    BigDecimal getValidPayoutFromChildMemberMonth(MbrRebateAgentQryDto dto);
    BigDecimal getValidPayoutFromChildMemberAgent(MbrRebateAgentQryDto dto);
    BigDecimal getValidPayoutFromChildMemberAgentMonth(MbrRebateAgentQryDto dto);
    // 月表计算
    BigDecimal getValidPayoutFromChildMemberEx(MbrRebateAgentQryDto dto);
    BigDecimal getValidPayoutFromChildMemberAgentEx(MbrRebateAgentQryDto dto);
    BigDecimal getRebateChildTotal(MbrRebateAgentQryDto dto);
    List<MbrRebateAgentMonth> getSubMemAgent (MbrRebateAgentQryDto dto);
    List<MbrRebateAgentBonus> getDealStatusList (MbrRebateAgentBonus dto);
    BigDecimal getBonusAmountExfromChildTotal(MbrRebateAgentQryDto dto);
    BigDecimal getSubTotalValidBet(MbrRebateAgentQryDto dto);
    BigDecimal getAllSubMemAgentRebateChildTotal(MbrRebateAgentQryDto dto);
    List<MbrRebateAgentMonth> getAllSubMemAgentRebateChildList(MbrRebateAgentQryDto dto);

    // 返利列表
    List<MbrRebateAgentRespBonusListDto> qryBonusList(MbrRebateAgentQryDto dto);
    List<MbrRebateAgentRespBonusListDto> getChildBonusList(MbrRebateAgentQryDto dto);
    int batchUpdateBonus(MbrRebateAgentAuditDto dto);
    List<MbrRebateAgentRespHistoryListDto> getMbrRebateAgentDayList(MbrRebateAgentQryDto dto);

}
