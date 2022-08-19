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
        // 加锁，避免等幂删除和新增冲突
        String key = RedisConstants.MBR_FUNDS_REPORT_COUNT_MONTH + siteCode;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 30, TimeUnit.MINUTES);
        try {
            if (Boolean.TRUE.equals(isExpired)) {
                if (StringUtils.isNotBlank(month) && month.length() == 7) {
                    // 如果是输入的月份，则循环计算整月，用于重新计算
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
        // 加锁，避免等幂删除和新增冲突
        String key = RedisConstants.MBR_FUNDS_REPORT_COUNT_DAY + siteCode;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 6, TimeUnit.MINUTES);
        try {
            if (Boolean.TRUE.equals(isExpired)) {
                if (StringUtils.isNotBlank(date) && date.length() == 7) {
                    // 如果是输入的月份，则循环计算整月，用于重新计算
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
            log.error("countDailyMbrFundsReport异常", e);
        } finally {
            redisService.del(key);
        }
        log.info("countDailyMbrFundsReport==" +siteCode + "==end" );
    }

    private void doCount (String siteCode, String date, Long beforeSystemTime) {
        String excuteDate = "";

        if (StringUtils.isBlank(date)) {
            // 每日前10分钟依然更新昨日, 避免漏掉统计
            String nowDate = DateUtil.getCurrentDate(DateUtil.FORMAT_10_DATE);
            Date standardDate = DateUtil.parse(nowDate.concat(" 00:10:00"), DateUtil.FORMAT_18_DATE_TIME);
            if (new Date().before(standardDate)) {
                log.info("countDailyMbrFundsReport==" + siteCode + "==当前是每日前10分钟，更新昨日");
                nowDate = DateUtil.format(DateUtil.getDateBefore(new Date(), 1), DateUtil.FORMAT_10_DATE);
            }
            log.info("countDailyMbrFundsReport==" + siteCode + "==更新日期"+ nowDate);
            excuteDate = nowDate;
        } else {
            excuteDate = date;
            log.info("countDailyMbrFundsReport==" + siteCode + "==更新指定日期"+ excuteDate);
        }
        MbrFundsReport model = new MbrFundsReport();
        model.setReportDate(excuteDate);
        List<MbrFundsReport> mbrFundsReportList = mbrFundsReportMapper.countMbrFundsReport(model);
        log.info("countDailyMbrFundsReport==" + siteCode + "==更新日期"+ excuteDate + "==本次更新总用户数"+ mbrFundsReportList.size());

        for (MbrFundsReport report : mbrFundsReportList) {
            report.setLastupdate(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));

            MbrFundsReport existModel = mbrFundsReportMapper.getMbrTodayReport(report);
            if (Objects.nonNull(existModel) && report.getReportDate().equals(report.getAuditDate())) {
                // 如果报表统计日期与审核日期相同，则全部更新数据
                report.setId(existModel.getId());
                mbrFundsReportMapper.updateByPrimaryKeySelective(report);
            } else if (Objects.nonNull(existModel) && !report.getReportDate().equals(report.getAuditDate())) {
                log.info("countDailyMbrFundsReport==" + siteCode + "==更新日期"+ excuteDate + "==发现跨天数据，申请{}审核{}用户{}",
                        report.getReportDate(), report.getAuditDate(), report.getAccountId());
                // 如果报表统计日期与审核日期不同，则重新计算申请日期当天的优惠
                MbrFundsReport countReportDayData = mbrFundsReportMapper.countMbrFundsReportByAccountId(report);
                log.info("countDailyMbrFundsReport==" + siteCode + "==更新日期"+ excuteDate + "==发现跨天数据，用户{}申请{}新数据{}报表数据{}",
                        report.getAccountId(), report.getReportDate(), JSON.toJSONString(countReportDayData), JSON.toJSONString(existModel));

                existModel.setBonus(countReportDayData.getBonus());
                existModel.setOnlineBonus(countReportDayData.getOnlineBonus());
                existModel.setOfflineBonus(countReportDayData.getOfflineBonus());
                mbrFundsReportMapper.updateByPrimaryKeySelective(existModel);
            } else {
                // 当所有数据为0时，不进行新增
                if (report.getDeposit().doubleValue()==0 && report.getWithdraw().doubleValue()==0 && report.getActualDeposit().doubleValue()==0 &&
                        report.getBonus().doubleValue()==0 && report.getOnlineBonus().doubleValue()==0 && report.getOfflineBonus().doubleValue()==0 &&
                        report.getTaskBonus().doubleValue()==0) {
                    // do nothing
                } else {
                    mbrFundsReportMapper.insertSelective(report);
                }
            }

        }
        log.info("countDailyMbrFundsReport==" + siteCode + "==更新日期"+ excuteDate + "==更新完成，用时"+ (System.currentTimeMillis() - beforeSystemTime) + "毫秒");
    }
}
