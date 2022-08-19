package com.wsdy.saasops.api.modules.user.mapper;


import com.wsdy.saasops.api.modules.user.dto.*;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrRebateReportNew;
import com.wsdy.saasops.modules.member.entity.MbrTree;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ApiPromotionMapper {

	BigDecimal findYestodayRebates(@Param("accountId") Integer accountId);

	BigDecimal findTotalRebates(@Param("accountId") Integer accountId);

	BigDecimal findTotalResult(@Param("accountId") Integer accountId, @Param("depth") Integer depth);

	List<RebateAccountDto> recentlyActive(@Param("accountId") Integer accountId, @Param("depth") Integer depth);

	int findActiveUserCount(MbrRebateReportNew rebateReport);

	//BigDecimal findTotalBalance(MbrRebateReport rebateReport);

	BigDecimal findValidBetTotal(RebateAccountDto rebateAccountDto);

	RebateAccountDto rebateInfo(@Param("accountId") Integer accountId);

	BigDecimal getRebateTotalByDepth(MbrRebateReportNew rebateReport);

	List<MbrAccount> findParentInfo(MbrAccount mbrAccount);

	List<MbrAccount> findChildsInfo(MbrAccount mbrAccount);

	Integer getMbrTreeDepth(MbrAccount mbrAccount);

	List<RebateAccSanGongDto> getSubAccRebateRatio(RebateAccSanGongDto rebateAccSanGongDto);

	MbrTree  verifyMbrRelation(@Param("childNodeId") Integer childId, @Param("parentId") Integer parentId);

	RebateAccSanGongSumDto getSubAccRebateSum(RebateAccSanGongSumDto rebateAccSanGongSumDto);

	List<RebateAccSanGongDetailDto> getSubAccRebateDetail(RebateAccSanGongDetailDto rebateAccSanGongDetailDto);

	Integer getChildCount(@Param("accountId") Integer accountId);

	BigDecimal getTotalRebateForParent(RebateAccSanGongSumDto rebateAccSanGongSumDto);


	List<FriendRebateDto> getApiFriendRebateDtoList(@Param("firstChargeStartTime") String firstChargeStartTime, @Param("firstChargeEndTime") String firstChargeEndTime, @Param("startTime") String startTime, @Param("endTime")String endTime, @Param("accountId")Integer accountId, @Param("subLoginName")String subLoginName, @Param("showAll") Integer showAll);


	FriendRebateDto getApiFriendRebateDtoDetails(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("loginName")String loginName, @Param("subLoginName")String subLoginName);

	List<FriendRebateRewardDto> getFriendRebateRewardReportForDay(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("accountId")Integer accountId);

	List<FriendRebateRewardDto> getFriendRebateRewardReportForMonth(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("accountId")Integer accountId);

	FriendRebateAccountDto getFriendRebateAccountInfo(@Param("loginName") String loginName);


	List<FriendRebateSumDto> getFriendRebateSummary(@Param("accountId") Integer accountId);


	FriendRebatePersonalRewardSummaryDto getFriendRebatePersonalRewardSummary(@Param("accountId") Integer accountId, @Param("startTime")String startTime, @Param("endTime")String endTime);


	FriendRebateFriendRewardSummaryDto getFriendRebateFriendsRewardSummary(@Param("firstChargeStartTime") String firstChargeStartTime, @Param("firstChargeEndTime") String firstChargeEndTime,  @Param("accountId")Integer accountId, @Param("startTime")String startTime, @Param("endTime")String endTime, @Param("subLoginName")String subLoginName);

	List<FriendRebateRewardListDto> rewardList( @Param("startTime")String startTime, @Param("endTime")String endTime, @Param("accountId") Integer accountId);

}
