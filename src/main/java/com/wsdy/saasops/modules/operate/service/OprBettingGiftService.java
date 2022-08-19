package com.wsdy.saasops.modules.operate.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ActivityConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dto.*;
import com.wsdy.saasops.modules.operate.entity.*;
import com.wsdy.saasops.modules.operate.mapper.GameMapper;
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
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class OprBettingGiftService {

    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private ElasticSearchConnection_Read connection;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private OprMixActivityService oprMixActivityService;
    @Autowired
    private TGmGameService tGmGameService;
    /**
     * 投就送
     * @param actActivity
     * @param accountId
     * @param ip
     */
    public void applyBettingGiftBonus(OprActActivity actActivity, int accountId, String ip){
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        // 规则校验
        BettingGiftDto giftDto = jsonUtil.fromJson(actActivity.getRule(),BettingGiftDto.class);
        if(isNull(giftDto) || Collections3.isEmpty(giftDto.getBettingGiftRuleDtos())){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // 校验姓名等信息
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, giftDto.getIsName(),
                giftDto.getIsBank(), giftDto.getIsMobile(), false, giftDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }

        // 获取统计周期时间
        Integer drawType = giftDto.getDrawType();
        Map<String,String> bonusTimeMap = getStartTimeAndEndTime(drawType,0);
        // 获取统计周期是否领取过投就送
        OprActBonus bonus = getLastBonus(actActivity.getId(),mbrAccount.getId(),bonusTimeMap);
        if(nonNull(bonus)){
            throw new R200Exception(ActivityConstants.CLAIMED);
        }

        // 获得计算时间
        Map<String,String> timeMap = getStartTimeAndEndTime(drawType,1);
        // 获取满足条件的规则
        BettingGiftRuleDto ruleDto = checkoutBettingGiftValidBet(giftDto, mbrAccount, timeMap, false);
        if (isNull(ruleDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        accountBettingGift(ruleDto, actActivity, mbrAccount, ip, OrderConstants.ACTIVITY_BETTINGGIFT,null);
    }

    /**
     * 检查是否可领取投就送
     * @param actActivity
     * @param accountId
     * @param ip
     */
    public OprActActivity checkBettingGiftBonus(OprActActivity actActivity, int accountId, String ip){
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        // 规则校验
        BettingGiftDto giftDto = jsonUtil.fromJson(actActivity.getRule(),BettingGiftDto.class);
        if(isNull(giftDto) || Collections3.isEmpty(giftDto.getBettingGiftRuleDtos())){
            log.info("检查会员{}是否可领活动，活动ID{}，活动规则未配置", mbrAccount.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        // 校验姓名等信息
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, giftDto.getIsName(),
                giftDto.getIsBank(), giftDto.getIsMobile(), false, giftDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            log.info("检查会员{}是否可领活动，活动ID{}，会员未完善信息", mbrAccount.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }

        // 获取统计周期时间
        Integer drawType = giftDto.getDrawType();
        Map<String,String> bonusTimeMap = getStartTimeAndEndTime(drawType,0);
        // 获取统计周期是否领取过投就送
        OprActBonus bonus = getLastBonus(actActivity.getId(),mbrAccount.getId(),bonusTimeMap);
        if(nonNull(bonus)){
            log.info("检查会员{}是否可领活动，活动ID{}，会员周期内已领取该活动", mbrAccount.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }

        // 获得计算时间
        Map<String,String> timeMap = getStartTimeAndEndTime(drawType,1);
        // 获取满足条件的规则
        BettingGiftRuleDto ruleDto = checkoutBettingGiftValidBet(giftDto, mbrAccount, timeMap, false);
        if (isNull(ruleDto)) {
            log.info("检查会员{}是否可领活动，活动ID{}，会员不满足规则", mbrAccount.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }

        actActivity.setCanApply(Constants.EVNumber.one);
        actActivity.setCanApplyBonus(ruleDto.getDonateAmount());
        actActivity.setActivityAlready(ruleDto.getValidBet());
        return actActivity;
    }


    /**
     *  投就送-获取投注和存款满足条件的规则
     * @param giftDto           投就送dto
     * @param mbrAccount        会员
     * @param timeMap           计算时间map
     * @param isMixActivity     是否是混合活动-投就送子规则计算 true 混合规则 false 单一投就送
     * @return
     */
    public BettingGiftRuleDto checkoutBettingGiftValidBet(BettingGiftDto giftDto,MbrAccount mbrAccount, Map<String,String> timeMap, boolean isMixActivity){
        // 获取投注范围
        List<AuditCat> auditCats = giftDto.getAuditCats();
        if(Collections3.isEmpty(auditCats)){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // 获取catId和catCode的map,转化catId 到gameCategory
        Map<Integer, String> catMap = tGmGameService.getCatCodeMap();

        // 总的有效投注
        BigDecimal validBet = BigDecimal.ZERO;

        // 获取统计范围内的有效投注
        for(AuditCat auditCat:auditCats) {
            if(Collections3.isNotEmpty(auditCat.getDepots())){
                // 获得平台id list
                List<Integer> ids = auditCat.getDepots().stream().map(AuditDepot::getDepotId).collect(Collectors.toList());
                if(Collections3.isNotEmpty(ids)) {
                    // 按类别计算勾选平台的有效投注
                    String gameCategory = catMap.get(auditCat.getCatId());
                    if(isMixActivity){      // 混合活动-投就送子规则  查es
                        // 平台ids转化为平台codes
                        List<String> depostCodes = gameMapper.getDepotCodesByIds(ids);
                        // 获取有效投注
                        BigDecimal catValidBet = oprMixActivityService.getValidBetTotal(timeMap,mbrAccount,gameCategory.toLowerCase(),depostCodes);
                        validBet = validBet.add(catValidBet);
                    }else{  // 投就送  查rpt
                        BigDecimal catValidBet = mbrMapper.findValidBetTotalByDepotIds(mbrAccount.getId(), gameCategory, ids, timeMap.get("startTime"), timeMap.get("endTime"));
                        validBet = validBet.add(catValidBet);
                    }
                }
            }
        }
        List<BettingGiftRuleDto> ruleDtos = giftDto.getBettingGiftRuleDtos();
        ruleDtos.sort((r1, r2) -> r2.getValidBetMin().compareTo(r1.getValidBetMin()));//按有效投注倒序

        //仅筛选出投注额满足要求得规则
        final BigDecimal validBet1 = validBet;
        List<BettingGiftRuleDto> validBetRuleDtos = ruleDtos.stream().filter(dto -> validBet1.compareTo(dto.getValidBetMin()) >= 0)
                .collect(Collectors.toList());

        // 查出统计时间内的审核通过的总存款
        BigDecimal depositAmount = fundMapper.sumFundDepositByAccountId(mbrAccount.getId(),timeMap.get("startTime"),timeMap.get("endTime"));

        // 获取符合存款要求的规则
        BettingGiftRuleDto matchedRuleDto = null;
        for(BettingGiftRuleDto ruleDto:validBetRuleDtos){
            ruleDto.setValidBet(validBet);
            if (checkBettingGiftDeposit(ruleDto, depositAmount)) {
                matchedRuleDto = ruleDto;
                break;
            }
        }
        return matchedRuleDto;
    }

    public OprActBonus getLastBonus(Integer actId,Integer accountId,Map<String,String> timeMap){
        String startTime = timeMap.get("startTime");
        String endTime = timeMap.get("endTime");
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(actId);
        bonus.setAccountId(accountId);
        bonus.setStartTime(startTime);
        bonus.setEndTime(endTime);
        bonus = operateActivityMapper.findLastBonus(bonus);
        return bonus;
    }

    public Boolean checkBettingGiftDeposit(BettingGiftRuleDto ruleDto,BigDecimal depositAmount){

        Integer depositAmountType = ruleDto.getDepositAmountType();
        BigDecimal depositAmountMin = ruleDto.getDepositMin();
        if(Constants.EVNumber.zero == depositAmountType){//不限制存款金额
            ruleDto.setDepositAmount(BigDecimal.ZERO);
            return true;
        }else{//限制存款金额，校验
            if(depositAmount.compareTo(depositAmountMin) >= 0){
                ruleDto.setDepositAmount(depositAmount);
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param rule 周期
     * @param past 1上一周期/0本周期/-1下一周期
     * @return
     */
    public Map<String,String> getStartTimeAndEndTime(Integer rule,Integer past){
        Map<String,String> timeMap = new HashMap<>(2);
        String startTime = null;
        String endTime = null;
        if(isNull(rule) || Constants.EVNumber.zero == rule){
            startTime = DateUtil.getPastDate(past,DateUtil.FORMAT_10_DATE)+ " " +DateUtil.DATE_STATE;
            endTime = DateUtil.getPastDate(past-1,DateUtil.FORMAT_10_DATE)+ " " +DateUtil.DATE_STATE;
        }else if(Constants.EVNumber.one == rule){
            startTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME,past,0);//上周第一天
            endTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME,past-1,0);//本周第一天
        }else if(Constants.EVNumber.two == rule){
            startTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME,past,0);//上月第一天
            endTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME,past-1,0);//本月第一天
        }
        timeMap.put("startTime",startTime);
        timeMap.put("endTime",endTime);
        return timeMap;
    }

    /**
     * 投就送处理并生成红利
     * @param ruleDto               投就送规则dto
     * @param actActivity           活动
     * @param account               会员
     * @param ip                    会员申请ip
     * @param financialCode         活动财务code
     * @param subRuleTmplCode       混合活动-投就送子规则code
     */
    public void accountBettingGift(BettingGiftRuleDto ruleDto,OprActActivity actActivity, MbrAccount account, String ip,String financialCode, String subRuleTmplCode){
        OprActBonus bonus = actActivityCastService.setOprActBonus(account.getId(), account.getLoginName(), actActivity.getId(),
                null, null, actActivity.getRuleId());
        bonus.setScope(null);
        bonus.setIp(ip);
        bonus.setDevSource(account.getLoginSource());
        bonus.setValidBet(ruleDto.getValidBet());
        int multipleWater = nonNull(ruleDto.getMultipleWater()) ? ruleDto.getMultipleWater().intValue() : 0;
        bonus.setDiscountAudit(new BigDecimal(multipleWater));
        bonus.setBonusAmount(ruleDto.getDonateAmount());
        bonus.setDepositedAmount(ruleDto.getDepositAmount());
        bonus.setAuditAmount(auditAccountService.getAddAuditAmount(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        bonus.setSource(Constants.EVNumber.zero);
        bonus.setCreateUser(account.getLoginName());
        bonus.setSubRuleTmplCode(subRuleTmplCode);      // 混合活动子规则code 存就送AQ0000003 投就送AQ0000012 救援金AQ0000004 其他AQ0000015
        // 插入bonus
        actBonusMapper.insert(bonus);

        // 判断是否需要审核，不审核直接发放红利
        if (actActivity.getIsAudit() == Constants.EVNumber.zero) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            bonus.setDepositedAmount(null);
            actActivityCastService.auditOprActBonus(bonus, financialCode, actActivity.getActivityName(), Boolean.TRUE);
        }
    }

    public Integer checkoutBettingGiftStatus(OprActActivity actActivity, int accountId){
        BettingGiftDto giftDto = jsonUtil.fromJson(actActivity.getRule(), BettingGiftDto.class);
        if(isNull(giftDto) || Collections3.isEmpty(giftDto.getBettingGiftRuleDtos())){
            return Constants.EVNumber.four;
        }

        List<BettingGiftRuleDto> ruleDtos = giftDto.getBettingGiftRuleDtos();
        //设置活动最大赠送金额和流水倍数
        setBettingGiftAmountMax(actActivity,ruleDtos);
        return Constants.EVNumber.one;
    }

    public void setBettingGiftAmountMax(OprActActivity actActivity,List<BettingGiftRuleDto> ruleDtos){

        if (Collections3.isNotEmpty(ruleDtos)) {
            Collections.sort(ruleDtos,Comparator.comparing(BettingGiftRuleDto::getValidBetMin).reversed());
            BettingGiftRuleDto ruleDtoMax = ruleDtos.get(0);
            actActivity.setDonateType(Constants.EVNumber.one);
            actActivity.setAmountMax(ruleDtoMax.getDonateAmount());
            actActivity.setMultipleWater(ruleDtoMax.getMultipleWater());

            BettingGiftRuleDto ruleDtoMin = ruleDtos.get(ruleDtos.size()-1);
            actActivity.setAmountMin(ruleDtoMin.getDepositMin());
            actActivity.setValidBet(ruleDtoMin.getValidBetMin());
        }
    }


    public Map getValidBet(List<String> sitePrefix, String username, String startTime, String endTime) {

        Map<String, Object> rsmap = new HashMap<>();
        try {
            BoolQueryBuilder query = QueryBuilders.boolQuery();
            query.must(QueryBuilders.termsQuery("userName", username));
            query.must(QueryBuilders.termsQuery("sitePrefix", sitePrefix));
            query.must(QueryBuilders.rangeQuery("payoutTime").gte(DateUtil.formatEsDate(startTime))
                    .lt(DateUtil.formatEsDate(endTime)));
            SumAggregationBuilder betaggs = AggregationBuilders.sum("bet").field("bet");
            SumAggregationBuilder validbetaggs = AggregationBuilders.sum("validBet").field("validBet");
            SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
            searchRequestBuilder.addAggregation(betaggs);
            searchRequestBuilder.addAggregation(validbetaggs);
            searchRequestBuilder.setQuery(query);

            Response response = connection.restClient_Read.performRequest("GET", "/" +
                            ElasticSearchConstant.RPT_WATER + "/" + ElasticSearchConstant.RPT_BET_WATERR_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(),
                            ContentType.APPLICATION_JSON));
            log.info(searchRequestBuilder.toString());
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            Integer counts = (Integer) ((Map) map.get("hits")).get("total");
            if (null != (Map) map.get("aggregations")) {
                BigDecimal bet = (BigDecimal) ((Map) ((Map) map.get("aggregations")).get("bet")).get("value");
                BigDecimal validbet = (BigDecimal) ((Map) ((Map) map.get("aggregations")).get("validBet")).get("value");
                rsmap.put("counts", counts);
                //总投注
                rsmap.put("bet", bet.setScale(2, BigDecimal.ROUND_DOWN));
                //有效投注
                rsmap.put("validBet", validbet.setScale(2, BigDecimal.ROUND_HALF_UP));
            } else {
                rsmap.put("counts", counts);
                //总投注
                rsmap.put("bet", BigDecimal.ZERO);
                //有效投注
                rsmap.put("validBet", BigDecimal.ZERO);
            }
            return rsmap;
        } catch (Exception e) {
            log.error("getValidBet==" + e);
            throw new RRException("查询异常!");
        }
    }
}

