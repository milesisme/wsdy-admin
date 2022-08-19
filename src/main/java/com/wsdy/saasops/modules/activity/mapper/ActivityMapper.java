package com.wsdy.saasops.modules.activity.mapper;

import com.wsdy.saasops.modules.activity.dto.RebateHuPengDetailsDto;
import com.wsdy.saasops.modules.activity.dto.RebateHuPengFriendsRewardDto;
import com.wsdy.saasops.modules.activity.dto.RebateHuPengRewardDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ActivityMapper {

     List<RebateHuPengRewardDto> huPengRebateRewardList(String loginName, Integer groupId, String startTime, String endTime);

     List<RebateHuPengDetailsDto> huPengRebateRewardDetails(String loginName, String startTime, String endTime);

     BigDecimal huPengRebateRewardDetailsSummary(@Param("loginName") String loginName, @Param("startTime") String startTime, @Param("endTime") String endTime);

     List<RebateHuPengFriendsRewardDto> huPengFriendsRebateRewardList(String loginName, Integer groupId, String startTime, String endTime);

     Integer getMbrHupengFriendsCount(Integer accountId, Integer subAccountId ,Integer activityId,  String incomeDay);


     Integer getMbrHupengFriendsRewardCount(Integer accountId,Integer activityId, String incomeDay);

}
