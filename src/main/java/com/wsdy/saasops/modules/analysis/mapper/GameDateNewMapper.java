package com.wsdy.saasops.modules.analysis.mapper;

import com.wsdy.saasops.modules.analysis.entity.GameReportModel;
import com.wsdy.saasops.modules.analysis.entity.RptBetTotalModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface GameDateNewMapper {

    List<RptBetTotalModel> getBetDayGroupAgentList(GameReportModel model);

    List<RptBetTotalModel> getBetDayGroupAgentCount(GameReportModel model);

    List<RptBetTotalModel> findBetDayBetAgent(GameReportModel model);

    List<RptBetTotalModel> getBetDayGroupGameTypeList(GameReportModel model);

    RptBetTotalModel getBetDayByAgentTotal(GameReportModel model);

    List<RptBetTotalModel> getBetDayGroupUserList(GameReportModel model);
}
