package com.wsdy.saasops.api.modules.activity.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.api.modules.activity.dto.FirstChargeRewardDto;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.activity.entity.MbrRebateFirstChargeReward;
import com.wsdy.saasops.modules.activity.mapper.MbrRebateFirstChargeRewardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FirstChargeApiService {
    @Autowired
    private MbrRebateFirstChargeRewardMapper mbrRebateFirstChargeRewardMapper;

    public PageUtils getApiFirstChargeList(Integer accountId, String startTime, String endTime,  Integer pageNo, Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        List<FirstChargeRewardDto> mbrRebateFirstChargeRewardList =  mbrRebateFirstChargeRewardMapper.getApiFirstChargeByAccountId(accountId, startTime, endTime);
        return BeanUtil.toPagedResult(mbrRebateFirstChargeRewardList);
    }
}
