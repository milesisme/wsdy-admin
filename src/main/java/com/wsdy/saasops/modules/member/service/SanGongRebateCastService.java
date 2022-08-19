package com.wsdy.saasops.modules.member.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.base.mapper.BaseMapper;
import com.wsdy.saasops.modules.fund.dao.FundAuditMapper;
import com.wsdy.saasops.modules.fund.entity.FundAudit;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrRebateReportNewMapper;
import com.wsdy.saasops.modules.member.dto.RebateValidBetDto;
import com.wsdy.saasops.modules.member.dto.SanGongCatsDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrRebateReportNew;
import com.wsdy.saasops.modules.member.mapper.SanGongMapper;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.constants.Constants.ONE_HUNDRED;
import static com.wsdy.saasops.common.utils.DateUtil.*;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_10_DATE;
import static com.wsdy.saasops.modules.member.service.AccountWaterCastService.HHMMSS;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
public class SanGongRebateCastService {

    @Autowired
    private ElasticSearchConnection_Read connection;
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private SanGongMapper sanGongMapper;
    @Autowired
    private MbrRebateReportNewMapper rebateReportMapper;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private FundAuditMapper fundAuditMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private AccountWaterSettlementService waterSettlementService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MbrAccountMapper accountMapper;


    public void sanGongRebateCast(String siteCode) {
        log.info("开始计算三公" + siteCode);
        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.EG_SANGONG_FLG);
        if (isNull(setting) || !"1".equals(setting.getSysvalue())) {
            log.info("该站点未开启三公计算" + siteCode);
            return;
        }
        SysSetting sysSetting = sysSettingService.getSysSetting(SystemConstants.EG_SANGONG_AGENT);
        if (isNull(sysSetting)) {
            log.info("该站点未配置三公计算代理" + siteCode);
            return;
        }
        List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(siteCode);
        String startTime = formatEsDate(getPastDate(Constants.EVNumber.one, FORMAT_10_DATE) + HHMMSS);
        String endTime = formatEsDate(getPastDate(-1, FORMAT_10_DATE) + HHMMSS);
        List<String> loginNames = getValidBetAccounts(sitePrefix, startTime, endTime, "0");
        List<MbrAccount> mbrAccountList = sanGongMapper.findAccountListByCagencyid(Integer.valueOf(sysSetting.getSysvalue()));
        if (Collections3.isNotEmpty(loginNames)) {
            loginNames.stream().forEach(ns -> accountRebateCastNew(startTime, endTime, sitePrefix, ns, mbrAccountList, siteCode));
        }
    }

    @Transactional
    @Async("sanGongRebateTaskAsyncExecutor")
    public void accountRebateCastNew(String startTime, String endTime, List<String> sitePrefix, String loginName,
                                     List<MbrAccount> mbrAccountList, String siteCode) {
        String key = RedisConstants.SAN_GONG_CAST + siteCode + loginName;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, loginName, 200, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            Optional<MbrAccount> account = mbrAccountList.stream().filter(e ->
                    StringUtils.isNotEmpty(e.getLoginName()) && e.getLoginName().equals(loginName)).findAny();
            if (account.isPresent()) {
                MbrAccount mbrAccount = account.get();
                List<RebateValidBetDto> validBetDtos = getValidBet(sitePrefix, startTime, endTime, "0", mbrAccount.getLoginName());
                if (Collections3.isNotEmpty(validBetDtos)) {
                    Map<String, List<RebateValidBetDto>> validBetGroupingBy =
                            validBetDtos.stream().collect(Collectors.groupingBy(RebateValidBetDto::getGameCategory));

                    List<String> esUpdateIds = Lists.newArrayList();
                    List<SanGongCatsDto> rebateDtos = Lists.newArrayList();

                    List<MbrAccount> accountList = sanGongMapper.findSuperiorAccountList(mbrAccount.getId());

                    for (String gameCategory : validBetGroupingBy.keySet()) {
                        List<RebateValidBetDto> rebateValidBetDtos = validBetGroupingBy.get(gameCategory);
                        BigDecimal validBet = rebateValidBetDtos.stream().filter(p -> nonNull(p.getValidBet()))
                                .map(RebateValidBetDto::getValidBet).reduce(BigDecimal::add).get();
                        Object objectCatId = CommonUtil.getKey(Constants.depotCatMap, gameCategory);
                        Integer catId = nonNull(objectCatId) ? Integer.parseInt(objectCatId.toString()) : null;

                        esUpdateIds.addAll(rebateValidBetDtos.stream().map(RebateValidBetDto::getId).collect(Collectors.toList()));
                        if (mbrAccount.getDepth() != Constants.EVNumber.three) {
                            BigDecimal bonusAmount = CommonUtil.adjustScale(mbrAccount.getRebateRatio().divide(
                                    new BigDecimal(ONE_HUNDRED)).multiply(validBet));
                            if (bonusAmount.compareTo(BigDecimal.ZERO) == 1) {
                                MbrRebateReportNew rebateReport = setMbrRebateReport(mbrAccount, catId,
                                        bonusAmount, validBet, mbrAccount, mbrAccount.getRebateRatio(), BigDecimal.ZERO, Constants.EVNumber.zero);
                                rebateReportMapper.insert(rebateReport);
                                rebateDtos.add(getSanGongCatsDto(mbrAccount.getId(), rebateReport.getId(), bonusAmount, Boolean.TRUE, Boolean.FALSE));
                            }
                        }
                        for (int i = 0; i < accountList.size(); i++) {
                            int subscript = i + 1;
                            BigDecimal rebateRatioActual = BigDecimal.ZERO;
                            MbrAccount account1 = new MbrAccount();
                            Boolean isSuperior = Boolean.FALSE;
                            if (accountList.get(i).getDepth() != Constants.EVNumber.one) {
                                rebateRatioActual = accountList.get(i).getRebateRatio().subtract(accountList.get(subscript).getRebateRatio());
                                account1 = accountList.get(subscript);
                            } else {
                                BigDecimal rebateRatio = nonNull(mbrAccount.getRebateRatio()) ? mbrAccount.getRebateRatio() : BigDecimal.ZERO;
                                rebateRatioActual = accountList.get(i).getRebateRatio().subtract(rebateRatio);
                                account1 = mbrAccount;
                                isSuperior = Boolean.TRUE;
                            }
                            BigDecimal bonusAmount = CommonUtil.adjustScale(rebateRatioActual.divide(
                                    new BigDecimal(ONE_HUNDRED)).multiply(validBet));
                            if (bonusAmount.compareTo(BigDecimal.ZERO) == 1) {
                                MbrRebateReportNew rebateReport = setMbrRebateReport(accountList.get(i), catId,
                                        bonusAmount, validBet, account1, accountList.get(i).getRebateRatio(), rebateRatioActual, Constants.EVNumber.one);
                                rebateReportMapper.insert(rebateReport);
                                rebateDtos.add(getSanGongCatsDto(accountList.get(i).getId(), rebateReport.getId(), bonusAmount, Boolean.FALSE, isSuperior));
                            }
                        }
                    }

                    if (rebateDtos.size() > 0) {
                        Integer auditId = Constants.EVNumber.zero, topAuditId = Constants.EVNumber.zero;
                        Map<Integer, List<SanGongCatsDto>> sanGongGroupingBy =
                                rebateDtos.stream().collect(Collectors.groupingBy(SanGongCatsDto::getAccountId));
                        for (Integer accountIdKey : sanGongGroupingBy.keySet()) {
                            List<SanGongCatsDto> sanGongCatsDtos = sanGongGroupingBy.get(accountIdKey);
                            MbrAccount account1 = accountMapper.selectByPrimaryKey(accountIdKey);
                            BigDecimal amount = sanGongCatsDtos.stream().map(SanGongCatsDto::getAmount).reduce(BigDecimal::add).get();
                            List<Integer> rebateIds = sanGongCatsDtos.stream().map(SanGongCatsDto::getRebateId).collect(Collectors.toList());
                            FundAudit audit = sangongRebateAccount(amount, account1, siteCode);
                            sanGongMapper.updateBatchRebateReport(audit.getId(), rebateIds);
                            if (sanGongCatsDtos.get(0).getIsSelf()) {
                                auditId = audit.getId();
                            }
                            if (sanGongCatsDtos.get(0).getIsSuperior()) {
                                topAuditId = audit.getId();
                            }
                        }
                        Integer finalAuditId = auditId;
                        Integer finalTopAuditId = topAuditId;
                        esUpdateIds.forEach(st -> waterSettlementService.
                                esUpdateByQuery(getUpdateAuditJson(finalAuditId.toString(), finalTopAuditId.toString()), st));
                    }
                }
            }
            redisService.del(key);
        }
    }

    private SanGongCatsDto getSanGongCatsDto(Integer accountId, Integer rebateId, BigDecimal bonusAmount, Boolean isSelf, Boolean isSuperior) {
        SanGongCatsDto sanGongCatsDto = new SanGongCatsDto();
        sanGongCatsDto.setAccountId(accountId);
        sanGongCatsDto.setRebateId(rebateId);
        sanGongCatsDto.setAmount(bonusAmount);
        sanGongCatsDto.setIsSelf(isSelf);
        sanGongCatsDto.setIsSuperior(isSuperior);
        return sanGongCatsDto;
    }

    private FundAudit sangongRebateAccount(BigDecimal amount, MbrAccount supAccount, String siteCode) {
        FundAudit fundAudit = new FundAudit();
        fundAudit.setAccountId(supAccount.getId());
        fundAudit.setAmount(amount);
        fundAudit.setMemo("三公返利");
        fundAudit.setFinancialCode(OrderConstants.ACCOUNT_REBATE_FA);
        fundAudit.setLoginName(supAccount.getLoginName());
        fundAudit.setStatus(Constants.EVNumber.one);
        fundAudit.setDepositType(Constants.EVNumber.three);
        fundAudit.setOrderNo(String.valueOf(new SnowFlake().nextId()));
        fundAudit.setOrderPrefix(OrderConstants.FUND_ORDER_AUDIT);
        fundAudit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundAudit.setCreateUser(Constants.SYSTEM_USER);
        fundAudit.setModifyTime(fundAudit.getCreateTime());
        fundAudit.setModifyUser(Constants.SYSTEM_USER);
        fundAudit.setAuditTime(fundAudit.getCreateTime());
        fundAudit.setAuditUser(Constants.SYSTEM_USER);
        MbrBillDetail mbrBillDetail = walletService.castWalletAndBillDetail(fundAudit.getLoginName(),
                fundAudit.getAccountId(), fundAudit.getFinancialCode(), fundAudit.getAmount(),
                fundAudit.getOrderNo(), Boolean.TRUE,null,null);
        fundAudit.setBillDetailId(mbrBillDetail.getId());
        fundAuditMapper.insert(fundAudit);

        applicationEventPublisher.publishEvent(
                new BizEvent(this, siteCode, supAccount.getId(), BizEventType.ACCOUNT_REBATE,
                        amount, fundAudit.getOrderPrefix() + fundAudit.getOrderNo()));
        return fundAudit;
    }

    private MbrRebateReportNew setMbrRebateReport(MbrAccount topAccount, Integer catId, BigDecimal amount,
                                                  BigDecimal validbet, MbrAccount mbrAccount,
                                                  BigDecimal rebateRatio, BigDecimal rebateRatioActual, Integer depth) {
        MbrRebateReportNew rebateReport = new MbrRebateReportNew();
        rebateReport.setReportTime(getCurrentDate(FORMAT_18_DATE_TIME));
        rebateReport.setLoginName(topAccount.getLoginName());
        rebateReport.setAccountId(topAccount.getId());
        rebateReport.setCatId(catId);
        rebateReport.setAmount(amount);
        rebateReport.setValidbet(validbet);
        rebateReport.setSubAccountId(mbrAccount.getId());
        rebateReport.setSubLoginName(mbrAccount.getLoginName());
        rebateReport.setDepth(depth);
        rebateReport.setRebateRatio(rebateRatio);
        rebateReport.setRebateRatioActual(rebateRatioActual);
        return rebateReport;
    }

    public String getUpdateAuditJson(String auditId, String topAuditId) {
        return String.format("{\"doc\" : {\"isRebate\" : \"%s\",\"isSubAuditId\":\"%s\",\"isAuditId\":\"%s\"}}", "1", topAuditId, auditId);
    }

    private List<RebateValidBetDto> getValidBet(List<String> sitePrefix, String startTime, String endTime, String isRebate, String username) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termsQuery("userName", username));
        query.must(QueryBuilders.termsQuery("sitePrefix", sitePrefix));
        if (StringUtils.isNotEmpty(isRebate)) {
            query.must(QueryBuilders.termsQuery("isRebate", isRebate));
        }
        if (nonNull(startTime)) {
            query.must(QueryBuilders.rangeQuery("payoutTime").gte(startTime).lt(endTime));
        }

        SearchRequestBuilder searchRequestBuilder =
                connection.client.prepareSearch("report")
                        .setQuery(query).setSize(ElasticSearchConstant.SEARCH_COUNT)
                        .setFetchSource(new String[]{"id", "validBet", "gameCategory"}, null);
        try {
            String str = searchRequestBuilder.toString();
            Response response = connection.restClient_Read.performRequest(
                    "GET", "/" + ElasticSearchConstant.RPT_WATER
                            + "/" + ElasticSearchConstant.RPT_BET_WATERR_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"),
                    new NStringEntity(str, ContentType.APPLICATION_JSON));

            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));
            List<RebateValidBetDto> validBetDtos = Lists.newArrayList();
            for (Object obj : hits) {
                Map objmap = (Map) obj;
                RebateValidBetDto validBetDto = JSON.parseObject(objmap.get("_source").toString(), RebateValidBetDto.class);
                validBetDtos.add(validBetDto);
            }
            return validBetDtos;
        } catch (Exception e) {
            log.error("三公获取会员所有投注额失败", e);
        }
        return null;
    }

    private List<String> getValidBetAccounts(List<String> sitePrefix, String startTime, String endTime, String isRebate) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termsQuery("sitePrefix", sitePrefix));
        query.must(QueryBuilders.termsQuery("isRebate", isRebate));
        if (nonNull(startTime)) {
            query.must(QueryBuilders.rangeQuery("payoutTime").gte(startTime).lt(endTime));
        }
        TermsAggregationBuilder agg = AggregationBuilders
                .terms("userName").field("userName")
                .size(ElasticSearchConstant.SEARCH_COUNT);

        SearchRequestBuilder searchRequestBuilder =
                connection.client.prepareSearch("report")
                        .setQuery(query).addAggregation(agg);
        try {
            String str = searchRequestBuilder.toString();
            Response response = connection.restClient_Read.performRequest(
                    "GET", "/" + ElasticSearchConstant.RPT_WATER
                            + "/" + ElasticSearchConstant.RPT_BET_WATERR_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"),
                    new NStringEntity(str, ContentType.APPLICATION_JSON));

            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            Map validBet = (Map) ((Map) map.get("aggregations")).get("userName");
            if (nonNull(validBet)) {
                List<String> loginNames = Lists.newArrayList();
                for (Object obj : (JSONArray) validBet.get("buckets")) {
                    Map objMap = (Map) obj;
                    loginNames.add(objMap.get("key").toString());
                }
                return loginNames;
            }
        } catch (Exception e) {
            log.error("获取三公会员出错", e);
        }
        return null;
    }
}
