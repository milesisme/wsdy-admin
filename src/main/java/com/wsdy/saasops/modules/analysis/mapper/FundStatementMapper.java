package com.wsdy.saasops.modules.analysis.mapper;

import com.wsdy.saasops.modules.analysis.dto.FundAgentStatementDto;
import com.wsdy.saasops.modules.analysis.dto.FundStatementDto;
import com.wsdy.saasops.modules.analysis.entity.FundStatementModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FundStatementMapper {
    List<FundStatementModel> findFundReportPage(FundStatementModel model);
    List<FundStatementModel> findFundReportMbrPage(FundStatementModel model);
    FundStatementModel findFundTotalInfo(FundStatementModel model);
    List<FundStatementModel> findDepotPayoutList(FundStatementModel model);
    List<FundStatementModel> findTagencyList(FundStatementModel model);
    List<FundStatementModel> findMemberList(FundStatementModel model);

    List<FundStatementModel> agentSubMbrList(FundStatementModel model);
    
    /**
     * 	会员资金报表
     * @param model
     * @return
     */
    List<FundStatementModel> fundMbrList(FundStatementModel model);
    
    List<FundStatementModel> mbrSubList(FundStatementModel model);

    List<FundStatementDto> agentList(FundStatementModel model);

    FundStatementDto agentListSum(FundStatementModel model);

    List<FundAgentStatementDto> tagencyFundList(FundStatementModel model);

}
