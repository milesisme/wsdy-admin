package com.wsdy.saasops.modules.activity.mapper;

import com.wsdy.saasops.api.modules.activity.dto.FirstChargeRewardDto;
import com.wsdy.saasops.modules.activity.entity.MbrRebateFirstChargeReward;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MbrRebateFirstChargeRewardMapper extends MyMapper<MbrRebateFirstChargeReward> {
    int  findMbrRebateFirstChargeReward(MbrRebateFirstChargeReward mbrRebateFirstChargeReward);


    int findMbrRebateFirstChargeRewardCount (MbrRebateFirstChargeReward mbrRebateFirstChargeReward);


    int updateMbrRebateFirstChargeBilldIdAndAuditIdById(MbrRebateFirstChargeReward mbrRebateFirstChargeReward);


    List<FirstChargeRewardDto> getApiFirstChargeByAccountId(Integer accountId, String startTime, String endTime );
}
