package com.wsdy.saasops.modules.operate.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_10_DATE;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_4_DATE;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_5_DATE;
import static com.wsdy.saasops.common.utils.DateUtil.daysBetween;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.base.mapper.BaseMapper;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dto.JbirthdayDto;
import com.wsdy.saasops.modules.operate.dto.JbirthdayInfoDto;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class OprBirthdayActivityService {
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
    
    private static final String defaultsdf = "yyyy-MM-dd HH:mm:ss";
    private static final String sdf = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public Boolean checkoutBirthdayBonus(OprActActivity actActivity, MbrAccount mbrAccount) {
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(actActivity.getId());
        bonus.setAccountId(mbrAccount.getId());
        bonus.setApplicationTimeYear(getCurrentDate(FORMAT_4_DATE));
        int count = operateActivityMapper.findBounsCount(bonus);    // 当天有过非拒绝的申请状态的不能申请
        if (count > 0) { //已经申请活动
            return false;
        }
        return true;
    }

    public Integer checkoutBirthdayActivityStatus(OprActActivity actActivity, int accountId) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
//        if(oprActActivityService.isBlackList(accountId, TOpActtmpl.birthdayCode)||oprActActivityService.isBlackList(accountId, TOpActtmpl.allActivityCode)){
//            log.info(mbrAccount.getLoginName()+"是生日礼金活动黑名单会员");
//            return Constants.EVNumber.four;
//        }
//        //判断是否存在活动代理黑名单
//        if (oprActActivityService.valAgentBackList(mbrAccount,TOpActtmpl.birthdayCode)){
//            log.info(mbrAccount.getLoginName()+"上级代理存在生日礼金活动黑名单");
//            return Constants.EVNumber.four;
//        }
//        //判断是否存在所有活动黑名单
//        if (oprActActivityService.valAgentBackList(mbrAccount,TOpActtmpl.allActivityCode)){
//            log.info(mbrAccount.getLoginName()+"上级代理存在所有活动黑名单");
//            return Constants.EVNumber.four;
//        }
        JbirthdayDto dto = jsonUtil.fromJson(actActivity.getRule(), JbirthdayDto.class);
        if (isNull(dto)) {
            return Constants.EVNumber.four;
        }
        if (nonNull(dto.getDay()) && dto.getDay() > 0) {
            int num = daysBetween(mbrAccount.getRegisterTime(), getCurrentDate(FORMAT_18_DATE_TIME));
            if (num <= dto.getDay()) {
                return Constants.EVNumber.four;
            }
        }

        // 申请条件校验
        String isAccountMsg = oprActActivityCastService.checkoutAccountMsg(mbrAccount,
                Constants.EVNumber.zero, dto.getIsName(),
                dto.getIsBank(), dto.getIsMobile(), dto.getIsMail(), false);
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            return Constants.EVNumber.four;
        }
        String day = getCurrentDate(FORMAT_5_DATE);
        if (org.apache.commons.lang.StringUtils.isEmpty(mbrAccount.getBirthday())){
            return Constants.EVNumber.four;
        }
        String birthday = mbrAccount.getBirthday().substring(5,10);
        if (!day.equals(birthday)) {
            return Constants.EVNumber.four;
        }
        // 是否可领取校验
        Boolean isBonus = checkoutBirthdayBonus(actActivity, mbrAccount);
        if (!isBonus) {
            return Constants.EVNumber.four;
        }
        // 层级条件校验
        JbirthdayInfoDto ruleScopeDto = getRuleScopeDtos(dto.getRuleScopeDtos(), mbrAccount.getActLevelId());
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

    public void setMemDayGiftAmountMax(OprActActivity actActivity, JbirthdayInfoDto ruleScopeDto) {
        actActivity.setDonateType(Constants.EVNumber.one);          // 固定金额类型
        actActivity.setMultipleWater(Objects.isNull(ruleScopeDto.getMultipleWater()) ? Double.valueOf(0) : ruleScopeDto.getMultipleWater());   // 固定流水
        actActivity.setAmountMax(Objects.isNull(ruleScopeDto.getDonateAmount()) ? BigDecimal.ZERO : ruleScopeDto.getDonateAmount());        // 固定金额
        actActivity.setAmountMin(Objects.isNull(ruleScopeDto.getDepositMin()) ? BigDecimal.ZERO : ruleScopeDto.getDepositMin());          // 最小存款金额
        actActivity.setValidBet(Objects.isNull(ruleScopeDto.getValidbetMin()) ? BigDecimal.ZERO : ruleScopeDto.getValidbetMin());          // 最小投注金额
    }

    public void applyBirthday(OprActActivity actActivity, int accountId, String ip) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        JbirthdayDto dto = jsonUtil.fromJson(actActivity.getRule(), JbirthdayDto.class);
        if (isNull(dto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        String isAccountMsg = oprActActivityCastService.checkoutAccountMsg(mbrAccount,
                Constants.EVNumber.zero, dto.getIsName(),
                dto.getIsBank(), dto.getIsMobile(), dto.getIsMail(), false);
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }
        String day = getCurrentDate(FORMAT_5_DATE);
        if (StringUtils.isEmpty(mbrAccount.getBirthday())){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        String birthday = mbrAccount.getBirthday().substring(5,10);
        if (!day.equals(birthday)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // 是否领取校验
        Boolean isBonus = checkoutBirthdayBonus(actActivity, mbrAccount);
        if (!isBonus) {
            throw new R200Exception(ActivityConstants.CLAIMED);
        }

        // 层级条件校验
        JbirthdayInfoDto ruleScopeDto = getRuleScopeDtos(dto.getRuleScopeDtos(), mbrAccount.getActLevelId());
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
        accountBirthday(ruleScopeDto, actActivity, mbrAccount, ip);
    }

    // 校验存款
    public Boolean checkoutRuleDeposit(MbrAccount mbrAccount, JbirthdayInfoDto ruleScopeDto) {
        // 当日累积存款
        BigDecimal depositAmount = fundMapper.sumFundDepositByAccountId(mbrAccount.getId(),
                DateUtil.getTodayStart(FORMAT_10_DATE), DateUtil.getTodayEnd(FORMAT_10_DATE));
        BigDecimal depositAmountMin = ruleScopeDto.getDepositMin();
        if (!Objects.isNull(depositAmountMin) && depositAmount.compareTo(depositAmountMin) >= 0) {
            ruleScopeDto.setDepositAmount(depositAmount);
            return true;
        }
        return false;
    }

    // 校验投注
    public Boolean checkoutRuleValidBet(MbrAccount mbrAccount, JbirthdayInfoDto ruleScopeDto) {
        if (Integer.valueOf(Constants.EVNumber.zero).equals(ruleScopeDto.getValidbetMin())) {
            return true;
        }
        // 当日累积投注
        List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(CommonUtil.getSiteCode());
        BigDecimal betBigDecimal = getValidVet(sitePrefix, mbrAccount.getLoginName(), DateUtil.getTodayStart(FORMAT_10_DATE), DateUtil.getTodayEnd(FORMAT_10_DATE));
        BigDecimal validBetMin = ruleScopeDto.getValidbetMin();
        if (!Objects.isNull(validBetMin) && betBigDecimal.compareTo(validBetMin) >= 0) {
            ruleScopeDto.setValidBet(betBigDecimal);
            return true;
        }
        return false;
    }

    public JbirthdayInfoDto getRuleScopeDtos(List<JbirthdayInfoDto> ruleScopeDtos, Integer actLevelId) {
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

    // 会员日生成红利记录
    private void accountBirthday(JbirthdayInfoDto ruleDto, OprActActivity actActivity, MbrAccount account, String ip) {
        OprActBonus bonus = oprActActivityCastService.setOprActBonus(account.getId(), account.getLoginName(), actActivity.getId(),
                null, null, actActivity.getRuleId());
        bonus.setScope(null);
        bonus.setCreateUser(account.getLoginName());
        bonus.setIp(ip);
        bonus.setDevSource(account.getLoginSource());
        bonus.setValidBet(ruleDto.getValidBet());
        bonus.setDiscountAudit(new BigDecimal(ruleDto.getMultipleWater().intValue()));
        bonus.setBonusAmount(ruleDto.getDonateAmount());
        bonus.setDepositedAmount(ruleDto.getDepositAmount());
        bonus.setAuditAmount(auditAccountService.getAddAuditAmount(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        bonus.setSource(Constants.EVNumber.zero);
        bonus.setCreateUser(account.getLoginName());
        actBonusMapper.insert(bonus);
        if (actActivity.getIsAudit() == Constants.EVNumber.zero) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            bonus.setDepositedAmount(null);
            oprActActivityCastService.auditOprActBonus(bonus, OrderConstants.ACTIVITY_SRLJ, actActivity.getActivityName(), Boolean.TRUE);
        }
    }
}
