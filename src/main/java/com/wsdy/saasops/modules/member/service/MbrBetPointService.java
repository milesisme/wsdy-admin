package com.wsdy.saasops.modules.member.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.agent.service.AgentComReportService;
import com.wsdy.saasops.modules.analysis.mapper.AnalysisMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountOtherMapper;
import com.wsdy.saasops.modules.member.dao.MbrFundsReportMapper;
import com.wsdy.saasops.modules.member.dao.MbrGroupMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAccountOther;
import com.wsdy.saasops.modules.member.entity.MbrFundsReport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 投注比计算任务
 */
@Slf4j
@Service
public class MbrBetPointService {

    @Autowired
    private RedisService redisService;
    @Autowired
    private AnalysisMapper analysisMapper;
    @Autowired
    private MbrAccountOtherMapper mbrAccountOtherMapper;
    @Autowired
    private MbrAccountService mbrAccountService;


    public void countBetPoint(String siteCode, Integer passDay){
        log.info("countBetPoint==" + siteCode + "==start" );
        String endTime = DateUtil.getCurrentDate(DateUtil.FORMAT_10_DATE).concat(" 23:59:59");
        String startTime = DateUtil.getPastDate(passDay, DateUtil.FORMAT_10_DATE).concat(" 00:00:00");
        log.info("countBetPoint==" + siteCode + "==获取时间{} - {}", startTime, endTime);
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        Long beforeSystemTime = System.currentTimeMillis();
        // 加锁，避免等幂删除和新增冲突
        String key = RedisConstants.MBR_BET_POINT_COUNT + siteCode;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 30, TimeUnit.MINUTES);
        try {
            if (Boolean.TRUE.equals(isExpired)) {
                // 获取指定天数内有存款，优惠，投注的会员
                List<String> betUsername = analysisMapper.getBetPointUsernameByDate(startTime, endTime);
                log.info("countBetPoint==" + siteCode + "==获取{}天内的更新会员数{}", passDay, betUsername.size());
                // 计算玩家投注比
                List<MbrAccountOther> betPointUser = analysisMapper.getBetPointByUsername(betUsername);
                for (MbrAccountOther other : betPointUser) {
                    MbrAccountOther exist = mbrAccountOtherMapper.selectOne(new MbrAccountOther(){{
                        setLoginName(other.getLoginName());
                    }});
                    if (exist == null) {
                        log.info("countBetPoint==" + siteCode + "==发现不存在other表数据的会员{}", other.getLoginName());
                        MbrAccount account = mbrAccountService.getAccountInfo(other.getLoginName());
                        other.setAccountId(account.getId());
                        mbrAccountOtherMapper.insert(other);
                    } else {
                        other.setId(exist.getId());
                        mbrAccountOtherMapper.updateByPrimaryKeySelective(other);
                    }
                }
            }
        } catch (Exception e){
            log.error("countBetPoint异常=="+ siteCode, e);
        } finally {
            redisService.del(key);
        }
        log.info("countBetPoint==" +siteCode + "==end==用时{}毫秒", (System.currentTimeMillis() - beforeSystemTime));
    }

    public static void main(String[] args) {
        System.out.println(DateUtil.getPastDate(2, DateUtil.FORMAT_10_DATE));
    }
}
