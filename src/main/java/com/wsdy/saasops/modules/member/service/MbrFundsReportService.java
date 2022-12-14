package com.wsdy.saasops.modules.member.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.agent.service.AgentComReportService;
import com.wsdy.saasops.modules.member.dao.MbrGroupMapper;
import com.wsdy.saasops.modules.member.entity.MbrFundsReport;
import com.wsdy.saasops.modules.member.dao.MbrFundsReportMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MbrFundsReportService {

    @Autowired
    private RedisService redisService;
    @Autowired
    private MbrAccountDeviceService mbrAccountDeviceService;
    @Autowired
    private MbrGroupMapper mbrGroupMapper;
    @Autowired
    private AgentComReportService agentComReportService;
    @Autowired
    private MbrFundsReportMapper mbrFundsReportMapper;

    /*public static void main(String[] args) throws Exception{
        String date = "2022-02";
        Date dateMonth = new SimpleDateFormat(DateUtil.FORMAT_6_DATE, Locale.CHINA).parse(date);
        Calendar ac = Calendar.getInstance(Locale.CHINA);
        ac.setTime(dateMonth);
        Integer lastDay = ac.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i=0; i <= lastDay; i++) {
            String forDate = date.concat(String.format("-%02d", i));
            System.out.println(forDate);
        }
    }*/

    public void countMonthlyMbrFundsReport(String siteCode){
        log.info("countMonthlyMbrFundsReport==" + siteCode + "==start" );
        String month = DateUtil.getCurrentDate(DateUtil.FORMAT_6_DATE);
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        Long beforeSystemTime = System.currentTimeMillis();
        // ??????????????????????????????????????????
        String key = RedisConstants.MBR_FUNDS_REPORT_COUNT_MONTH + siteCode;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 30, TimeUnit.MINUTES);
        try {
            if (Boolean.TRUE.equals(isExpired)) {
                if (StringUtils.isNotBlank(month) && month.length() == 7) {
                    // ?????????????????????????????????????????????????????????????????????
                    Date dateMonth = new SimpleDateFormat(DateUtil.FORMAT_6_DATE, Locale.CHINA).parse(month);
                    Calendar ac = Calendar.getInstance(Locale.CHINA);
                    ac.setTime(dateMonth);
                    Integer lastDay = ac.getActualMaximum(Calendar.DAY_OF_MONTH);
                    for (int i=1; i <= lastDay; i++) {
                        String forDate = month.concat(String.format("-%02d", i));
                        doCount(siteCode, forDate, beforeSystemTime);
                    }
                }
            }
        } catch (Exception e){
            log.error("countMonthlyMbrFundsReport", e);
        } finally {
            redisService.del(key);
        }
        log.info("countMonthlyMbrFundsReport==" +siteCode + "==end" );
    }

    public void countDailyMbrFundsReport(String siteCode, String date){
        log.info("countDailyMbrFundsReport==" + siteCode + "==start" );
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        Long beforeSystemTime = System.currentTimeMillis();
        // ??????????????????????????????????????????
        String key = RedisConstants.MBR_FUNDS_REPORT_COUNT_DAY + siteCode;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 6, TimeUnit.MINUTES);
        try {
            if (Boolean.TRUE.equals(isExpired)) {
                if (StringUtils.isNotBlank(date) && date.length() == 7) {
                    // ?????????????????????????????????????????????????????????????????????
                    Date dateMonth = new SimpleDateFormat(DateUtil.FORMAT_6_DATE, Locale.CHINA).parse(date);
                    Calendar ac = Calendar.getInstance(Locale.CHINA);
                    ac.setTime(dateMonth);
                    Integer lastDay = ac.getActualMaximum(Calendar.DAY_OF_MONTH);
                    for (int i=1; i <= lastDay; i++) {
                        String forDate = date.concat(String.format("-%02d", i));
                        doCount(siteCode, forDate, beforeSystemTime);
                    }
                } else {
                    doCount(siteCode, date, beforeSystemTime);
                }
            }
        } catch (Exception e){
            log.error("countDailyMbrFundsReport??????", e);
        } finally {
            redisService.del(key);
        }
        log.info("countDailyMbrFundsReport==" +siteCode + "==end" );
    }

    private void doCount (String siteCode, String date, Long beforeSystemTime) {
        String excuteDate = "";

        if (StringUtils.isBlank(date)) {
            // ?????????10????????????????????????, ??????????????????
            String nowDate = DateUtil.getCurrentDate(DateUtil.FORMAT_10_DATE);
            Date standardDate = DateUtil.parse(nowDate.concat(" 00:10:00"), DateUtil.FORMAT_18_DATE_TIME);
            if (new Date().before(standardDate)) {
                log.info("countDailyMbrFundsReport==" + siteCode + "==??????????????????10?????????????????????");
                nowDate = DateUtil.format(DateUtil.getDateBefore(new Date(), 1), DateUtil.FORMAT_10_DATE);
            }
            log.info("countDailyMbrFundsReport==" + siteCode + "==????????????"+ nowDate);
            excuteDate = nowDate;
        } else {
            excuteDate = date;
            log.info("countDailyMbrFundsReport==" + siteCode + "==??????????????????"+ excuteDate);
        }
        MbrFundsReport model = new MbrFundsReport();
        model.setReportDate(excuteDate);
        List<MbrFundsReport> mbrFundsReportList = mbrFundsReportMapper.countMbrFundsReport(model);
        log.info("countDailyMbrFundsReport==" + siteCode + "==????????????"+ excuteDate + "==????????????????????????"+ mbrFundsReportList.size());

        for (MbrFundsReport report : mbrFundsReportList) {
            report.setLastupdate(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));

            MbrFundsReport existModel = mbrFundsReportMapper.getMbrTodayReport(report);
            if (Objects.nonNull(existModel) && report.getReportDate().equals(report.getAuditDate())) {
                // ?????????????????????????????????????????????????????????????????????
                report.setId(existModel.getId());
                mbrFundsReportMapper.updateByPrimaryKeySelective(report);
            } else if (Objects.nonNull(existModel) && !report.getReportDate().equals(report.getAuditDate())) {
                log.info("countDailyMbrFundsReport==" + siteCode + "==????????????"+ excuteDate + "==???????????????????????????{}??????{}??????{}",
                        report.getReportDate(), report.getAuditDate(), report.getAccountId());
                // ??????????????????????????????????????????????????????????????????????????????????????????
                MbrFundsReport countReportDayData = mbrFundsReportMapper.countMbrFundsReportByAccountId(report);
                log.info("countDailyMbrFundsReport==" + siteCode + "==????????????"+ excuteDate + "==???????????????????????????{}??????{}?????????{}????????????{}",
                        report.getAccountId(), report.getReportDate(), JSON.toJSONString(countReportDayData), JSON.toJSONString(existModel));

                existModel.setBonus(countReportDayData.getBonus());
                existModel.setOnlineBonus(countReportDayData.getOnlineBonus());
                existModel.setOfflineBonus(countReportDayData.getOfflineBonus());
                mbrFundsReportMapper.updateByPrimaryKeySelective(existModel);
            } else {
                // ??????????????????0?????????????????????
                if (report.getDeposit().doubleValue()==0 && report.getWithdraw().doubleValue()==0 && report.getActualDeposit().doubleValue()==0 &&
                        report.getBonus().doubleValue()==0 && report.getOnlineBonus().doubleValue()==0 && report.getOfflineBonus().doubleValue()==0 &&
                        report.getTaskBonus().doubleValue()==0) {
                    // do nothing
                } else {
                    mbrFundsReportMapper.insertSelective(report);
                }
            }

        }
        log.info("countDailyMbrFundsReport==" + siteCode + "==????????????"+ excuteDate + "==?????????????????????"+ (System.currentTimeMillis() - beforeSystemTime) + "??????");
    }
}
