package com.wsdy.saasops.modules.member.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.base.mapper.BaseMapper;
import com.wsdy.saasops.modules.member.dto.WaterValidBetDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dao.OprActWaterBetdataMapper;
import com.wsdy.saasops.modules.operate.dao.OprActWaterMapper;
import com.wsdy.saasops.modules.operate.dto.*;
import com.wsdy.saasops.modules.operate.entity.*;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
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
import static com.wsdy.saasops.common.utils.CommonUtil.adjustScale;
import static com.wsdy.saasops.common.utils.DateUtil.*;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.vipRedenvelopeCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.waterRebatesCode;
import static java.util.Objects.nonNull;


@Slf4j
@Service
public class Ys6WaterCastService {

    public static final String HHMMSS = " 00:00:00";
    @Autowired
    private OperateActivityMapper activityMapper;
    @Autowired
    private ElasticSearchConnection_Read connection;
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private OprActWaterMapper actWaterMapper;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private AuditAccountService accountAuditService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OprActWaterBetdataMapper oprActWaterBetdataMapper;
    @Autowired
    private OprActActivityService actActivityService;


    public void castWaterActivity(String siteCode) {

        List<OprActActivity> activities = getOprActActivitys(null);

        String startTime = formatEsDate(getPastDate(Constants.EVNumber.one, FORMAT_10_DATE) + HHMMSS);
        String endTime = formatEsDate(getPastDate(Constants.EVNumber.zero, FORMAT_10_DATE) + HHMMSS);

        List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(siteCode);
        for (OprActActivity as : activities) {
            JWaterRebatesDto dto = new Gson().fromJson(as.getRule(), JWaterRebatesDto.class);
            int forWeek = dayForWeek(getCurrentDate(FORMAT_10_DATE));
            if (dto.getPeriod() == 1 && forWeek != Constants.EVNumber.one) {
                log.info("活动周结算，今天不是周一【" + as.getId() + "】");
                continue;
            }
            if (dto.getPeriod() == 1 && forWeek == Constants.EVNumber.one) {
                startTime = formatEsDate(getPastDate(Constants.EVNumber.seven, FORMAT_10_DATE) + HHMMSS);
                endTime = formatEsDate(getPastDate(Constants.EVNumber.zero, FORMAT_10_DATE) + HHMMSS);
            }
            List<MbrAccount> accountList = mbrMapper.findAccountAndValidbetList(
                    getPastDate(Constants.EVNumber.eight, FORMAT_10_DATE),
                    getPastDate(Constants.EVNumber.one, FORMAT_10_DATE));
            if (Collections3.isEmpty(accountList)) {
                continue;
            }
            String finalStartTime = startTime;
            String finalEndTime = endTime;
            accountList.forEach(account ->
                    castMbrValidbet(account, siteCode, finalStartTime, finalEndTime, as, dto, sitePrefix));
        }
    }

    public List<OprActActivity> getOprActActivitys(Integer isSelfHelp) {
        OprActActivity activity = new OprActActivity();
        activity.setUseStart(getPastDate(Constants.EVNumber.one, FORMAT_10_DATE));
        activity.setTmplCode(waterRebatesCode);
        activity.setIsSelfHelp(isSelfHelp);
        activity.setIsSelfHelpShow(isSelfHelp);
        return activityMapper.findWaterActivity(activity);
    }


    @Async("waterActivityTaskAsyncExecutor")
    @Transactional
    public void castMbrValidbet(MbrAccount account, String siteCode, String startTime, String endTime, OprActActivity activity, JWaterRebatesDto dto, List<String> sitePrefix) {
        String key = RedisConstants.BATCH_WATER_ACCOUNT + activity.getId() + siteCode + account.getLoginName();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, account.getLoginName(), 200, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            ThreadLocalCache.setSiteCodeAsny(siteCode);

            if(actActivityService.isBlackList(account.getId(),waterRebatesCode)||actActivityService.isBlackList(account.getId(), TOpActtmpl.allActivityCode)){
                log.info(account.getLoginName()+"是返水黑名单会员");
                return;
            }
            //检查被发放的账号是否存在上级代理在黑名单

            //判断是否存在vip活动代理黑名单
            if (actActivityService.valAgentBackList(account,waterRebatesCode)){
                log.info(account.getLoginName()+"上级代理存在返水黑名单");
                return;
            }
            //判断是否存在所有活动黑名单
            if (actActivityService.valAgentBackList(account,TOpActtmpl.allActivityCode)){
                log.info(account.getLoginName()+"上级代理存在所有活动黑名单");
                return;
            }

            String oldStartTime = startTime.toString();
            int countWater = activityMapper.findValidbetCount(activity.getId(), getCurrentDate(FORMAT_10_DATE), Constants.SYSTEM_USER, account.getId());
            if (countWater > 0) {
                log.info("该会员今日已经返水【" + account.getLoginName() + "】,数据库" + "【" + siteCode + "】");
                return;
            }
            List<Integer> waterIds = accountValidBetDetail(account, startTime, endTime, activity, dto, sitePrefix);
            if (waterIds.size() > 0) {
                activity.setWaterStart(oldStartTime);
                activity.setWaterEnd(endTime);
                OprActBonus bonus = insertOprActBonus(account, activity, siteCode, Constants.SYSTEM_USER, waterIds, RedisConstants.OPRACT_WATER_BATCHINFO);
                if (activity.getIsAudit() == Constants.EVNumber.zero && nonNull(bonus)) {
                    bonus.setStatus(Constants.EVNumber.one);
                    bonus.setAuditUser(Constants.SYSTEM_USER);
                    bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    grantOprActBonus(bonus, siteCode);
                }
            }
            redisService.del(key);
        }
        log.info("会员【"+ account.getLoginName() + "】castMbrValidbet====标记自动反水");
    }

    public void grantOprActBonus(OprActBonus bonus, String siteCode) {

        MbrBillDetail billDetail = walletService.castWalletAndBillDetail(bonus.getLoginName(),
                bonus.getAccountId(), OrderConstants.ACTIVITY_WATERBONUS,
                bonus.getBonusAmount(), bonus.getOrderNo().toString(),
                Boolean.TRUE,null,null);
        bonus.setBillDetailId(billDetail.getId());
        actBonusMapper.updateByPrimaryKeySelective(bonus);

        if (bonus.getAuditAmount().compareTo(BigDecimal.ZERO) == 1) {
            accountAuditService.insertAccountAudit(
                    bonus.getAccountId(), null,
                    null, null,
                    bonus.getAuditAmount(), bonus.getBonusAmount(),
                    bonus.getRuleId(), Constants.EVNumber.four);
        }

        BizEvent bizEvent = new BizEvent(this, siteCode,
                bonus.getAccountId(), BizEventType.MEMBER_COMMISSION_SUCCESS);
        bizEvent.setAcvitityMoney(bonus.getBonusAmount());
        applicationEventPublisher.publishEvent(bizEvent);
    }

    public OprActBonus insertOprActBonus(MbrAccount account, OprActActivity activity, String siteCode, String sysUser, List<Integer> waterIds, String prefixKey) {
        OprActWater actWater = activityMapper.findSumAccountWater(account.getId(), waterIds);
        if (nonNull(actWater)) {
            OprActBonus waterBonus = new OprActBonus();
            waterBonus.setStatus(Constants.EVNumber.two);
            waterBonus.setActivityId(activity.getId());
            waterBonus.setAccountId(account.getId());
            waterBonus.setLoginName(account.getLoginName());
            waterBonus.setApplicationTime(getCurrentDate(FORMAT_18_DATE_TIME));
            waterBonus.setValidBet(actWater.getValidBet());
            waterBonus.setBonusAmount(actWater.getAmount());
            waterBonus.setAuditAmount(actWater.getAuditAmount());
            waterBonus.setIsShow(Constants.EVNumber.one);
            waterBonus.setRuleId(activity.getRuleId());
            waterBonus.setCreateUser(sysUser);
            waterBonus.setOrderNo(String.valueOf(new SnowFlake().nextId()));
            waterBonus.setOrderPrefix(OrderConstants.ACTIVITY_AC);
            waterBonus.setFinancialCode(OrderConstants.ACTIVITY_WATERBONUS);
            waterBonus.setWaterdateid(insertOprActWaterBatchInfo(activity, siteCode, prefixKey));
            actBonusMapper.insert(waterBonus);

            activityMapper.updateOprActWater(waterBonus.getId(), waterIds);
            return waterBonus;
        }
        return null;
    }

    public Integer insertOprActWaterBatchInfo(OprActActivity activity, String siteCode, String prefixKey) {
        String waterEnd = StringUtil.substring(activity.getWaterEnd(), 0, 10);
        String waterStart = StringUtil.substring(activity.getWaterStart(), 0, 10);
        String currentDate = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
        while (true) {
            List<OprActWaterBetdata> waterBetdataList = activityMapper.findWaterBetDateByTime(activity.getId(), waterStart, waterEnd);
            if (waterBetdataList.size() > 0) {
                OprActWaterBetdata waterBetdata = new OprActWaterBetdata();
                waterBetdata.setApplicationTime(currentDate);
                waterBetdata.setId(waterBetdataList.get(0).getId());
                oprActWaterBetdataMapper.updateByPrimaryKeySelective(waterBetdata);
                return waterBetdataList.get(0).getId();
            }
            String key = prefixKey + siteCode + activity.getId();
            Boolean isExpired = redisService.setRedisExpiredTimeBo(key, activity.getId(), 5, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isExpired)) {
                OprActWaterBetdata bean = new OprActWaterBetdata();
                try {
                    bean.setActivityId(activity.getId());
                    bean.setApplicationTime(currentDate);
                    bean.setWaterEnd(waterEnd);
                    bean.setWaterStart(waterStart);
                    oprActWaterBetdataMapper.insert(bean);
                    return bean.getId();
                } finally {
                    redisService.del(key);
                }
            }
        }
    }

    private List<Integer> accountValidBetDetail(MbrAccount account, String startTime, String endTime,
                                                OprActActivity activity, JWaterRebatesDto dto, List<String> sitePrefix) {
        List<Integer> waterIds = Lists.newArrayList();

        List<JWaterRebatesLevelDto> levelDtoList = getWaterRebatesLevelList(account,dto.getRebatesNeDto());//获取代理返水规则
        if (Collections3.isNotEmpty(levelDtoList)) {

            Optional<JWaterRebatesLevelDto> rebatesLevelDto = levelDtoList.stream().filter(e ->
                    e.getAccountLevel().equals(account.getActLevelId())).findAny();

            if (rebatesLevelDto.isPresent()) {
                JWaterRebatesLevelDto jWaterRebatesLevelDto = rebatesLevelDto.get();
                if (Collections3.isNotEmpty(jWaterRebatesLevelDto.getRebatesLevelListDtos())) {

                    Map<Integer, List<JWaterRebatesLevelListDto>> rebatesLevelListGroupingBy =
                            jWaterRebatesLevelDto.getRebatesLevelListDtos().stream().collect(
                                    Collectors.groupingBy(JWaterRebatesLevelListDto::getCatId));

                    for (Integer catIdKey : rebatesLevelListGroupingBy.keySet()) {
                        List<JWaterRebatesLevelListDto> rebatesLevelList = rebatesLevelListGroupingBy.get(catIdKey);

                        List<Integer> depotIds = rebatesLevelList.stream().map(JWaterRebatesLevelListDto::getDepotId).collect(Collectors.toList());
                        List<TGmDepot> depotCodes = baseMapper.findDepotCodesById(depotIds);
                        List<String> codes = depotCodes.stream().map(TGmDepot::getDepotCode).collect(Collectors.toList());

//                        List<String> gameCategorys = catIdKey == Constants.EVNumber.one
//                                ? Lists.newArrayList(Constants.depotCatMap.get(catIdKey),
//                                Constants.depotCatMap.get(Constants.EVNumber.nine))
//                                : Lists.newArrayList(Constants.depotCatMap.get(catIdKey));

                        List<String> gameCategorys = Lists.newArrayList(Constants.depotCatMap.get(catIdKey));
                        List<WaterValidBetDto> validBetDtoList = getValidBet(sitePrefix, account.getLoginName(), gameCategorys, startTime, endTime, codes, "0");

                        if (validBetDtoList.size() > 0) {
                            for (WaterValidBetDto rs : validBetDtoList) {

                                Integer depotId = depotCodes.stream().filter(
                                        d -> d.getDepotCode().equalsIgnoreCase(rs.getDepotCode())).findAny().get().getId();

                                JWaterRebatesLevelListDto rebatesLevelListDto = rebatesLevelList.stream().filter(
                                        d -> d.getDepotId().equals(depotId)).findAny().get();

                                if (nonNull(rebatesLevelListDto.getDonateRatio())) {

                                    BigDecimal bonusAmount = adjustScale(rebatesLevelListDto.getDonateRatio().divide(
                                            new BigDecimal(ONE_HUNDRED)).multiply(rs.getValidBet()));

                                    if (bonusAmount.compareTo(BigDecimal.ZERO) == 1) {
                                        BigDecimal auditAmount = nonNull(jWaterRebatesLevelDto.getMultipleWater())
                                                ? adjustScale(new BigDecimal(jWaterRebatesLevelDto.getMultipleWater())
                                                .multiply(bonusAmount)) : BigDecimal.ZERO;

                                        waterIds.add(insertOprActWater(account, catIdKey, rs, activity, depotCodes, bonusAmount, auditAmount));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return waterIds;
    }

    public List<JWaterRebatesLevelDto> getWaterRebatesLevelList(MbrAccount account,JWaterRebatesNeDto dto){

        List<JWaterRebatesAgentDto> agentDtoList = dto.getAgentDtoList();
        List<JWaterRebatesLevelDto> defaultLevelList = dto.getLevelDtoList();
        if(Collections3.isNotEmpty(agentDtoList)){
            Optional<JWaterRebatesAgentDto> cAgentLevelDto = agentDtoList.stream().filter(agentDto ->
                    account.getCagencyId().equals(agentDto.getAgentId())).findFirst();
            if(cAgentLevelDto.isPresent()){
                return cAgentLevelDto.get().getLevelDtoList();
            }
            Optional<JWaterRebatesAgentDto> tAgentLevelDto = agentDtoList.stream().filter(agentDto ->
                    account.getTagencyId().equals(agentDto.getAgentId())).findFirst();
            if(tAgentLevelDto.isPresent()){
                return tAgentLevelDto.get().getLevelDtoList();
            }
        }
        return defaultLevelList;
    }

    public int insertOprActWater(MbrAccount account, Integer catId, WaterValidBetDto validBetDto,
                                 OprActActivity activity, List<TGmDepot> depotCodes,
                                 BigDecimal amount, BigDecimal auditAmount) {

        OprActWater actWater = new OprActWater();
        actWater.setAccountId(account.getId());
        actWater.setLoginName(account.getLoginName());
        actWater.setCatId(catId);
        actWater.setValidBet(validBetDto.getValidBet());
        actWater.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
        actWater.setActivityId(activity.getId());
        actWater.setRuleId(activity.getRuleId());
        actWater.setAmount(amount);
        actWater.setAuditAmount(auditAmount);
        actWater.setDepotId(depotCodes.stream().filter(
                        d -> d.getDepotCode().equalsIgnoreCase(validBetDto.getDepotCode()))
                .findAny().get().getId());
        actWaterMapper.insert(actWater);
        return actWater.getId();
    }

    public List<WaterValidBetDto> getValidBet(List<String> sitePrefix, String username, List<String> gameCategorys, String startTime, String endTime, List<String> depots, String water) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termsQuery("userName", username));
        query.must(QueryBuilders.termsQuery("sitePrefix", sitePrefix));
        if (Collections3.isNotEmpty(gameCategorys)) {
            query.must(QueryBuilders.termsQuery("gameCategory", gameCategorys));
        }
        if (StringUtils.isNotEmpty(water)) {
            query.must(QueryBuilders.termsQuery("water", water));
        }
        if (nonNull(startTime)) {
            query.must(QueryBuilders.rangeQuery("payoutTime").gte(startTime).lt(endTime));
        }
        BoolQueryBuilder pfbuilder = QueryBuilders.boolQuery();
        if (Collections3.isNotEmpty(depots)) {
            depots.forEach(pf -> {
                pfbuilder.should(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("platform", pf)));
            });
        }
        query.must(pfbuilder);

        TermsAggregationBuilder agg = AggregationBuilders
                .terms("platform").field("platform").
                subAggregation(AggregationBuilders.sum("validBet").field("validBet"))
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
            Map validBet = (Map) ((Map) map.get("aggregations")).get("platform");
            List<WaterValidBetDto> validBetDtos = Lists.newArrayList();
            if (nonNull(validBet)) {
                for (Object obj : (JSONArray) validBet.get("buckets")) {
                    Map objMap = (Map) obj;
                    WaterValidBetDto waterValidBetDto = new WaterValidBetDto();
                    waterValidBetDto.setDepotCode(objMap.get("key").toString());
                    Map vmap = nonNull(objMap.get("validBet")) ? (Map) objMap.get("validBet") : null;
                    waterValidBetDto.setValidBet(CommonUtil.adjustScale(nonNull(vmap.get("value"))
                            ? new BigDecimal(vmap.get("value").toString()) : BigDecimal.ZERO));

                    validBetDtos.add(waterValidBetDto);
                }
                return validBetDtos;
            }
        } catch (Exception e) {
            log.error("返水获取会员所有投注额失败", e);
        }
        return null;
    }
}
