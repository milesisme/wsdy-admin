package com.wsdy.saasops.modules.analysis.mapper;

import com.wsdy.saasops.modules.analysis.entity.BounsReportQueryModel;
import com.wsdy.saasops.modules.analysis.entity.RptWinLostModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface BounsMapper {

    List<RptWinLostModel> findSubordinateAgent(BounsReportQueryModel model);

    RptWinLostModel findSubordinateAgentListTotal(BounsReportQueryModel model);

    RptWinLostModel findSubordinateBonus(BounsReportQueryModel model);

}
