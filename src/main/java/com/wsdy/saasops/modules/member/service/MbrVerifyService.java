package com.wsdy.saasops.modules.member.service;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.MD5;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.VerifyDepositMapper;
import com.wsdy.saasops.modules.member.dao.VerifyWarnMapper;
import com.wsdy.saasops.modules.member.entity.VerifyDeposit;
import com.wsdy.saasops.modules.member.entity.VerifyWarn;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Service
public class MbrVerifyService {

    @Value("${deposit.secretkey}")
    private String secretkey;
    @Autowired
    private VerifyDepositMapper verifyDepositMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private VerifyWarnMapper verifyWarnMapper;

    @Transactional
    public void addMbrVerifyDeposit(FundDeposit deposit, String siteCode) {
       /* VerifyDeposit verifyDeposit = setMbrVerifyDeposit(deposit, DateUtil.getCurrentDate(FORMAT_18_DATE_TIME), siteCode);

        VerifyDeposit verifyDeposit1 = new VerifyDeposit();
        verifyDeposit1.setDepositId(deposit.getId());
        verifyDeposit1.setSiteCode(siteCode);
        int count = verifyDepositMapper.selectCount(verifyDeposit1);
        if (count == 0) {
            verifyDepositMapper.insert(verifyDeposit);
        }*/
    }

    public void castDatasecret(String siteCode) {
        int count = redisService.findLikeRedis(RedisConstants.ACCOUNT_VERIFYSERCRET + siteCode + "*");
        if (count == 0) {
            List<FundDeposit> fundDeposits = fundMapper.findVerifyDeposit();
            if (Collections3.isNotEmpty(fundDeposits)) {
                fundDeposits.stream().forEach(fs -> {
                    verifyFundDeposit(fs, siteCode);
                });
            }
        }
    }

    @Transactional
    @Async("verifyFundDepositAsyncExecutor")
    public void verifyFundDeposit(FundDeposit deposit, String siteCode) {
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        String key = RedisConstants.ACCOUNT_VERIFYSERCRET + siteCode + deposit.getId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, deposit.getId() + StringUtils.EMPTY, 2, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isExpired)) {
            VerifyDeposit verifyDeposit = fundMapper.findSecretDepositOne(deposit.getId(), siteCode);
            if (nonNull(verifyDeposit)) {
                VerifyDeposit verifyDeposit2 = setMbrVerifyDeposit(deposit, verifyDeposit.getCreatetime(), siteCode);
                if (!verifyDeposit2.getDatasecret().equalsIgnoreCase(verifyDeposit.getDatasecret())) {
                    insertVerifyWarn(deposit, verifyDeposit.getAccountId(), verifyDeposit.getId(), siteCode);
                }
            }
            if (isNull(verifyDeposit)) {
                if (DateUtil.timeCompare("2021-10-02 20:50:00", deposit.getAuditTime(), DateUtil.FORMAT_18_DATE_TIME) == -1) {
                    insertVerifyWarn(deposit, null, null, siteCode);
                }
            }
            redisService.del(key);
        }
    }

    public void insertVerifyWarn(FundDeposit deposit, Integer accountId, Integer verifydepositid, String siteCode) {
        VerifyWarn verifyWarn1 = new VerifyWarn();
        verifyWarn1.setDepositid(deposit.getId());
        verifyWarn1.setSiteCode(siteCode);
        int count = verifyWarnMapper.selectCount(verifyWarn1);
        if (count == 0) {
            VerifyWarn verifyWarn = new VerifyWarn();
            verifyWarn.setCreatetime(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
            verifyWarn.setFromaccountid(accountId);
            verifyWarn.setToaccountid(deposit.getAccountId());
            verifyWarn.setVerifydepositid(verifydepositid);
            verifyWarn.setDepositid(deposit.getId());
            verifyWarn.setSiteCode(siteCode);
            verifyWarnMapper.insert(verifyWarn);
        }
    }

    private VerifyDeposit setMbrVerifyDeposit(FundDeposit deposit, String createtime, String siteCode) {
        VerifyDeposit verifyDeposit = new VerifyDeposit();
        verifyDeposit.setAccountId(deposit.getAccountId());
        verifyDeposit.setCreatetime(createtime);
        verifyDeposit.setMark(deposit.getMark());
        if (StringUtils.isNotEmpty(deposit.getCreateTime())) {
            verifyDeposit.setDepositCreatetime(deposit.getCreateTime().substring(0, 19));
        }
        verifyDeposit.setDepositAmount(deposit.getDepositAmount());
        verifyDeposit.setDepositId(deposit.getId());
        verifyDeposit.setOrderNo(deposit.getOrderNo());
        verifyDeposit.setSiteCode(siteCode);

        String amount = deposit.getDepositAmount().stripTrailingZeros().toPlainString();
        String str = deposit.getAccountId() + createtime + deposit.getMark() + verifyDeposit.getDepositCreatetime() +
                amount + deposit.getId() + deposit.getOrderNo() + siteCode + secretkey;
        String sign = MD5.getMD5(str);
        verifyDeposit.setDatasecret(sign);
        return verifyDeposit;
    }
}
