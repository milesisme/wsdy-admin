package com.wsdy.saasops.agapi.modules.mapper;


import com.wsdy.saasops.agapi.modules.dto.AgentAccountDto;
import com.wsdy.saasops.agapi.modules.dto.AgentAccountReportDto;
import com.wsdy.saasops.agapi.modules.dto.AgentListDto;
import com.wsdy.saasops.modules.agent.entity.AgentCryptoCurrencies;
import com.wsdy.saasops.modules.agent.entity.AgyBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrCryptoCurrencies;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgapiMapper {

    AgentAccountDto findAccountInfo(@Param("accountId") Integer accountId);

    List<AgyBillDetail> findBillDetail(AgyBillDetail detail);

    List<AgentAccountReportDto> findAgentAccountReportList(
            @Param("cagencyId") Integer cagencyId,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    List<AgentListDto> fundAgentList(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("accountId") Integer id);


    boolean isTagency(@Param("accountId") Integer accountId);

    AgentCryptoCurrencies selectCryptoCurrenciesByAddress(
            @Param("id") Integer id,
            @Param("accountId") Integer accountId);

    List<AgentCryptoCurrencies> selectCryptocurrenciesCount(
            @Param("id") Integer id,
            @Param("walletaddress") String walletaddress);

    List<AgentCryptoCurrencies> userCryptoCurrencies(AgentCryptoCurrencies mbrCryptoCurrencies);

}
