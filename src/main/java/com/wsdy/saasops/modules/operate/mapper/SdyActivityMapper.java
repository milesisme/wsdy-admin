package com.wsdy.saasops.modules.operate.mapper;

import com.wsdy.saasops.api.modules.user.dto.SdyActivity.ActivityLevelDto;
import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.lottery.entity.OprActLottery;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SdyActivityMapper {

    List<ActivityLevelDto> findAcocountActivityLevel(@Param("id") Integer id);

    List<MbrAccount> findAccountLevelList(@Param("lastId") Integer lastId);

    BaseBank findBankOne(@Param("bankname") String bankname);

    FundDeposit findLotterFundDeposit(
            @Param("accountId") Integer accountId,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    int countLotteryByAccountId(
            @Param("accountId") Integer accountId,
            @Param("prizeArea") Integer prizeArea,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    List<OprActLottery> findLotteryList(
            @Param("prizeArea") Integer prizeArea);

    int findLotteryMax(@Param("prizeArea") Integer prizeArea);
}
