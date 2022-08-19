package com.wsdy.saasops.api.modules.activity.mapper;

import com.wsdy.saasops.api.modules.activity.dto.HuPengFriendRewardSummaryDto;
import com.wsdy.saasops.api.modules.activity.dto.HuPengRebateRewardDto;
import com.wsdy.saasops.api.modules.activity.dto.HuPengRebateRewardListDto;
import com.wsdy.saasops.api.modules.activity.dto.HuPengSummaryDto;
import com.wsdy.saasops.modules.activity.dto.HuPengRewardDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiHuPengMapper {
     List<HuPengRewardDto> getApiHuPengRebateDtoList(String startTime, String endTime, Integer accountId, String subLoginName);

     List<HuPengRebateRewardDto>  getHuPengRebateRewardReportForDay(String startTime, String endTime, Integer accountId);

     List<HuPengRebateRewardDto>  getHuPengRebateRewardReportForMonth(String startTime, String endTime, Integer accountId);

     HuPengFriendRewardSummaryDto getHupengRebateRewardSummary(String startTime, String endTime, Integer accountId, String subLoginName);

     List<HuPengRebateRewardListDto> rewardList(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("accountId") Integer accountId);

     HuPengSummaryDto getHuPengSummary(Integer accountId);
}
