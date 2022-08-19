package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentDay;
import com.wsdy.saasops.modules.member.entity.MbrFundsReport;
import com.wsdy.saasops.modules.member.entity.MbrMessageInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MbrFundsReportMapper extends MyMapper<MbrFundsReport> {

    List<MbrFundsReport> countMbrFundsReport(MbrFundsReport model);

    MbrFundsReport countMbrFundsReportByAccountId(MbrFundsReport model);

    MbrFundsReport getMbrTodayReport(MbrFundsReport model);

    List<MbrFundsReport> getMbrTodayReportList(MbrFundsReport model);

    int updateReportDepositByUserDate(MbrFundsReport entity);

}
