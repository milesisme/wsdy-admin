package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrRebateFriendsReward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MbrRebateFriendsRewardMapper extends MyMapper<MbrRebateFriendsReward> {

    Integer getMbrRebateFriendsRewardCount(@Param("accountId") Integer accountId, @Param("activityId")Integer activityId, @Param("calcDay")String calcDay , @Param("type")Integer type,  @Param("content")String content);
}
