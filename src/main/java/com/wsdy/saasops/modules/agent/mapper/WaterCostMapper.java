package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.modules.agent.dto.DepotCostDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WaterCostMapper {

    DepotCostDto findAgentCostSum(DepotCostDto model);

    DepotCostDto findCostLostReportView(DepotCostDto model);

    List<DepotCostDto> findCostReportViewAgent(DepotCostDto model);

    List<DepotCostDto> findCostListLevel(DepotCostDto model);

    DepotCostDto findCostListLevelSum(DepotCostDto model);

    List<DepotCostDto> findCostAccountDetails(DepotCostDto model);

}
