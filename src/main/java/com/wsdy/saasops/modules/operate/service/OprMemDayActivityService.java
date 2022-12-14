package com.wsdy.saasops.modules.operate.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_10_DATE;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.dayForWeek;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static com.wsdy.saasops.common.utils.DateUtil.getDayMonthYear;
import static java.util.Objects.isNull;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ActivityConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.base.mapper.BaseMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dto.MemDayGiftDto;
import com.wsdy.saasops.modules.operate.dto.MemDayRuleScopeDto;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class OprMemDayActivityService {
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    BaseMapper baseMapper;
    @Autowired
    private ElasticSearchConnection_Read connection;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private OprAppDownloadActivityService oprAppDownloadActivityService;

    private static final String defaultsdf = "yyyy-MM-dd HH:mm:ss";
    private static final String sdf = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public Boolean checkoutMemDayGiftBonus(OprActActivity actActivity, MbrAccount mbrAccount) {
        // ?????????????????????????????????
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(actActivity.getId());
        bonus.setAccountId(mbrAccount.getId());
        bonus.setStartTime(DateUtil.getTodayStartWithoutTime(FORMAT_10_DATE));
        bonus.setEndTime(DateUtil.getTodayEndWithoutTime(FORMAT_10_DATE));
        int count = operateActivityMapper.findBounsCount(bonus);    // ???????????????????????????????????????????????????
        if (count > 0) { //??????????????????
            return false;
        }
        return true;
    }

    public Integer checkoutMemDayGiftStatus(OprActActivity actActivity, int accountId) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
//        if(oprActActivityService.isBlackList(accountId, TOpActtmpl.memDayGiftCode)||oprActActivityService.isBlackList(accountId, TOpActtmpl.allActivityCode)){
//            log.info(mbrAccount.getLoginName()+"?????????????????????????????????");
//            return Constants.EVNumber.four;
//        }
//        //???????????????????????????????????????
//        if (oprActActivityService.valAgentBackList(mbrAccount,TOpActtmpl.memDayGiftCode)){
//            log.info(mbrAccount.getLoginName()+"??????????????????????????????????????????");
//            return Constants.EVNumber.four;
//        }
//        //???????????????????????????????????????
//        if (oprActActivityService.valAgentBackList(mbrAccount,TOpActtmpl.allActivityCode)){
//            log.info(mbrAccount.getLoginName()+"???????????????????????????????????????");
//            return Constants.EVNumber.four;
//        }
        MemDayGiftDto giftDto = jsonUtil.fromJson(actActivity.getRule(), MemDayGiftDto.class);
        if (isNull(giftDto)) {
            return Constants.EVNumber.four;
        }
        // ??????????????????
        String isAccountMsg = oprActActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, giftDto.getIsName(),
                giftDto.getIsBank(), giftDto.getIsMobile(), false, false);
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            return Constants.EVNumber.four;
        }

        // ?????????????????????
        Boolean isBonus = checkoutMemDayGiftBonus(actActivity, mbrAccount);
        if (!isBonus) {
            return Constants.EVNumber.four;
        }

        // ?????????????????????
        Boolean memDayCheckout = checkoutMemDay(giftDto);
        if (!memDayCheckout) {
            return Constants.EVNumber.four;
        }

        // ??????????????????
        MemDayRuleScopeDto ruleScopeDto = getRuleScopeDtos(giftDto.getRuleScopeDtos(), mbrAccount.getActLevelId(), giftDto.getScope());
        if (isNull(ruleScopeDto)) {
            return Constants.EVNumber.four;
        }
        // ????????????
        Boolean depositCheckout = oprAppDownloadActivityService.checkoutRuleDeposit(mbrAccount, ruleScopeDto);
        if (!depositCheckout) {
            return Constants.EVNumber.four;
        }
        // ????????????
        Boolean validBetCheckout = checkoutRuleValidBet(mbrAccount, ruleScopeDto);
        if (!validBetCheckout) {
            return Constants.EVNumber.four;
        }
        // ?????????????????????????????????????????????(??????????????????)
        // ?????????????????????????????????
        if (Objects.isNull(ruleScopeDto.getDonateAmount()) || Objects.isNull(ruleScopeDto.getMultipleWater())) {
            return Constants.EVNumber.four;
        }
        setMemDayGiftAmountMax(actActivity, ruleScopeDto);
        return Constants.EVNumber.one;
    }

    public void setMemDayGiftAmountMax(OprActActivity actActivity, MemDayRuleScopeDto giftDto) {
        actActivity.setDonateType(Constants.EVNumber.one);          // ??????????????????
        actActivity.setMultipleWater(Objects.isNull(giftDto.getMultipleWater()) ? Double.valueOf(0) : giftDto.getMultipleWater());   // ????????????
        actActivity.setAmountMax(Objects.isNull(giftDto.getDonateAmount()) ? BigDecimal.ZERO : giftDto.getDonateAmount());        // ????????????
        actActivity.setAmountMin(Objects.isNull(giftDto.getDepositMin()) ? BigDecimal.ZERO : giftDto.getDepositMin());          // ??????????????????
        actActivity.setValidBet(Objects.isNull(giftDto.getValidBetMin()) ? BigDecimal.ZERO : giftDto.getValidBetMin());          // ??????????????????
    }

    public void applyMemDayGift(OprActActivity actActivity, int accountId, String ip) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        MemDayGiftDto giftDto = jsonUtil.fromJson(actActivity.getRule(), MemDayGiftDto.class);
        if (isNull(giftDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // ??????????????????
        String isAccountMsg = oprActActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, giftDto.getIsName(),
                giftDto.getIsBank(), giftDto.getIsMobile(), false, false);
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }
        // ??????????????????
        Boolean isBonus = checkoutMemDayGiftBonus(actActivity, mbrAccount);
        if (!isBonus) {
            throw new R200Exception(ActivityConstants.CLAIMED);
        }
        // ?????????????????????
        Boolean memDayCheckout = checkoutMemDay(giftDto);
        if (!memDayCheckout) {
            throw new R200Exception(ActivityConstants.MEMDAY_TIME_NOT_FIT);
        }
        // ??????????????????
        MemDayRuleScopeDto ruleScopeDto = getRuleScopeDtos(giftDto.getRuleScopeDtos(), mbrAccount.getActLevelId(), giftDto.getScope());
        if (isNull(ruleScopeDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // ????????????
        Boolean depositCheckout = oprAppDownloadActivityService.checkoutRuleDeposit(mbrAccount, ruleScopeDto);
        if (!depositCheckout) {
            throw new R200Exception(ActivityConstants.MEMDAY_DEPOSIT_NOT_FIT);
        }
        // ????????????
        Boolean validBetCheckout = checkoutRuleValidBet(mbrAccount, ruleScopeDto);
        if (!validBetCheckout) {
            throw new R200Exception(ActivityConstants.MEMDAY_VALID_BET_NOT_FIT);
        }

        // d??????????????????
        // d?????????????????????????????????
        if (Objects.isNull(ruleScopeDto.getDonateAmount()) || Objects.isNull(ruleScopeDto.getMultipleWater())) {
            throw new R200Exception(ActivityConstants.MEMDAY_NOT_FIT_NULL);
        }
        accountMemDayGift(ruleScopeDto, actActivity, mbrAccount, ip,
                OrderConstants.ACTIVITY_MEMDAY, Constants.EVNumber.zero, mbrAccount.getLoginName());
    }

    // ???????????????
    public Boolean checkoutMemDay(MemDayGiftDto giftDto) {
        Integer memDayType = giftDto.getMemDayType();
        List<Integer> validDates = giftDto.getValidDates();
        if (Integer.valueOf(Constants.EVNumber.zero).equals(memDayType)) {    // ??????
            // ?????????????????????????????????
            int forWeek = dayForWeek(getCurrentDate(FORMAT_10_DATE));
            for (Integer date : validDates) {
                if (date.equals(forWeek)) {
                    return true;
                }
            }
        }
        if (Integer.valueOf(Constants.EVNumber.one).equals(memDayType)) { // ??????
            // ?????????????????????
            int forMonth = getDayMonthYear(getCurrentDate(FORMAT_10_DATE), Constants.dayMonthYear.day);
            for (Integer date : validDates) {
                if (date.equals(forMonth)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ????????????
    public Boolean checkoutRuleValidBet(MbrAccount mbrAccount, MemDayRuleScopeDto ruleScopeDto) {
        if (Integer.valueOf(Constants.EVNumber.zero).equals(ruleScopeDto.getValidBetType())) {
            return true;
        }
        // ?????????????????????
        BigDecimal validBetMin = ruleScopeDto.getValidBetMin();
        if (validBetMin == null) {
        	validBetMin = BigDecimal.ZERO;
        }
        List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(CommonUtil.getSiteCode());
        
        if (!Objects.isNull(validBetMin) && validBetMin.compareTo(BigDecimal.ZERO) > 0) {
	        // ??????????????????
	        BigDecimal betBigDecimal = getValidVet(sitePrefix, mbrAccount.getLoginName(), DateUtil.getTodayStart(FORMAT_18_DATE_TIME), DateUtil.getTodayEnd(FORMAT_18_DATE_TIME));
	        if (betBigDecimal.compareTo(validBetMin) < 0) {
	            return false;
	        } 
	        ruleScopeDto.setValidBet(betBigDecimal);
        }
        
        // ?????????????????????
        BigDecimal lastValidBetMin = ruleScopeDto.getLastValidBetMin();
        if (!Objects.isNull(lastValidBetMin) && lastValidBetMin.compareTo(BigDecimal.ZERO) > 0) {
        	// ??????????????????
        	BigDecimal betBigDecimal = getValidVet(sitePrefix, mbrAccount.getLoginName(),
        			DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME, 1, 0),
                    DateUtil.getEndOfMonth(DateUtil.FORMAT_18_DATE_TIME, 1, 0));
        	if (betBigDecimal.compareTo(lastValidBetMin) < 0) {
        		return false;
        	} 
        	ruleScopeDto.setLastValidBetMin(lastValidBetMin);
        }
        return true;
    }

    public MemDayRuleScopeDto getRuleScopeDtos(List<MemDayRuleScopeDto> ruleScopeDtos, Integer actLevelId, Integer scope) {
        if (Collections3.isEmpty(ruleScopeDtos)) {
            return null;
        }
        if (scope == Constants.EVNumber.zero) {
            return ruleScopeDtos.get(0);
        }
        return ruleScopeDtos.stream()
                .filter(rs -> rs.getActLevelId() == actLevelId)
                .findFirst().orElse(null);
    }

    public BigDecimal getValidVet(List<String> sitePrefix, String username, String startTime, String endTime) {
        try {
            BoolQueryBuilder query = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termsQuery("userName", username))
                    .must(QueryBuilders.termsQuery("sitePrefix", sitePrefix))
                    .must(QueryBuilders.boolQuery());
            if (startTime != null && endTime != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
                SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
                query.must(QueryBuilders.rangeQuery("payoutTime").gte(dateFormatSdf.format(dateFormat.parse(startTime))).
                        lt(dateFormatSdf.format(dateFormat.parse(endTime))));
            }

            SearchRequestBuilder searchRequestBuilder =
                    connection.client.prepareSearch("report")
                            .setQuery(query).addAggregation(
                                    AggregationBuilders.sum("validBet").field("validBet"));

            String str = searchRequestBuilder.toString();
            Response response = connection.restClient_Read.performRequest(
                    "GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/"
                            + ElasticSearchConstant.REPORT_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"),
                    new NStringEntity(str, ContentType.APPLICATION_JSON));

            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            Map validBet = (Map) ((Map) map.get("aggregations")).get("validBet");
            if (Objects.nonNull(validBet)) {
                return new BigDecimal(validBet.get("value").toString());
            }
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
        }
        return BigDecimal.ZERO;
    }

    // ???????????????????????????
    public void accountMemDayGift(MemDayRuleScopeDto ruleDto, OprActActivity actActivity,
                                  MbrAccount account, String ip, String financialCode, Integer source, String createUser) {
        OprActBonus bonus = oprActActivityCastService.setOprActBonus(account.getId(), account.getLoginName(), actActivity.getId(),
                null, null, actActivity.getRuleId());
        bonus.setScope(null);
        bonus.setCreateUser(account.getLoginName());
        bonus.setIp(ip);
        bonus.setDevSource(account.getLoginSource());
        bonus.setValidBet(ruleDto.getValidBet());
        bonus.setDiscountAudit(new BigDecimal(ruleDto.getMultipleWater()));
        bonus.setBonusAmount(ruleDto.getDonateAmount());
        bonus.setDepositedAmount(ruleDto.getDepositAmount());
        bonus.setAuditAmount(auditAccountService.getAddAuditAmount(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        bonus.setSource(source);
        bonus.setCreateUser(createUser);
        actBonusMapper.insert(bonus);
        if (actActivity.getIsAudit() == Constants.EVNumber.zero) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            bonus.setDepositedAmount(null);
            oprActActivityCastService.auditOprActBonus(bonus, financialCode, actActivity.getActivityName(), Boolean.TRUE);
        }

    }
}
