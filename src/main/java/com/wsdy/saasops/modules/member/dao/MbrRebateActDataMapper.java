package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrRebateActData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MbrRebateActDataMapper  extends MyMapper<MbrRebateActData> {
    MbrRebateActData findFriendRebateVipRewardData(Integer accountId, Integer subAccountId, Integer activityId, Integer dataType);

    List<MbrRebateActData> findFriendRebateChargeRewardData(Integer accountId, Integer activityId, Integer dataType);
}
