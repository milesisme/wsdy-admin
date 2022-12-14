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
     * ?????????
     * @param actActivity
     * @param accountId
     * @param ip
     */
    public void applyBettingGiftBonus(OprActActivity actActivity, int accountId, String ip){
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        // ????????????
        BettingGiftDto giftDto = jsonUtil.fromJson(actActivity.getRule(),BettingGiftDto.class);
        if(isNull(giftDto) || Collections3.isEmpty(giftDto.getBettingGiftRuleDtos())){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // ?????????????????????
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, giftDto.getIsName(),
                giftDto.getIsBank(), giftDto.getIsMobile(), false, giftDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }

        // ????????????????????????
        Integer drawType = giftDto.getDrawType();
        Map<String,String> bonusTimeMap = getStartTimeAndEndTime(drawType,0);
        // ??????????????????????????????????????????
        OprActBonus bonus = getLastBonus(actActivity.getId(),mbrAccount.getId(),bonusTimeMap);
        if(nonNull(bonus)){
            throw new R200Exception(ActivityConstants.CLAIMED);
        }

        // ??????????????????
        Map<String,String> timeMap = getStartTimeAndEndTime(drawType,1);
        // ???????????????????????????
        BettingGiftRuleDto ruleDto = checkoutBettingGiftValidBet(giftDto, mbrAccount, timeMap, false);
        if (isNull(ruleDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        accountBettingGift(ruleDto, actActivity, mbrAccount, ip, OrderConstants.ACTIVITY_BETTINGGIFT,null);
    }

    /**
     * ??????????????????????????????
     * @param actActivity
     * @param accountId
     * @param ip
     */
    public OprActActivity checkBettingGiftBonus(OprActActivity actActivity, int accountId, String ip){
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        // ????????????
        BettingGiftDto giftDto = jsonUtil.fromJson(actActivity.getRule(),BettingGiftDto.class);
        if(isNull(giftDto) || Collections3.isEmpty(giftDto.getBettingGiftRuleDtos())){
            log.info("????????????{}???????????????????????????ID{}????????????????????????", mbrAccount.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        // ?????????????????????
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, giftDto.getIsName(),
                giftDto.getIsBank(), giftDto.getIsMobile(), false, giftDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            log.info("????????????{}???????????????????????????ID{}????????????????????????", mbrAccount.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }

        // ????????????????????????
        Integer drawType = giftDto.getDrawType();
        Map<String,String> bonusTimeMap = getStartTimeAndEndTime(drawType,0);
        // ??????????????????????????????????????????
        OprActBonus bonus = getLastBonus(actActivity.getId(),mbrAccount.getId(),bonusTimeMap);
        if(nonNull(bonus)){
            log.info("????????????{}???????????????????????????ID{}????????????????????????????????????", mbrAccount.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }

        // ??????????????????
        Map<String,String> timeMap = getStartTimeAndEndTime(drawType,1);
        // ???????????????????????????
        BettingGiftRuleDto ruleDto = checkoutBettingGiftValidBet(giftDto, mbrAccount, timeMap, false);
        if (isNull(ruleDto)) {
            log.info("????????????{}???????????????????????????ID{}????????????????????????", mbrAccount.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }

        actActivity.setCanApply(Constants.EVNumber.one);
        actActivity.setCanApplyBonus(ruleDto.getDonateAmount());
        actActivity.setActivityAlready(ruleDto.getValidBet());
        return actActivity;
    }


    /**
     *  ?????????-??????????????????????????????????????????
     * @param giftDto           ?????????dto
     * @param mbrAccount        ??????
     * @param timeMap           ????????????map
     * @param isMixActivity     ?????????????????????-???????????????????????? true ???????????? false ???????????????
     * @return
     */
    public BettingGiftRuleDto checkoutBettingGiftValidBet(BettingGiftDto giftDto,MbrAccount mbrAccount, Map<String,String> timeMap, boolean isMixActivity){
        // ??????????????????
        List<AuditCat> auditCats = giftDto.getAuditCats();
        if(Collections3.isEmpty(auditCats)){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // ??????catId???catCode???map,??????catId ???gameCategory
        Map<Integer, String> catMap = tGmGameService.getCatCodeMap();

        // ??????????????????
        BigDecimal validBet = BigDecimal.ZERO;

        // ????????????????????????????????????
        for(AuditCat auditCat:auditCats) {
            if(Collections3.isNotEmpty(auditCat.getDepots())){
                // ????????????id list
                List<Integer> ids = auditCat.getDepots().stream().map(AuditDepot::getDepotId).collect(Collectors.toList());
                if(Collections3.isNotEmpty(ids)) {
                    // ??????????????????????????????????????????
                    String gameCategory = catMap.get(auditCat.getCatId());
                    if(isMixActivity){      // ????????????-??????????????????  ???es
                        // ??????ids???????????????codes
                        List<String> depostCodes = gameMapper.getDepotCodesByIds(ids);
                        // ??????????????????
                        BigDecimal catValidBet = oprMixActivityService.getValidBetTotal(timeMap,mbrAccount,gameCategory.toLowerCase(),depostCodes);
                        validBet = validBet.add(catValidBet);
                    }else{  // ?????????  ???rpt
                        BigDecimal catValidBet = mbrMapper.findValidBetTotalByDepotIds(mbrAccount.getId(), gameCategory, ids, timeMap.get("startTime"), timeMap.get("endTime"));
                        validBet = validBet.add(catValidBet);
                    }
                }
            }
        }
        List<BettingGiftRuleDto> ruleDtos = giftDto.getBettingGiftRuleDtos();
        ruleDtos.sort((r1, r2) -> r2.getValidBetMin().compareTo(r1.getValidBetMin()));//?????????????????????

        //??????????????????????????????????????????
        final BigDecimal validBet1 = validBet;
        List<BettingGiftRuleDto> validBetRuleDtos = ruleDtos.stream().filter(dto -> validBet1.compareTo(dto.getValidBetMin()) >= 0)
                .collect(Collectors.toList());

        // ????????????????????????????????????????????????
        BigDecimal depositAmount = fundMapper.sumFundDepositByAccountId(mbrAccount.getId(),timeMap.get("startTime"),timeMap.get("endTime"));

        // ?????????????????????????????????
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
        if(Constants.EVNumber.zero == depositAmountType){//?????????????????????
            ruleDto.setDepositAmount(BigDecimal.ZERO);
            return true;
        }else{//???????????????????????????
            if(depositAmount.compareTo(depositAmountMin) >= 0){
                ruleDto.setDepositAmount(depositAmount);
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param rule ??????
     * @param past 1????????????/0?????????/-1????????????
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
            startTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME,past,0);//???????????????
            endTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME,past-1,0);//???????????????
        }else if(Constants.EVNumber.two == rule){
            startTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME,past,0);//???????????????
            endTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME,past-1,0);//???????????????
        }
        timeMap.put("startTime",startTime);
        timeMap.put("endTime",endTime);
        return timeMap;
    }

    /**
     * ??????????????????????????????
     * @param ruleDto               ???????????????dto
     * @param actActivity           ??????
     * @param account               ??????
     * @param ip                    ????????????ip
     * @param financialCode         ????????????code
     * @param subRuleTmplCode       ????????????-??????????????????code
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
        bonus.setSubRuleTmplCode(subRuleTmplCode);      // ?????????????????????code ?????????AQ0000003 ?????????AQ0000012 ?????????AQ0000004 ??????AQ0000015
        // ??????bonus
        actBonusMapper.insert(bonus);

        // ??????????????????????????????????????????????????????
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
        //?????????????????????????????????????????????
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
                //?????????
                rsmap.put("bet", bet.setScale(2, BigDecimal.ROUND_DOWN));
                //????????????
                rsmap.put("validBet", validbet.setScale(2, BigDecimal.ROUND_HALF_UP));
            } else {
                rsmap.put("counts", counts);
                //?????????
                rsmap.put("bet", BigDecimal.ZERO);
                //????????????
                rsmap.put("validBet", BigDecimal.ZERO);
            }
            return rsmap;
        } catch (Exception e) {
            log.error("getValidBet==" + e);
            throw new RRException("????????????!");
        }
    }
}

