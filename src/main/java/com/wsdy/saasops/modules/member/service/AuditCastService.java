package com.wsdy.saasops.modules.member.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.analysis.entity.RptBetModel;
import com.wsdy.saasops.modules.analysis.entity.RptBetTotalModel;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.base.mapper.BaseMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrAuditAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrAuditBonusMapper;
import com.wsdy.saasops.modules.member.dao.MbrAuditFraudMapper;
import com.wsdy.saasops.modules.member.dto.BounsValidBetDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAuditAccount;
import com.wsdy.saasops.modules.member.entity.MbrAuditBonus;
import com.wsdy.saasops.modules.member.entity.MbrAuditFraud;
import com.wsdy.saasops.modules.member.mapper.AuditMapper;
import com.wsdy.saasops.modules.operate.dao.TGmCatMapper;
import com.wsdy.saasops.modules.operate.dao.TGmDepotMapper;
import com.wsdy.saasops.modules.operate.entity.TGmCat;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.service.TGmDepotService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static com.wsdy.saasops.modules.member.service.AccountWaterCastService.HHMMSS;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class AuditCastService {

    @Autowired
    private AuditMapper auditMapper;
    @Autowired
    private MbrAuditAccountMapper auditAccountMapper;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private TGmCatMapper gmCatMapper;
    @Autowired
    private MbrAuditFraudMapper auditFraudMapper;
    @Autowired
    private MbrAuditBonusMapper auditBonusMapper;
    @Autowired
    private TGmDepotService depotService;
    @Autowired
    private TGmDepotMapper depotMapper;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private ElasticSearchConnection_Read connection;
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private RedisService redisService;


    public List<Integer> findAuditAccountIds(Integer accountId) {
        return auditMapper.findAuditAccountId(accountId);
    }

    @Async("auditCastExecutor")
    public void doingCronAuditAccount(String siteCode, Integer id, List<String> sitePrefix, Boolean isCastAudit) {
        String key = RedisConstants.AUDIT_ACCOUNT_SIGN + siteCode + id;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, id+StringUtils.EMPTY, 10, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isExpired)) {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            MbrAccount account = accountMapper.selectByPrimaryKey(id);
            log.info("??????==siteCode==" + siteCode + "==accountId==" + account.getId() + "==loginName==" + account.getLoginName() + "==??????");
            // 1.??????2?????????????????????
            String startTime = formatEsDate(getPastDate(1, FORMAT_10_DATE) + HHMMSS);
            String endTime = formatEsDate(getPastDate(-1, FORMAT_10_DATE) + HHMMSS);
            BigDecimal bigDecimal = accountValidBet(sitePrefix, account.getLoginName(), startTime, endTime);
            log.info("??????==siteCode==" + siteCode + "==accountId==" + account.getId() + "==loginName==" + account.getLoginName() + "==bigDecimal==" + bigDecimal);
            // 2.???????????????2??????????????????????????????
            if (bigDecimal.compareTo(BigDecimal.ZERO) == 1 || Boolean.FALSE.equals(isCastAudit)) {
                // 3. ??????????????????mbr_audit_bonus????????????????????????,betDto??????
                BounsValidBetDto betDto = updateAuditBonus(account, null, siteCode);
                // 4. ????????????mbr_audit_bonus ?????????????????? mbr_audit_account????????????????????????
                transferBonus(betDto.getAuditBonuses());
                // 5. ?????????????????? betDto??????
                updateAuditAccount(account, siteCode, betDto);
            }
            redisService.del(key);
            log.info("??????==siteCode==" + siteCode + "==accountId==" + account.getId() + "==loginName==" + account.getLoginName() + "==??????");
        }
    }

    private void transferBonus(List<MbrAuditBonus> auditBonuses) {
        if (Collections3.isNotEmpty(auditBonuses)) {
            Map<Integer, List<MbrAuditBonus>> auditBonusGroupingBy =
                    auditBonuses.stream().collect(
                            Collectors.groupingBy(
                                    MbrAuditBonus::getDepotId));
            for (Integer depotIdKey : auditBonusGroupingBy.keySet()) {
                List<MbrAuditBonus> auditBonusList = auditBonusGroupingBy.get(depotIdKey);
                long counValid = auditBonusList.stream().filter(p ->
                                Constants.EVNumber.zero == p.getIsValid())
                        .map(MbrAuditBonus::getId).count();
                long couStatusnt = auditBonusList.stream().filter(p ->
                                Constants.EVNumber.zero == p.getStatus())
                        .map(MbrAuditBonus::getId).count();
                if (counValid == Constants.EVNumber.zero
                        && couStatusnt == Constants.EVNumber.zero) {
                    auditBonusList.stream().forEach(b -> {
                        b.setIsDrawings(Constants.EVNumber.one);
                        b.setTransferTime(getCurrentDate(FORMAT_18_DATE_TIME));
                        auditBonusMapper.updateByPrimaryKey(b);
                    });
                    auditAccountService.addOrUpdateMbrAuditHistory(
                            auditBonusList.get(auditBonusList.size() - 1),
                            auditBonusList.get(0).getTime());
                }
            }
        }
    }

    /**
     *  ???????????????????????????
     * @param account       ??????
     * @param siteCode      ??????
     * @param validBetDto   betDto??????
     * @return
     */
    private List<MbrAuditAccount> updateAuditAccount(MbrAccount account, String siteCode, BounsValidBetDto validBetDto) {
        log.info("??????==siteCode==" + siteCode + "==accountId==" + account.getId() + "==loginName==" + account.getLoginName() + "==updateAuditAccount==??????");
        // 1.???????????????????????????/????????????????????????????????????list???????????????
        List<MbrAuditAccount> auditAccounts = updateValidBet(account.getId(), siteCode, validBetDto);
        if (Collections3.isNotEmpty(auditAccounts)) {
            // 2.??????????????????
            updateAccountAudit(auditAccounts);
        }
        log.info("??????==siteCode==" + siteCode + "==accountId==" + account.getId() + "==loginName==" + account.getLoginName() + "==updateAuditAccount==??????");
        return auditAccounts;
    }

    private List<MbrAuditAccount> updateValidBet(Integer accountId, String siteCode, BounsValidBetDto validBetDto) {
        log.info("??????==siteCode==" + siteCode + "==accountId==" + accountId + "==updateValidBet==??????");
        // 1. ?????????????????????????????????????????????????????????
        MbrAuditAccount accountAudit = new MbrAuditAccount();
        accountAudit.setIsDrawings(Constants.EVNumber.zero);
        accountAudit.setSort(Boolean.TRUE);
        accountAudit.setAccountId(accountId);
        List<MbrAuditAccount> audits = auditMapper.finAuditList(accountAudit);
        Long bounsCount = null;
        if (audits.size() > 0) {
            // ????????????????????????????????????(????????????)???????????????
            bounsCount = audits.stream().filter(p ->
                            nonNull(p.getRuleId()) && p.getStatus() == 0)
                    .map(MbrAuditAccount::getId).count();
        }
        // 2.??????????????????????????????????????????????????????????????????audits ???????????????/??????
        for (int i = 0; i < audits.size(); i++) {
            MbrAuditAccount audit = audits.get(i);
            // 2.1 ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            RptBetModel rptBetModel = getValidBet(audit, audits, i, siteCode, bounsCount);
            // 2.2 ???????????????????????????????????????
            audit.setStatus(Constants.EVNumber.zero);   // ?????????0?????????
            audit.setIsOut(Constants.EVNumber.zero);    // ??????????????? ?????????
            audit.setValidBet(BigDecimal.ZERO);         // ????????????
            audit.setPayOut(BigDecimal.ZERO);           // ??????
            audit.setDepositBalance(BigDecimal.ZERO);   // ????????????
            if (nonNull(rptBetModel)) {
                audit.setValidBet(nonNull(rptBetModel.getValidBet()) ? rptBetModel.getValidBet() : BigDecimal.ZERO);
                audit.setPayOut(nonNull(rptBetModel.getPayout()) ? rptBetModel.getPayout() : BigDecimal.ZERO);
                audit.setDepositBalance(audit.getPayOut()); // ?????????????????????????????????
            }
        }
        // 3. ????????????????????????????????????????????????
        if (Collections3.isNotEmpty(audits)) {
            Collections.reverse(audits);
        }
        // 4. ??????????????????????????????????????????????????????????????????bonusRemainValidBet??????0???????????????validBet?????????
        //  -- 4????????????????????????????????????????????????mbr_audit_bonus
        for (int i = 0; i < audits.size(); i++) {
            MbrAuditAccount audit = audits.get(i);
            audit.setBonusRemainValidBet(getBonusRemainValidBet(validBetDto, audit));   // validBetDto size???0?????????bonusRemainValidBet ???0
            audit.setValidBet(audit.getValidBet().add(
                    nonNull(audit.getBonusRemainValidBet())
                            ? audit.getBonusRemainValidBet() : BigDecimal.ZERO));
            auditAccountMapper.updateByPrimaryKey(audit);
        }
        log.info("??????==siteCode==" + siteCode + "==accountId==" + accountId + "==updateValidBet==??????");
        return audits;
    }

    private void updateAccountAudit(List<MbrAuditAccount> accountAudits) {
        // ????????????????????????
        MbrAuditAccount remainValidBet = new MbrAuditAccount();
        remainValidBet.setRemainValidBet(BigDecimal.ZERO);  // ?????????

        // 1.????????????????????????
        accountAudits.forEach(
                st -> castAuditAccout(st, remainValidBet)
        );

        // 2.???????????????????????????????????????????????? ??????+??????-->????????????????????????0??????????????????-->??????????????????
        // ????????????????????????
        long count = accountAudits.stream().filter(p ->
                        Constants.EVNumber.zero == p.getStatus())
                .map(MbrAuditAccount::getId).count();
        if (count > 0) {
            castDepositBalance(accountAudits);
        }
    }

    private List<String> getDepotCode(Integer accountId, String endTime, List<String> depotCodes) {
        List<TGmDepot> depots = depotService.finfTGmDepotList();
        List<String> depotStr = depots.stream().map(TGmDepot::getDepotCode).collect(Collectors.toList());
        if (Collections3.isNotEmpty(depotCodes)) {
            depotStr = Collections3.subtract(depotStr, depotCodes);
        }
        List<MbrAuditBonus> auditBonuses = findMbrAuditBonusByAccountId(accountId, null, endTime);
        if (Collections3.isNotEmpty(auditBonuses)) {
            List<String> bonusesStr = auditBonuses.stream()
                    .map(MbrAuditBonus::getDepotCode).collect(Collectors.toList());
            List<String> stringList = Collections3.subtract(depotStr, bonusesStr);
            return stringList;
        }
        return depotStr;
    }

    private RptBetModel getValidBet(MbrAuditAccount audit, List<MbrAuditAccount> audits, int i, String siteCode, Long bounsCount) {
        log.info("??????==siteCode==" + siteCode + "==accountId==" + audit.getAccountId() + "==loginName==" + audit.getLoginName() + "==getValidBet==??????");
        String startTime = audit.getTime();
        // ???????????????????????????????????????????????????????????????????????????(????????????)
        String endTime = i == audits.size() - 1 ? getCurrentDate(FORMAT_18_DATE_TIME) : audits.get(i + 1).getTime();
        MbrAuditBonus auditBonus = new MbrAuditBonus();
        auditBonus.setAccountId(audit.getAccountId());
        auditBonus.setStartTime(startTime);
        auditBonus.setEndTime(endTime);
        // 1. mbr_audit_bonus ??????  auditBonuses size??????0
        List<MbrAuditBonus> auditBonuses = auditMapper.fundAuditBonusByTime(auditBonus);
        Boolean isRptBetModelOne = Boolean.FALSE;
        if (auditBonuses.size() == 0) {
            isRptBetModelOne = Boolean.TRUE;
        }
        if (Boolean.FALSE.equals(isRptBetModelOne)) {
            long counDrawings = auditBonuses.stream().filter(p ->
                            Constants.EVNumber.one == p.getIsDrawings())
                    .map(MbrAuditBonus::getId).count();
            if (counDrawings == 0) {
                isRptBetModelOne = Boolean.TRUE;
            }
        }
        // 2. ????????????????????????
        if (Boolean.TRUE.equals(isRptBetModelOne)) {
            // 3. ??????????????????????????????code
            List<String> depotCodes = getDepotCode(audit.getAccountId(), endTime, null);
            // 4. ?????????????????????????????????????????????????????????????????????
            RptBetModel rptBetModel = getRptBetModel(siteCode, audit, startTime, endTime, depotCodes, bounsCount);
            log.info("??????==siteCode==" + siteCode + "==accountId==" + audit.getAccountId() + "==loginName==" + audit.getLoginName() + "==getValidBet==??????1");
            return rptBetModel;
        }
        // 3. ????????????????????????
        List<String> depotStr = auditBonuses.stream().filter(a ->
                        a.getIsDrawings() == Constants.EVNumber.zero ||
                                (StringUtils.isNotEmpty(a.getTransferTime())
                                        && a.getTransferTime().compareTo(endTime) > 0))
                .map(MbrAuditBonus::getDepotCode).collect(Collectors.toList());
        RptBetModel betModel = new RptBetModel();
        betModel.setValidBet(BigDecimal.ZERO);
        betModel.setPayout(BigDecimal.ZERO);

        List<MbrAuditBonus> auditBonusList = auditBonuses.stream().filter(
                        a -> StringUtils.isNotEmpty(a.getTransferTime())
                                && a.getIsDrawings() == Constants.EVNumber.one
                                && a.getTransferTime().compareTo(endTime) < 0)
                .collect(Collectors.toList());
        auditBonusList.sort((r1, r2) -> r1.getTransferTime().compareTo(r2.getTransferTime()));

        String timeFrom = "", timeTo = "";
        int count = auditBonusList.size() + 1;
        for (int j = 0; j < count; j++) {
            String depotCode = StringUtils.EMPTY;
            if (j == 0 && auditBonusList.size() > 0) {
                MbrAuditBonus bonus = auditBonusList.get(j);
                timeFrom = startTime;
                timeTo = bonus.getTransferTime();
                depotCode = bonus.getDepotCode();
                depotStr.add(depotCode);
            }
            if (j > 0 && j < count - 1) {
                MbrAuditBonus bonus = auditBonusList.get(j);
                timeFrom = auditBonusList.get(j - 1).getTransferTime();
                timeTo = bonus.getTransferTime();
                depotCode = bonus.getDepotCode();
                depotStr.add(depotCode);
            }
            if (j == count - 1) {
                timeFrom = auditBonusList.size() == 0
                        ? startTime : auditBonusList.get(auditBonusList.size() - 1).getTransferTime();
                timeTo = endTime;
            }
            List<String> depotCodes = getDepotCode(audit.getAccountId(), endTime, depotStr);
            RptBetModel rptBetModel = getRptBetModel(siteCode, audit, timeFrom, timeTo, depotCodes, bounsCount);
            if (nonNull(rptBetModel)) {
                betModel.setValidBet(betModel.getValidBet().add(
                        nonNull(rptBetModel.getValidBet()) ? rptBetModel.getValidBet() : BigDecimal.ZERO));
                betModel.setPayout(betModel.getPayout().add(
                        nonNull(rptBetModel.getPayout()) ? rptBetModel.getPayout() : BigDecimal.ZERO));
            }
            if (StringUtils.isNotEmpty(depotCode)) {
                depotStr.remove(depotCode);
            }
        }
        log.info("??????==siteCode==" + siteCode + "==accountId==" + audit.getAccountId() + "==loginName==" + audit.getLoginName() + "==getValidBet==??????2");
        return betModel;
    }

    private RptBetModel getRptBetModel(String siteCode, MbrAuditAccount audit, String startTime, String
            endTime, List<String> depotCodes, Long bounsCount) {
        List<RptBetModel> betModels = analysisService.getValidBet(siteCode,
                Lists.newArrayList(audit.getLoginName().toLowerCase()),
                formatEsDate(startTime), formatEsDate(endTime),
                null, null, depotCodes, bounsCount);
        if (betModels.size() > 0) {
            return betModels.get(0);
        }
        return null;
    }

    private BigDecimal getBonusRemainValidBet(BounsValidBetDto dto, MbrAuditAccount auditAccount) {
        List<MbrAuditBonus> auditBonuses = dto.getAuditBonuses();
        BigDecimal bigDecimal = nonNull(auditAccount.getBonusRemainValidBet())
                ? auditAccount.getBonusRemainValidBet() : BigDecimal.ZERO;
        if (Collections3.isNotEmpty(auditBonuses)) {
            Map<Integer, List<MbrAuditBonus>> auditBonusGroupingBy =
                    auditBonuses.stream().collect(
                            Collectors.groupingBy(
                                    MbrAuditBonus::getDepotId));
            for (Integer depotIdKey : auditBonusGroupingBy.keySet()) {
                List<MbrAuditBonus> auditBonusList = auditBonusGroupingBy.get(depotIdKey);
                Collections.sort(auditBonusList, Comparator.comparing(MbrAuditBonus::getTime));
                if (dto.getDepotIds().contains(auditBonusList.get(0).getDepotId())) {
                    continue;
                }
                if (auditAccount.getTime().compareTo(
                        auditBonusList.get(auditBonusList.size() - 1).getTime()) > 0) {
                    continue;
                }
                long counValid = auditBonusList.stream().filter(p ->
                                Constants.EVNumber.zero == p.getIsValid())
                        .map(MbrAuditBonus::getId).count();
                long couStatusnt = auditBonusList.stream().filter(p ->
                                Constants.EVNumber.zero == p.getStatus())
                        .map(MbrAuditBonus::getId).count();
                long couClean = auditBonusList.stream().filter(p ->
                                nonNull(p.getIsClean())
                                        && Constants.EVNumber.one == p.getIsClean())
                        .map(MbrAuditBonus::getIsClean).count();
                if (counValid == Constants.EVNumber.zero
                        && couStatusnt == Constants.EVNumber.zero
                        && couClean == Constants.EVNumber.zero) {
                    List<Integer> depotIds = dto.getDepotIds();
                    depotIds.add(auditBonusList.get(0).getDepotId());
                    dto.setDepotIds(depotIds);
                    bigDecimal = auditBonusList.get(0).getRemainValidBet().add(bigDecimal);
                }
            }
        }
        return bigDecimal;
    }

    private void castAuditAccout(MbrAuditAccount accountAudit, MbrAuditAccount remainValidBet) {
        BigDecimal addDecimal = isNull(accountAudit.getAuditAmount()) ? BigDecimal.ZERO : accountAudit.getAuditAmount();    // ????????????
        switch (accountAudit.getValidBet().compareTo(addDecimal)) { // ?????????????????????????????????
            // ????????????????????????
            case 0:
                if (StringUtil.isEmpty(accountAudit.getPassTime())) { // ???????????????passtime
                    accountAudit.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
                }
                accountAudit.setStatus(Constants.EVNumber.one);
                break;
            // ????????????????????????????????? ??????????????????????????????
            case 1:
                if (StringUtil.isEmpty(accountAudit.getPassTime())) { // ???????????????passtime
                    accountAudit.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
                }
                accountAudit.setStatus(Constants.EVNumber.one);

                BigDecimal bigDecimal1 = accountAudit.getValidBet().subtract(addDecimal);   // ???????????? - ???????????? = ????????????
                remainValidBet.setRemainValidBet(bigDecimal1.add(remainValidBet.getRemainValidBet()));  // ???????????????????????????
                break;
            // ???????????????????????????????????????????????????????????????
            case -1:
                castRemainValidBet(accountAudit, remainValidBet, addDecimal);   // ????????????   ????????????  ????????????
                break;
            default:
        }
        // ???????????????????????????
        accountAudit.setRemainValidBet(remainValidBet.getRemainValidBet());
        auditAccountMapper.updateByPrimaryKeySelective(accountAudit);
    }

    /**
     *
     * @param accountAudit      ????????????
     * @param remainValidBet    ????????????
     * @param bigDecimal        ????????????
     */
    private void castRemainValidBet(MbrAuditAccount accountAudit, MbrAuditAccount remainValidBet, BigDecimal bigDecimal) {
        BigDecimal decimal = accountAudit.getValidBet().add(remainValidBet.getRemainValidBet());    // ??????????????????+?????????????????? = ????????????
        // ????????????????????????????????? ???????????????????????????????????????0????????????????????????????????????
        if (decimal.compareTo(bigDecimal) == -1) {
            accountAudit.setStatus(Constants.EVNumber.zero);
            remainValidBet.setRemainValidBet(BigDecimal.ZERO);  // ??????????????????
            return;
        }
        if (StringUtil.isEmpty(accountAudit.getPassTime())) { // ???????????????passtime
            accountAudit.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
        }
        accountAudit.setStatus(Constants.EVNumber.one);
        remainValidBet.setRemainValidBet(decimal.subtract(bigDecimal));     // ?????????????????????????????????????????????????????????
    }

    @Transactional
    public void castDepositBalance(List<MbrAuditAccount> accountAudits) {
        BigDecimal totalBalance = BigDecimal.ZERO;  // ?????????????????????????????????????????????????????????????????????????????????0?????????????????????
        Boolean isPayOut = Boolean.FALSE;           // ??????????????? --> ?????????????????????
        for (int i = 0; i < accountAudits.size(); i++) {
            MbrAuditAccount audit = accountAudits.get(i);
            // ?????????(??????null)????????????????????????????????????(????????????)
            audit.setDepositAmount(nonNull(audit.getDepositAmount()) ? audit.getDepositAmount() : BigDecimal.ZERO);
            audit.setDepositOutBalance(nonNull(audit.getDepositOutBalance()) ? audit.getDepositOutBalance() : BigDecimal.ZERO);

            BigDecimal depositAmount = audit.getDepositAmount();    // ????????????
            // ???????????????????????????????????????????????????????????????????????????????????????--> ????????????????????????????????????????????????????????????????????????????????????0
            // ?????????????????????????????????????????????
            if (nonNull(audit.getDepositBalance()) && audit.getDepositBalance().compareTo(BigDecimal.ZERO) != 0) {
                isPayOut = Boolean.TRUE;
            }
            totalBalance = depositAmount.add(audit.getDepositBalance()).add(totalBalance);  // ???????????? = ?????? + ?????? + ????????????????????????????????????
            // ????????????????????????
            // ????????????????????????????????????
            if (totalBalance.compareTo(audit.getDepositOutBalance()) != 1) {
                if (StringUtil.isEmpty(audit.getPassTime())) { // ???????????????passtime
                    audit.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
                }
                audit.setStatus(Constants.EVNumber.one);
                audit.setIsOut(Constants.EVNumber.one);
                audit.setDepositBalance(totalBalance);      // ??????????????????????????????????????????????????????

                // ?????????????????? ????????????0???????????????????????????0??????????????????????????????
                int compareBalance = totalBalance.compareTo(BigDecimal.ZERO);
                if (compareBalance != 1) {
                    audit.setDepositBalance(BigDecimal.ZERO);
                }
            }
            // ?????????????????????????????????
            if (totalBalance.compareTo(audit.getDepositOutBalance()) == 1) {
                audit.setDepositBalance(totalBalance);
                audit.setIsOut(Constants.EVNumber.zero);
                totalBalance = BigDecimal.ZERO;     // ?????????
            }
            if (Boolean.FALSE.equals(isPayOut)) {   // ??????????????????????????????????????????????????????????????????????????????
                totalBalance = BigDecimal.ZERO;
            }
            auditAccountMapper.updateByPrimaryKeySelective(audit);
        }
    }


    private BounsValidBetDto updateAuditBonus(MbrAccount account, Integer depotId, String siteCode) {
        log.info("??????==siteCode==" + siteCode + "==accountId==" + account.getId() + "==loginName==" + account.getLoginName() + "==updateAuditBonus==??????");
        BounsValidBetDto bounsValidBetDto = new BounsValidBetDto();
        // 1.??????????????????
        List<MbrAuditBonus> auditBonuses = auditBonusValidBet(account, depotId, siteCode);
        if (auditBonuses.size() > 0) {
            Collections.reverse(auditBonuses);
            Map<Integer, Map<Integer, List<MbrAuditBonus>>>
                    auditBonusGroupingBy =
                    auditBonuses.stream()
                            .collect(Collectors.groupingBy(
                                    MbrAuditBonus::getDepotId,
                                    Collectors.groupingBy(
                                            MbrAuditBonus::getCatId)));

            for (Integer depotIdKey : auditBonusGroupingBy.keySet()) {
                Map<Integer, List<MbrAuditBonus>> auditBonusMapMap =
                        auditBonusGroupingBy.get(depotIdKey);
                MbrAuditBonus remainValidBet = new MbrAuditBonus();
                remainValidBet.setRemainValidBet(BigDecimal.ZERO);
                for (Integer catIdKey : auditBonusMapMap.keySet()) {
                    auditBonusMapMap.get(catIdKey).forEach(as -> {
                        castAuditBonus(as, remainValidBet);
                    });
                }
                bounsValidBetDto.setSumValidBet(remainValidBet.getRemainValidBet()
                        .add(nonNull(bounsValidBetDto.getSumValidBet()) ?
                                bounsValidBetDto.getSumValidBet() : BigDecimal.ZERO));
            }
            long count = auditBonuses.stream()
                    .filter(auditBonus ->
                            Constants.EVNumber.zero == auditBonus.getStatus())
                    .map(MbrAuditBonus::getId).count();
            if (count > 0) {
                casAuditBonusBalance(auditBonuses);
            }
            bounsValidBetDto.setAuditBonuses(auditBonuses);
        }
        log.info("??????==siteCode==" + siteCode + "==accountId==" + account.getId() + "==loginName==" + account.getLoginName() + "==updateAuditBonus==??????");
        return bounsValidBetDto;
    }


    private List<MbrAuditBonus> auditBonusValidBet(MbrAccount account, Integer depotId, String siteCode) {
        log.info("??????==siteCode==" + siteCode + "==accountId==" + account.getId() + "==loginName==" + account.getLoginName() + "==updateAuditBonus==auditBonusValidBet==??????");
        List<MbrAuditBonus> auditBonuses = findMbrAuditBonusByAccountId(account.getId(), depotId, null);
        if (Collections3.isNotEmpty(auditBonuses)) {
            Map<Integer, List<MbrAuditBonus>> auditBonusGroupingBy =
                    auditBonuses.stream().collect(
                            Collectors.groupingBy(
                                    MbrAuditBonus::getDepotId));
            for (Integer depotIdKey : auditBonusGroupingBy.keySet()) {
                updateAuditBonusValidBet(auditBonusGroupingBy.get(depotIdKey), siteCode, account);
            }
        }
        log.info("??????==siteCode==" + siteCode + "==accountId==" + account.getId() + "==loginName==" + account.getLoginName() + "==updateAuditBonus==auditBonusValidBet==??????");
        return auditBonuses;
    }

    private void castAuditBonus(MbrAuditBonus auditBonus, MbrAuditBonus remainValidBet) {
        BigDecimal addDecimal = auditBonus.getAuditAmount();
        switch (auditBonus.getValidBet().compareTo(addDecimal)) {
            case 0:
                auditBonus.setStatus(Constants.EVNumber.one);
                break;
            case 1:
                auditBonus.setStatus(Constants.EVNumber.one);
                BigDecimal bigDecimal1 = auditBonus.getValidBet().subtract(addDecimal);
                remainValidBet.setRemainValidBet(bigDecimal1.add(remainValidBet.getRemainValidBet()));
                if (auditBonus.getIsValid() == Constants.EVNumber.zero) {
                    remainValidBet.setRemainValidBet(BigDecimal.ZERO);
                }
                break;
            case -1:
                if (auditBonus.getIsValid() == Constants.EVNumber.zero) {
                    remainValidBet.setRemainValidBet(BigDecimal.ZERO);
                }
                castAuditBonusRemainValidBet(auditBonus, remainValidBet, addDecimal);
                break;
            default:
        }
        auditBonus.setRemainValidBet(remainValidBet.getRemainValidBet());
        auditBonusMapper.updateByPrimaryKey(auditBonus);
    }

    private void casAuditBonusBalance(List<MbrAuditBonus> auditBonuses) {
        BigDecimal totalBalance = BigDecimal.ZERO;
        for (int i = 0; i < auditBonuses.size(); i++) {
            MbrAuditBonus auditBonus = auditBonuses.get(i);
            auditBonus.setDiscountBalance(nonNull(auditBonus.getPayOut())
                    ? auditBonus.getPayOut() : BigDecimal.ZERO);
            BigDecimal depositAmount = auditBonus.getDepositAmount();
            totalBalance = depositAmount.add(auditBonus.getDiscountBalance()).add(totalBalance);
            if (totalBalance.compareTo(auditBonus.getOutBalance()) != 1) {
                auditBonus.setStatus(Constants.EVNumber.one);
                auditBonus.setIsOut(Constants.EVNumber.one);
                auditBonus.setDiscountBalance(totalBalance);
                int compareBalance = totalBalance.compareTo(BigDecimal.ZERO);
                if (compareBalance != 1) {
                    auditBonus.setDiscountBalance(BigDecimal.ZERO);
                }
            }
            if (totalBalance.compareTo(auditBonus.getOutBalance()) == 1) {
                auditBonus.setDiscountBalance(totalBalance);
                auditBonus.setIsOut(Constants.EVNumber.zero);
                totalBalance = BigDecimal.ZERO;
            }
            auditBonusMapper.updateByPrimaryKey(auditBonus);
        }
    }

    private void castAuditBonusRemainValidBet(MbrAuditBonus auditBonus, MbrAuditBonus remainValidBet, BigDecimal
            bigDecimal) {
        BigDecimal decimal = auditBonus.getValidBet().add(remainValidBet.getRemainValidBet());
        if (decimal.compareTo(bigDecimal) == -1) {
            auditBonus.setStatus(Constants.EVNumber.zero);
            remainValidBet.setRemainValidBet(BigDecimal.ZERO);
            return;
        }
        auditBonus.setStatus(Constants.EVNumber.one);
        remainValidBet.setRemainValidBet(decimal.subtract(bigDecimal));
    }

    private void insertMbrAuditFrauds(List<RptBetTotalModel> rptBetTotalModels, String catName, Long orderNo) {
        rptBetTotalModels.stream().forEach(b -> {
            if (!catName.equals(b.getGameCategory())) {
                MbrAuditFraud auditFraud = new MbrAuditFraud();
                auditFraud.setCatName(b.getGameCategory());
                TGmDepot depot = new TGmDepot();
                depot.setDepotCode(b.getPlatform());
                TGmDepot gmDepot = depotMapper.selectOne(depot);
                auditFraud.setDepotName(gmDepot.getDepotName());
                auditFraud.setOrderNo(orderNo);
                auditFraud.setStartTime(b.getMinTime());
                auditFraud.setEntTime(b.getMaxTime());
                auditFraud.setFraudValidBet(b.getValidBetTotal());
                auditFraud.setPayOut(b.getPayoutTotal());
                auditFraudMapper.insert(auditFraud);
            }
        });
    }


    private void updateAuditBonusValidBet(List<MbrAuditBonus> auditBonuses, String siteCode, MbrAccount account) {
        for (int i = 0; i < auditBonuses.size(); i++) {
            MbrAuditBonus bonus = auditBonuses.get(i);
            TGmCat gmCat = gmCatMapper.selectByPrimaryKey(bonus.getCatId());
            Long orderNo = new SnowFlake().nextId();
            String endTime = i == auditBonuses.size() - 1 ?
                    getCurrentDate(FORMAT_18_DATE_TIME) : auditBonuses.get(i + 1).getTime();
            String startTime = bonus.getTime();

            if (StringUtils.isNotEmpty(bonus.getUpdateAuditTime())
                    && bonus.getUpdateAuditTime().compareTo(endTime) < 0) {
                startTime = bonus.getUpdateAuditTime();
            }
            if (StringUtils.isEmpty(bonus.getDepotCode())) {
                TGmDepot depot = depotMapper.selectByPrimaryKey(bonus.getDepotId());
                bonus.setDepotCode(depot.getDepotCode());
            }
            List<RptBetTotalModel> rptBetTotalModels = analysisService.getGameCategoryReport(
                    siteCode, account.getLoginName(), Lists.newArrayList(bonus.getDepotCode()),
                    formatEsDate(startTime), formatEsDate(endTime));
            if (Collections3.isNotEmpty(rptBetTotalModels)) {
                RptBetTotalModel betTotalModel = getRptBetTotalModel(rptBetTotalModels, gmCat.getCatName());
                bonus.setValidBet(betTotalModel.getValidBetTotal());
                bonus.setPayOut(betTotalModel.getPayoutTotal());
                deleteAuditFraud(bonus.getOrderNo());
                bonus.setOrderNo(orderNo);
                bonus.setIsValid(Constants.EVNumber.one);
                bonus.setStatus(Constants.EVNumber.zero);
                bonus.setIsOut(Constants.EVNumber.zero);
                Boolean isFraud = Boolean.TRUE;
                if (rptBetTotalModels.size() > Constants.EVNumber.one) {
                    isFraud = Boolean.FALSE;
                }
                if (rptBetTotalModels.size() == Constants.EVNumber.one
                        && !rptBetTotalModels.get(0).getGameCategory().equals(gmCat.getCatName())) {
                    isFraud = Boolean.FALSE;
                }
                if (Boolean.FALSE.equals(isFraud)) {
                    bonus.setIsValid(Constants.EVNumber.zero);
                    bonus.setStatus(Constants.EVNumber.zero);
                    bonus.setIsDispose(Constants.EVNumber.zero);
                    insertMbrAuditFrauds(rptBetTotalModels, gmCat.getCatName(), orderNo);
                }
                auditBonusMapper.updateByPrimaryKey(bonus);
            }
        }
    }

    private void deleteAuditFraud(Long orderNo) {
        if (nonNull(orderNo)) {
            MbrAuditFraud auditFraud = new MbrAuditFraud();
            auditFraud.setOrderNo(orderNo);
            auditFraudMapper.delete(auditFraud);
        }
    }

    private RptBetTotalModel getRptBetTotalModel(List<RptBetTotalModel> rptBetTotalModels, String catName) {
        RptBetTotalModel totalModel = new RptBetTotalModel();
        if (Collections3.isNotEmpty(rptBetTotalModels)) {
            rptBetTotalModels.stream().forEach(rptBetTotalModel -> {
                if (catName.equals(rptBetTotalModel.getGameCategory())) {
                    totalModel.setValidBetTotal(rptBetTotalModel.getValidBetTotal().add(totalModel.getValidBetTotal()));
                }
                totalModel.setPayoutTotal(rptBetTotalModel.getPayoutTotal().add(totalModel.getPayoutTotal()));
            });
        }
        return totalModel;
    }

    public List<MbrAuditBonus> findMbrAuditBonusByAccountId(Integer accountId, Integer depotId, String auditTime) {
        MbrAuditBonus auditBonus = new MbrAuditBonus();
        auditBonus.setAccountId(accountId);
        auditBonus.setDepotId(depotId);
        auditBonus.setSort(Boolean.TRUE);
        auditBonus.setIsDrawings(Constants.EVNumber.zero);
        auditBonus.setAuditTime(auditTime);
        return auditMapper.finAuditBonusList(auditBonus);
    }

    public BigDecimal accountValidBet(List<String> sitePrefix, String username, String startTime, String endTime) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.rangeQuery("payoutTime").gte(startTime).lt(endTime));
        query.must(QueryBuilders.termsQuery("sitePrefix", sitePrefix));
        query.must(QueryBuilders.termsQuery("userName", username));

        SumAggregationBuilder agg = AggregationBuilders.sum("originalValidBet").field("originalValidBet");
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        searchRequestBuilder.setQuery(query);
        searchRequestBuilder.addAggregation(agg);
        String str = searchRequestBuilder.toString();
        try {
            Response response = connection.restClient_Read.performRequest("GET",
                    "/" + ElasticSearchConstant.REPORT_INDEX +
                            "/" + ElasticSearchConstant.REPORT_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"),
                    new NStringEntity(str, ContentType.APPLICATION_JSON));
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            Map map1 = (Map) ((Map) map.get("aggregations")).get("originalValidBet");
            return (BigDecimal) map1.get("value");
        } catch (Exception e) {
            log.error("??????==accountValidBet==loginName==" + username + "==error==" + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    public List<String> getSitePrefix(String siteCode) {
        return baseMapper.getApiPrefixBySiteCode(siteCode);
    }
}
