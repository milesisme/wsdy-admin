package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.modules.agent.dto.AgentChargeMDto;
import com.wsdy.saasops.modules.agent.dto.DepotCostDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChargeCostMapper {

    List<AgentChargeMDto> findServiceChargAgent(AgentChargeMDto model);

    AgentChargeMDto sumServiceChargAgent(AgentChargeMDto model);

    List<AgentChargeMDto> findServiceChargAccount(AgentChargeMDto model);

    AgentChargeMDto sumServiceChargAccount(AgentChargeMDto model);

}
