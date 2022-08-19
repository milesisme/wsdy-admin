package com.wsdy.saasops.modules.operate.service;

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
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dto.AppDownloadGiftDto;
import com.wsdy.saasops.modules.operate.dto.MemDayRuleScopeDto;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 *  app下载活动
 */
@Slf4j
@Service
@Transactional
public class OprAppDownloadActivityService {
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
    private FundMapper fundMapper;
    @Autowired
    BaseMapper baseMapper;
    @Autowired
    private ElasticSearchConnection_Read connection;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private OprActActivityService oprActActivityService;

    private static final String defaultsdf = "yyyy-MM-dd HH:mm:ss";
    private static final String sdf = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public Boolean checkoutMemDayGiftBonus(OprActActivity actActivity, MbrAccount mbrAccount) {
        // 校验当天是否有申请记录
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(actActivity.getId());
        bonus.setAccountId(mbrAccount.getId());
        bonus.setStartTime(DateUtil.getTodayStartWithoutTime(FORMAT_10_DATE));
        bonus.setEndTime(DateUtil.getTodayEndWithoutTime(FORMAT_10_DATE));
        int count = operateActivityMapper.findBounsCount(bonus);    // 当天有过非拒绝的申请状态的不能申请
        if (count > 0) { //已经申请活动
            return false;
        }
        return true;
    }

    public Integer checkoutMemDayGiftStatus(OprActActivity actActivity, int accountId) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
//        if(oprActActivityService.isBlackList(accountId, TOpActtmpl.appDownloadGiftCode)||oprActActivityService.isBlackList(accountId, TOpActtmpl.allActivityCode)){
//            log.info(mbrAccount.getLoginName()+"是APP下载彩金活动黑名单会员");
//            return Constants.EVNumber.four;
//        }
//        //判断是否存在活动代理黑名单
//        if (oprActActivityService.valAgentBackList(mbrAccount,TOpActtmpl.appDownloadGiftCode)){
//            log.info(mbrAccount.getLoginName()+"上级代理存在APP下载彩金活动黑名单");
//            return Constants.EVNumber.four;
//        }
//        //判断是否存在所有活动黑名单
//        if (oprActActivityService.valAgentBackList(mbrAccount,TOpActtmpl.allActivityCode)){
//            log.info(mbrAccount.getLoginName()+"上级代理存在所有活动黑名单");
//            return Constants.EVNumber.four;
//        }
        AppDownloadGiftDto giftDto = jsonUtil.fromJson(actActivity.getRule(), AppDownloadGiftDto.class);
        if (isNull(giftDto)) {
            return Constants.EVNumber.four;
        }
        // 申请条件校验
        String isAccountMsg = oprActActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, giftDto.getIsName(),
                giftDto.getIsBank(), giftDto.getIsMobile(), false, false);
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            return Constants.EVNumber.four;
        }

        // 是否可领取校验
        Boolean isBonus = checkoutMemDayGiftBonus(actActivity, mbrAccount);
        if (!isBonus) {
            return Constants.EVNumber.four;
        }

        // 层级条件校验
        MemDayRuleScopeDto ruleScopeDto = getRuleScopeDtos(giftDto.getRuleScopeDtos(), mbrAccount.getActLevelId(), giftDto.getScope());
        if (isNull(ruleScopeDto)) {
            return Constants.EVNumber.four;
        }
        // 存款限制
        Boolean depositCheckout = checkoutRuleDeposit(mbrAccount, ruleScopeDto);
        if (!depositCheckout) {
            return Constants.EVNumber.four;
        }
        // 投注限制
        Boolean validBetCheckout = checkoutRuleValidBet(mbrAccount, ruleScopeDto);
        if (!validBetCheckout) {
            return Constants.EVNumber.four;
        }
        // 设置活动最大赠送金额和流水倍数(前端展示数据)
        // 赠送金额和流水倍数校验
        if (Objects.isNull(ruleScopeDto.getDonateAmount()) || Objects.isNull(ruleScopeDto.getMultipleWater())) {
            return Constants.EVNumber.four;
        }
        setMemDayGiftAmountMax(actActivity, ruleScopeDto);
        return Constants.EVNumber.one;
    }

    public void setMemDayGiftAmountMax(OprActActivity actActivity, MemDayRuleScopeDto giftDto) {
        actActivity.setDonateType(Constants.EVNumber.one);          // 固定金额类型
        actActivity.setMultipleWater(Objects.isNull(giftDto.getMultipleWater()) ? Double.valueOf(0) : giftDto.getMultipleWater());   // 固定流水
        actActivity.setAmountMax(Objects.isNull(giftDto.getDonateAmount()) ? BigDecimal.ZERO : giftDto.getDonateAmount());        // 固定金额
        actActivity.setAmountMin(Objects.isNull(giftDto.getDepositMin()) ? BigDecimal.ZERO : giftDto.getDepositMin());          // 最小存款金额
        actActivity.setValidBet(Objects.isNull(giftDto.getValidBetMin()) ? BigDecimal.ZERO : giftDto.getValidBetMin());          // 最小投注金额
    }

    public void applyMemDayGift(OprActActivity actActivity, int accountId, String ip) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        AppDownloadGiftDto giftDto = jsonUtil.fromJson(actActivity.getRule(), AppDownloadGiftDto.class);
        if (isNull(giftDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // 申请条件校验
        String isAccountMsg = oprActActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, giftDto.getIsName(),
                giftDto.getIsBank(), giftDto.getIsMobile(), false, false);
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }
        // 是否领取校验
        Boolean isBonus = checkoutMemDayGiftBonus(actActivity, mbrAccount);
        if (!isBonus) {
            throw new R200Exception(ActivityConstants.CLAIMED);
        }
        // 层级条件校验
        MemDayRuleScopeDto ruleScopeDto = getRuleScopeDtos(giftDto.getRuleScopeDtos(), mbrAccount.getActLevelId(), giftDto.getScope());
        if (isNull(ruleScopeDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // 存款限制
        Boolean depositCheckout = checkoutRuleDeposit(mbrAccount, ruleScopeDto);
        if (!depositCheckout) {
            throw new R200Exception(ActivityConstants.MEMDAY_DEPOSIT_NOT_FIT);
        }
        // 投注限制
        Boolean validBetCheckout = checkoutRuleValidBet(mbrAccount, ruleScopeDto);
        if (!validBetCheckout) {
            throw new R200Exception(ActivityConstants.MEMDAY_VALID_BET_NOT_FIT);
        }

        // 生成红利数据
        // 赠送金额和流水倍数校验
        if (Objects.isNull(ruleScopeDto.getDonateAmount()) || Objects.isNull(ruleScopeDto.getMultipleWater())) {
            throw new R200Exception(ActivityConstants.MEMDAY_NOT_FIT_NULL);
        }
        accountMemDayGift(ruleScopeDto, actActivity, mbrAccount, ip,
                OrderConstants.ACTIVITY_APPDOWNLOAD, Constants.EVNumber.zero, mbrAccount.getLoginName());
    }

    // 校验存款
    public Boolean checkoutRuleDeposit(MbrAccount mbrAccount, MemDayRuleScopeDto ruleScopeDto) {
        if (Integer.valueOf(Constants.EVNumber.zero).equals(ruleScopeDto.getDepositAmountType())) {
            return true;
        }
        MemDayRuleScopeDto memDayRuleScopeDto = null;
        // 上月存款不低于多少
        if (nonNull(ruleScopeDto.getLastDepositMin()) && ruleScopeDto.getLastDepositMin().compareTo(BigDecimal.ZERO) == 1) {
        	memDayRuleScopeDto =
                    fundMapper.sumAndCountFundDeposit(mbrAccount.getId(),
                            DateUtil.getFirstOfMonth(DateUtil.FORMAT_10_DATE, 1, 0),
                            DateUtil.getEndOfMonth(DateUtil.FORMAT_10_DATE, 1, 0));
            if (memDayRuleScopeDto.getLastDepositMin().compareTo(ruleScopeDto.getLastDepositMin()) == -1) {
                return false;
            }
            ruleScopeDto.setDepositAmount(memDayRuleScopeDto.getLastDepositMin());
        }
        // 上月存款次数不低于多少次
        if (ruleScopeDto.getLastDepositMinTimes() != null && ruleScopeDto.getLastDepositMinTimes() > 0) {
        	if (memDayRuleScopeDto == null) {
        		memDayRuleScopeDto = fundMapper.sumAndCountFundDeposit(mbrAccount.getId(),
                                DateUtil.getFirstOfMonth(DateUtil.FORMAT_10_DATE, 1, 0),
                                DateUtil.getEndOfMonth(DateUtil.FORMAT_10_DATE, 1, 0));
        	}
        	int lastDepositMinTimes = memDayRuleScopeDto.getLastDepositMinTimes();
        	if (lastDepositMinTimes < ruleScopeDto.getLastDepositMinTimes()) {
        		return false;
        	}
        	ruleScopeDto.setLastDepositMinTimes(lastDepositMinTimes);
        }
        // 当日累积存款
        BigDecimal depositAmountMin = ruleScopeDto.getDepositMin();
        if (depositAmountMin != null && depositAmountMin.compareTo(BigDecimal.ZERO) == 1) {
        	BigDecimal depositAmount = fundMapper.sumFundDepositByAccountId(mbrAccount.getId(),
        			DateUtil.getTodayStart(FORMAT_10_DATE), DateUtil.getTodayEnd(FORMAT_10_DATE));
        	// 小于配置
        	if (depositAmount.compareTo(depositAmountMin) < 0) {
        		return false;
        	}
        	ruleScopeDto.setDepositAmount(depositAmount);
        }
        return true;
    }

    // 校验投注
    public Boolean checkoutRuleValidBet(MbrAccount mbrAccount, MemDayRuleScopeDto ruleScopeDto) {
        if (Integer.valueOf(Constants.EVNumber.zero).equals(ruleScopeDto.getValidBetType())) {
            return true;
        }
        // 当日累积投注
        List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(CommonUtil.getSiteCode());
        BigDecimal betBigDecimal = getValidVet(sitePrefix, mbrAccount.getLoginName(), DateUtil.getTodayStart(FORMAT_10_DATE), DateUtil.getTodayEnd(FORMAT_10_DATE));
        BigDecimal validBetMin = ruleScopeDto.getValidBetMin();
        if (!Objects.isNull(validBetMin) && betBigDecimal.compareTo(validBetMin) >= 0) {
            ruleScopeDto.setValidBet(betBigDecimal);
            return true;
        }
        return false;
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
            log.error("获取会员所有投注额失败", e);
        }
        return BigDecimal.ZERO;
    }

    //d app生成红利记录
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
