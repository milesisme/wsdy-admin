package com.wsdy.saasops.modules.analysis.service;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.analysis.dto.RptBetRcdDayDto;
import com.wsdy.saasops.modules.analysis.mapper.TurnoverRateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


/**
 *		job计算平台流水费率
 */
@Slf4j
@Service
public class TurnoverRateService {

    @Autowired
    private TurnoverRateMapper turnoverRateMapper;

    public void castAccountRate(String siteCode) {
        log.info("开始计算平台流水费率" + siteCode);
        List<RptBetRcdDayDto> rcdDayDtos = turnoverRateMapper.findRptBetRcdDayRateList(2000);
        rcdDayDtos.stream().forEach(rs -> {
            updateRptBetRcdDayCost(rs, siteCode);
        });
    }

    @Transactional
    @Async("updateRptBetRcdDayCostAsyncExecutor")
    public void updateRptBetRcdDayCost(RptBetRcdDayDto rs, String siteCode) {
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        BigDecimal waterrate = turnoverRateMapper.findSetGameRate(rs.getPlatform(), rs.getGametype());
        if (Objects.isNull(waterrate)) {
            waterrate = turnoverRateMapper.findDepotRate(Constants.depotCatGameTypeMap.get(rs.getGamecategory()), rs.getPlatform());
        }
        if (Objects.isNull(waterrate)) {
            turnoverRateMapper.updateRptBetRcdDayCost(BigDecimal.ZERO, Constants.EVNumber.one, BigDecimal.ZERO, rs.getId());
        } else {
            BigDecimal cost = CommonUtil.adjustScale(rs.getValidbet().multiply(waterrate.divide(new BigDecimal(Constants.ONE_HUNDRED))));
            turnoverRateMapper.updateRptBetRcdDayCost(cost, Constants.EVNumber.one, waterrate, rs.getId());
        }
    }

}