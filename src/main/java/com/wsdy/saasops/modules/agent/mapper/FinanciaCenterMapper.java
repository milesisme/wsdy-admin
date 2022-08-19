package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.agapi.modules.dto.WalletFlowParamDto;
import com.wsdy.saasops.agapi.modules.dto.WalletFlowResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface FinanciaCenterMapper {


   BigDecimal sumWithdramAmount(@Param("agentId") Integer agentId);

   List<WalletFlowResponseDto> walletFlow(WalletFlowParamDto flowParamDto);

   List<WalletFlowResponseDto> rechargeWalletFlow(WalletFlowParamDto flowParamDto);
   List<WalletFlowResponseDto> payoffWalletFlow(WalletFlowParamDto flowParamDto);

}
