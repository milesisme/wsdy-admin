package com.wsdy.saasops.modules.agent.mapper;


import com.wsdy.saasops.modules.agent.entity.AgentMerchantDetail;
import com.wsdy.saasops.modules.agent.entity.AgyWithdraw;
import com.wsdy.saasops.modules.fund.dto.CountEntity;
import com.wsdy.saasops.modules.fund.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WithdrawMapper {

    List<AgyWithdraw> findAccWithdrawList(AgyWithdraw accWithdraw);

    int findAccWithdrawCount(AgyWithdraw accWithdraw);

    Double accSumDrawingAmount(AgyWithdraw accWithdraw);

    int findMerchantPayCount(@Param("accountId") Integer accountId);

    List<AgyWithdraw> fundAccWithdrawMerchant(@Param("accountId") Integer accountId);

    List<CountEntity> withdrawCountByStatus(AgyWithdraw accWithdraw);

    AgyWithdraw findAccWithdrawListSum(AgyWithdraw accWithdraw);

    FundMerchantPay getMerchantPayByOrderno(@Param("orderNo") String orderNo);

    int updateAllLockStatus();

    AgentMerchantDetail findAgentMerchantDetailByTransId(@Param("orderId") String orderId);

}
