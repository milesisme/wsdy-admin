package com.wsdy.saasops.modules.operate.mapper;

import com.wsdy.saasops.modules.fund.dto.CountEntity;
import com.wsdy.saasops.modules.member.dto.RebateFriendsDetailsDto;
import com.wsdy.saasops.modules.member.dto.RebateFriendsPersonalDto;
import com.wsdy.saasops.modules.member.entity.MbrRebateFriendsReward;
import com.wsdy.saasops.modules.operate.dto.*;
import com.wsdy.saasops.modules.operate.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OperateActivityMapper {

    List<OprActActivity> findOprActActivityList(OprActActivity activity);

    OprActActivity findOprActActivity(@Param("id") Integer id);

    int findOprActBouns(OprActBonus waterBonus);

    List<TGmGame> findGameList(TGmGame gmGame);

    List<OprActActivity> findWebActList(
            @Param("actCatId") Integer actCatId,
            @Param("accountId") Integer accountId,
            @Param("terminal") Byte terminal,
            @Param("discount") Integer discount,
            @Param("buttonShow") Integer buttonShow,
            @Param("time") String time,
            @Param("tmplCode") String tmplCode,
            @Param("isShow") Integer isShow);

    List<OprActBonus> findWaterBonusList(OprActBonus waterBonus);

    List<ActivityWaterDetailDto> waterAuditList(OprActBonus waterBonus);

    List<ActivityWaterCatDto> depotAuditListQry(@Param("ids") List<Integer> ids);

    List<ActivityWaterTotalDto> waterList(OprActBonus waterBonus);

    List<OprActBonus> findAccountBonusList(OprActBonus bonus);

    List<OprActBonus> bonusAndTaskList(OprActBonus bonus);

    List<OprActBonus> findAccountBonusListEgSanGong(OprActBonus bonus);

    int findBounsCount(OprActBonus bonus);

    int findBounsCountEx(OprActBonus bonus);

    List<OprActActivity> findActivityBySatatus();

    List<BonusListDto> findAccountBouns(
            @Param("accountId") Integer accountId,
            @Param("status") Integer status,
            @Param("id") Integer id);

    List<TGmDepot> findDepotByCatId(
            @Param("catId") Integer catId);

    List<OprActBonus> findAccountBounsByStatus(
            @Param("accountId") Integer accountId,
            @Param("id") Integer id);

    int updateBounsState(
            @Param("activityId") Integer activityId,
            @Param("status") Integer status, @Param("memo") String memo);

    List<OprActActivity> findWaterActivity(OprActActivity activity);

    OprActActivity findAffActivity(OprActActivity activity);

    int findValidbetCount(
            @Param("activityId") Integer activityId,
            @Param("time") String time,
            @Param("createuser") String createuser,
            @Param("accountId") Integer accountId
    );

    OprActWater findSumAccountWater(
            @Param("accountId") Integer accountId,
            @Param("waterIds") List<Integer> waterIds);

    int updateOprActWater(
            @Param("bonusId") Integer bonusId,
            @Param("waterIds") List<Integer> waterIds);

    int findDepositApplyForActivity(OprActBonus bonus);

    List<CountEntity> activityAuditCountByStatus(OprActBonus oprActBonus);

    int findBonusWaterCount(
            @Param("accountId") Integer accountId,
            @Param("tmplCode") String tmplCode,
            @Param("time") String time);

    List<OprActRule> findoActRuleList(OprActRule actRule);

    List<OprActCat> findOprActCatList();

    List<OprActCat> findOprActCatListPage();

    List<OprActRule> findRuleActivityList();
    
    List<Integer> findExistRuleIds();

    List<OprActActivity> findActivityByTmplId(@Param("tmplId") Integer tmplId);

    List<OprActActivity> findNoRuleActivities();

    /**
     * 查询活动列表并统计各审核状态的数量
     *
     * @param activity
     * @return
     */
    List<OprActActivity> getActivitiesWithAuditCount(OprActActivity activity);

    List<OprActRule> findRuleRate();

    OprActRule findActRuleByCode(@Param("tmplCode") String tmplCode);

    List<ActivityWaterCatDto> findWaterDetailList(
            @Param("accountId") Integer accountId,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("catId") Integer catId
    );

    List<OprActWaterBetdata> findWaterBetDateByTime(
            @Param("activityid") Integer activityid,
            @Param("waterstart") String waterstart,
            @Param("waterend") String waterend
    );

    int findActRuleCount(@Param("tmplCode") String tmplCode);

    int findSelfActRuleCount(OprActRule rule);

    OprActBonus findLastBonus(OprActBonus bonus);

    BigDecimal getMbrAllBonusAmount(@Param("accountId") Integer accountId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    List<OprActActivity> getClaimedActivities(@Param("accountId") Integer accountId);

    List<OprActActivity> getClaimedMixActivities(@Param("accountId") Integer accountId);

    Integer getTodyRedPacketRainCount( OprActBonus bonus);

    List<RebateFriendsRewardDto> friendRebateRewardList(@Param("loginName") String loginName, @Param("groupId") Integer groupId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    List<RebateFriendsDto> friendRebateList(@Param("loginName") String loginName, @Param("groupId") Integer groupId, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("firstChargeStartTime") String firstChargeStartTime,@Param("firstChargeEndTime") String firstChargeEndTime);

    List<RebateFriendsDetailsDto> friendRebateRewardDetails(@Param("loginName") String loginName,  @Param("startTime") String startTime, @Param("endTime") String endTime);

    List<RebateFriendsPersonalDto>  friendRebatePersonalList(@Param("loginName") String loginName);


    BigDecimal friendRebateRewardDetailsSummary(@Param("loginName") String loginName,  @Param("startTime") String startTime, @Param("endTime") String endTime);


}
