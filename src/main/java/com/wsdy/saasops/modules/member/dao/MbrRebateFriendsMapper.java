package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrRebateFriends;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MbrRebateFriendsMapper extends MyMapper<MbrRebateFriends> {
    Integer getMbrRebateFriendsCount(@Param("subAccountId") Integer subAccountId, @Param("activityId")Integer activityId, @Param("calcDay")String calcDay , @Param("type")Integer type );
}
