package com.wsdy.saasops.modules.agent.mapper;


import com.wsdy.saasops.api.modules.pay.dto.DepositPostScript;
import com.wsdy.saasops.modules.agent.entity.AgentDeposit;
import com.wsdy.saasops.modules.fund.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepositMapper {

    List<AgentDeposit> findDepositList(AgentDeposit fundDeposit);

    Double findSumDepositAmount(AgentDeposit fundDeposit);

    int findDepositCount(FundDeposit fundDeposit);

    int updatePayStatus(AgentDeposit deposit);

    AgentDeposit findDepositListSum(AgentDeposit fundDeposit);

    DepositPostScript findOfflineDepositInfo(@Param("id") Integer id);

}
