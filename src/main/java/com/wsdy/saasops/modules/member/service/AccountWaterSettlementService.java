package com.wsdy.saasops.modules.member.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.wsdy.saasops.ElasticSearchConnection;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.base.mapper.BaseMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dto.SettlementValidBetDto;
import com.wsdy.saasops.modules.member.dto.WaterDepotDto;
import com.wsdy.saasops.modules.member.dto.WaterValidBetDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAuditAccount;
import com.wsdy.saasops.modules.member.mapper.AuditMapper;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dao.OprActWaterMapper;
import com.wsdy.saasops.modules.operate.dto.*;
import com.wsdy.saasops.modules.operate.entity.*;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.operate.mapper.OperateMapper;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.constants.Constants.ONE_HUNDRED;
import static com.wsdy.saasops.common.utils.CommonUtil.adjustScale;
import static com.wsdy.saasops.common.utils.DateUtil.*;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.preferentialCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.waterRebatesCode;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
@Transactional
public class AccountWaterSettlementService {

    private static final String HHMMSS = " 00:00:00";
    @Autowired
    private OperateActivityMapper activityMapper;
    @Autowired
    private ElasticSearchConnection_Read connection;
    @Autowired
    private ElasticSearchConnection searchConnection;
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private AccountWaterCastService waterCastService;
    @Autowired
    private AuditMapper auditMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private OperateMapper operateMapper;
    @Autowired
    private OprActActivityService oprActBaseService;
    @Autowired
    private OprActWaterMapper actWaterMapper;


    public List<WaterDepotDto> findAccountWaterRate(Integer accountId, String siteCode) {
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        List<OprActRule> oprActRuleList = activityMapper.findRuleRate();
        List<WaterDepotDto> waterDepotDtoList = operateMapper.findDepotAndCatNameList(siteCode);
        if (Collections3.isNotEmpty(oprActRuleList)) {
            JWaterRebatesDto dto = new Gson().fromJson(oprActRuleList.get(0).getRule(), JWaterRebatesDto.class);
//            List<JWaterRebatesLevelDto> levelDtoList = dto.getRebatesNeDto().getLevelDtoList();
            List<JWaterRebatesLevelDto> levelDtoList = waterCastService.getWaterRebatesLevelList( account, dto.getRebatesNeDto());
            Optional<JWaterRebatesLevelDto> rebatesLevelDto = levelDtoList.stream().filter(e ->
                    e.getAccountLevel().equals(account.getActLevelId())).findAny();
            if (rebatesLevelDto.isPresent()) {
                List<JWaterRebatesLevelListDto> rebatesLevelListDtos = rebatesLevelDto.get().getRebatesLevelListDtos();
                waterDepotDtoList.stream().forEach(wt -> {
                    rebatesLevelListDtos.stream().forEach(rs -> {
                        if (rs.getCatId().equals(wt.getCatId()) && rs.getDepotId().equals(wt.getDepotId())) {
                            wt.setDonateRatio(rs.getDonateRatio());
                        }
                    });
                });
            }
            Map<String, String> timeMap = getQueryTime(dto.getPeriod());
            List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(siteCode);

            // ??????????????????????????????????????????????????????????????????????????????
            List<MbrAuditAccount> list = auditMapper.findAuditAccountPreferentialEx(account.getId(), preferentialCode);
            if (Objects.nonNull(list) && list.size()> 0) {  // ?????????????????????
                if(Integer.valueOf(Constants.EVNumber.zero).equals(list.get(0).getStatus())){
                    log.info("????????????????????????????????????????????????????????????!???" + siteCode + accountId + "???");
                    return waterDepotDtoList;
                }
                // ????????????????????????????????????????????????
                String passTime = list.get(0).getPassTime();    // ??????????????????
                if(!StringUtil.isEmpty(passTime)){
                    passTime = passTime.substring(0,19);
                    String tmpStartTime = DateUtil.formatEsDateToTime(timeMap.get("startTime"));
                    log.info("?????????????????????????????????????????????" + passTime + ",????????????????????????" + tmpStartTime  + ",?????????"+ account.getLoginName() + "???");
                    int result = DateUtil.timeCompare(passTime,tmpStartTime,DateUtil.FORMAT_18_DATE_TIME);
                    if(result > Constants.EVNumber.zero){   // ??????????????????????????????????????????,????????????????????????
                        timeMap.put("startTime",DateUtil.formatEsDate(passTime));
                        log.info("????????????????????????"+ account.getLoginName() + "????????????????????????" + timeMap.get("startTime"));
                    }
                }
            }

            return depotWaterValidBetBatchFuture(waterDepotDtoList, account.getLoginName(), sitePrefix, timeMap);
        }
        return new ArrayList<>();
    }

    public PageUtils waterDetailList(OprActBonus oprActBonus, Integer pageNo, Integer pageSize) {
        return oprActBaseService.waterAuditList(oprActBonus, pageNo, pageSize);
    }

    public PageUtils depotWaterDetailList(OprActBonus oprActBonus, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<ActivityWaterCatDto> waterCatDtos = activityMapper.findWaterDetailList(oprActBonus.getAccountId(),
                oprActBonus.getStartTime(), oprActBonus.getEndTime(), oprActBonus.getCatId());
        return BeanUtil.toPagedResult(waterCatDtos);
    }

    private Map<String, String> getQueryTime(int period) {
        String startTime = formatEsDate(getPastDate(Constants.EVNumber.zero, FORMAT_10_DATE) + HHMMSS);
        String endTime = formatEsDate(getPastDate(-1, FORMAT_10_DATE) + HHMMSS);
        if (period == 1) {
            Map<String, String> stringMap = getWeekDateEx();
            startTime = formatEsDate(stringMap.get("mondayDate") + HHMMSS);
            endTime = formatEsDate(stringMap.get("sundayDate") + HHMMSS);
        }
        Map<String, String> map = new HashMap<>(2);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        return map;
    }
    private Map<String, String> getQueryTimeYestDay(int period) {
        String startTime = formatEsDate(getPastDate(Constants.EVNumber.one, FORMAT_10_DATE) + HHMMSS);
        String endTime = formatEsDate(getPastDate(Constants.EVNumber.zero, FORMAT_10_DATE) + HHMMSS);
        if (period == 1) {
            Map<String, String> stringMap = getWeekDateEx();
            startTime = formatEsDate(stringMap.get("mondayDate") + HHMMSS);
            endTime = formatEsDate(stringMap.get("sundayDate") + HHMMSS);
        }
        Map<String, String> map = new HashMap<>(2);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        return map;
    }

    public List<WaterDepotDto> depotWaterValidBetBatchFuture(List<WaterDepotDto> waterDepotDtoList, String loginName, List<String> sitePrefix, Map<String, String> timeMap) {

        List<CompletableFuture<WaterDepotDto>> depotFindingFutureList =
                waterDepotDtoList.stream().map(dt ->
                        depotWaterValidBetFuture(dt, loginName, sitePrefix, timeMap)).collect(Collectors.toList());

        CompletableFuture<Void> allFutures =
                CompletableFuture
                        .allOf(depotFindingFutureList.toArray(
                                new CompletableFuture[depotFindingFutureList.size()]));

        CompletableFuture<List<WaterDepotDto>> depotFailResults = allFutures.thenApply(v -> {
            return depotFindingFutureList.stream().map(
                            depotFailDtoCompletableFuture -> depotFailDtoCompletableFuture.join())
                    .collect(Collectors.toList());
        });
        try {
            return depotFailResults.get();
        } catch (Exception e) {
            log.error("depotWaterValidBetBatchFuture", e);
            return null;
        }
    }

    CompletableFuture<WaterDepotDto> depotWaterValidBetFuture(WaterDepotDto waterDepotDto, String loginName, List<String> sitePrefix, Map<String, String> timeMap) {
        return CompletableFuture.supplyAsync(() -> {
            waterDepotDto.setDonateRatio(nonNull(waterDepotDto.getDonateRatio()) ? waterDepotDto.getDonateRatio() : BigDecimal.ZERO);

//            List<String> gameCategorys = waterDepotDto.getCatId() == Constants.EVNumber.one
//                    ? Lists.newArrayList(Constants.depotCatMap.get(waterDepotDto.getCatId()),
//                    Constants.depotCatMap.get(Constants.EVNumber.nine))
//                    : Lists.newArrayList(Constants.depotCatMap.get(waterDepotDto.getCatId()));

            List<String> gameCategorys =Lists.newArrayList(Constants.depotCatMap.get(waterDepotDto.getCatId()));

            List<WaterValidBetDto> validBetDtos = waterCastService.getValidBet(sitePrefix, loginName, gameCategorys,
                    timeMap.get("startTime"), timeMap.get("endTime"), Lists.newArrayList(waterDepotDto.getDepotCode()), "1");

            List<WaterValidBetDto> currentValidBetDtos = waterCastService.getValidBet(sitePrefix, loginName, gameCategorys,
                    timeMap.get("startTime"), timeMap.get("endTime"), Lists.newArrayList(waterDepotDto.getDepotCode()), "0");

            if (Collections3.isNotEmpty(validBetDtos)) {
                waterDepotDto.setValidBet(validBetDtos.get(0).getValidBet());
            }
            if (Collections3.isNotEmpty(currentValidBetDtos)) {
                waterDepotDto.setCurrentValidBet(currentValidBetDtos.get(0).getValidBet());

                BigDecimal bonusAmount = adjustScale(waterDepotDto.getDonateRatio().divide(
                        new BigDecimal(ONE_HUNDRED)).multiply(waterDepotDto.getCurrentValidBet()));
                waterDepotDto.setAmount(bonusAmount);
            }
            return waterDepotDto;
        });
    }

    public void settlementWater(Integer accountId, String siteCode) {
        if(oprActBaseService.isBlackList(accountId,waterRebatesCode)||oprActBaseService.isBlackList(accountId, TOpActtmpl.allActivityCode)){
            throw new R200Exception("????????????????????????");
        }

        List<OprActActivity> activities = waterCastService.getOprActActivitys(Constants.EVNumber.one);
        if (activities.size() > 1) {
            throw new R200Exception("??????????????????");
        }
        if (activities.size() == 0) {
            throw new R200Exception("?????????????????????");
        }

        int count = auditMapper.findAuditAccountPreferential(accountId, preferentialCode);
        if (count > 0) {
            log.info("??????????????????????????????????????????????????????" + siteCode + accountId + "???");
            throw new R200Exception("?????????????????????,??????????????????");
        }

        OprActBonus actBonus = new OprActBonus();
        actBonus.setFinancialCode(OrderConstants.ACTIVITY_WATERBONUS);
        actBonus.setStatus(Constants.EVNumber.two);
        actBonus.setAccountId(accountId);
        int bonusCount = actBonusMapper.selectCount(actBonus);
        if (bonusCount > 0) {
            throw new R200Exception("?????????????????????????????????????????????");
        }

        List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(siteCode);
        for (OprActActivity as : activities) {
            JWaterRebatesDto dto = new Gson().fromJson(as.getRule(), JWaterRebatesDto.class);
            Map<String, String> timeMap = getQueryTime(dto.getPeriod());
            MbrAccount account = accountMapper.selectByPrimaryKey(accountId);

            // ??????????????????????????????????????????????????????????????????????????????
            List<MbrAuditAccount> list = auditMapper.findAuditAccountPreferentialEx(account.getId(), preferentialCode);
            if (Objects.nonNull(list) && list.size()> 0) {  // ?????????????????????
                if(Integer.valueOf(Constants.EVNumber.zero).equals(list.get(0).getStatus())){
                    log.info("????????????????????????????????????????????????????????????!???" + siteCode + accountId + "???");
                    throw new RRException("?????????????????????,??????????????????!");
                }
                // ????????????????????????????????????????????????
                String passTime = list.get(0).getPassTime();    // ??????????????????
                if(!StringUtil.isEmpty(passTime)) {
                    passTime = passTime.substring(0,19);
                    if (!StringUtil.isEmpty(passTime)) {
                        String tmpStartTime = DateUtil.formatEsDateToTime(timeMap.get("startTime"));
                        log.info("?????????????????????????????????????????????" + passTime + ",????????????????????????" + tmpStartTime + ",?????????" + account.getLoginName() + "???");
                        int result = DateUtil.timeCompare(passTime, tmpStartTime, DateUtil.FORMAT_18_DATE_TIME);
                        if (result > Constants.EVNumber.zero) {   // ??????????????????????????????????????????,????????????????????????
                            timeMap.put("startTime", DateUtil.formatEsDate(passTime));
                            log.info("????????????????????????" + account.getLoginName() + "????????????????????????" + timeMap.get("startTime"));
                        }
                    }
                }
            }

            castSettlementWater(account, siteCode, timeMap.get("startTime"), timeMap.get("endTime"), as, dto, sitePrefix);
        }
    }

    public void castSettlementWater(MbrAccount account, String siteCode, String startTime,
                                    String endTime, OprActActivity activity, JWaterRebatesDto dto, List<String> sitePrefix) {
        List<String> ids = Lists.newArrayList();
        try {
            Map mapValidBet = settlementValidBet(account, startTime, endTime, activity, dto, sitePrefix);
            List<SettlementValidBetDto> validBetDtoList = (List<SettlementValidBetDto>) mapValidBet.get("settlementValidBet");
            List<Integer> waterIds = (List<Integer>) mapValidBet.get("waterIds");
            if (validBetDtoList.size() > 0) {
                activity.setWaterStart(startTime);
                activity.setWaterEnd(endTime);
                OprActBonus bonus = waterCastService.insertOprActBonus(account, activity, siteCode, Constants.SYSTEM_WATER_USER, waterIds, RedisConstants.SETTLEMENT_WATER_BATCHINFO);
                if (activity.getIsAudit() == Constants.EVNumber.zero && nonNull(bonus)) {
                    bonus.setStatus(Constants.EVNumber.one);
                    bonus.setAuditUser(Constants.SYSTEM_WATER_USER);
                    bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    waterCastService.grantOprActBonus(bonus, siteCode);
                }
                ids = validBetDtoList.stream().map(SettlementValidBetDto::getId).collect(Collectors.toList());
                ids.stream().forEach(id -> esUpdateByQuery(getUpdateWaterJson(bonus.getId()), id));
            }
        } catch (Exception e){
            if (ids.size() > 0) {
                ids.stream().forEach(id -> esUpdateByQuery(getUpdateBounsJson(), id));
            }
            log.info("??????????????????" + account.getLoginName(), e);
            throw new R200Exception("???????????????");
        }
    }


    private Map settlementValidBet(MbrAccount account, String startTime, String endTime,
                                   OprActActivity activity, JWaterRebatesDto dto, List<String> sitePrefix) {
        List<SettlementValidBetDto> settlementValidBetDtoList = Lists.newArrayList();
        BigDecimal bonusAmountTotal = BigDecimal.ZERO;
        List<OprActWater> waters = Lists.newArrayList();
        List<JWaterRebatesLevelDto> levelDtoList = waterCastService.getWaterRebatesLevelList(account,dto.getRebatesNeDto());
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
                        List<SettlementValidBetDto> settlementValidBetDtos =
                                getValidBetGB(sitePrefix, account.getLoginName(),
                                        gameCategorys, startTime, endTime, codes, "0", null);

                        if (settlementValidBetDtos.size() > 0) {

                            Map<String, List<SettlementValidBetDto>> validBetDtoGroupingBy =
                                    settlementValidBetDtos.stream().collect(
                                            Collectors.groupingBy(
                                                    SettlementValidBetDto::getPlatform));

                            for (String depotCodeKey : validBetDtoGroupingBy.keySet()) {

                                Integer depotId = depotCodes.stream().filter(
                                        d -> d.getDepotCode().equalsIgnoreCase(depotCodeKey)).findAny().get().getId();

                                JWaterRebatesLevelListDto rebatesLevelListDto = rebatesLevelList.stream().filter(
                                        d -> d.getDepotId().equals(depotId)).findAny().get();

                                if (nonNull(rebatesLevelListDto.getDonateRatio())) {

                                    List<SettlementValidBetDto> validBetDtos = validBetDtoGroupingBy.get(depotCodeKey);
                                    Optional<BigDecimal> validBet = validBetDtos.stream()
                                            .filter(p -> nonNull(p.getValidBet()))
                                            .map(SettlementValidBetDto::getValidBet).reduce(BigDecimal::add);
                                    WaterValidBetDto validBetDto = new WaterValidBetDto();
                                    validBetDto.setDepotCode(depotCodeKey);
                                    validBetDto.setValidBet(validBet.isPresent() ? validBet.get() : BigDecimal.ZERO);

                                    BigDecimal bonusAmount = adjustScale(rebatesLevelListDto.getDonateRatio().divide(
                                            new BigDecimal(ONE_HUNDRED)).multiply(validBetDto.getValidBet()));

                                    bonusAmountTotal = bonusAmountTotal.add(bonusAmount);
                                    if (bonusAmount.compareTo(BigDecimal.ZERO) == 1) {

                                        BigDecimal auditAmount = nonNull(jWaterRebatesLevelDto.getMultipleWater())
                                                ? adjustScale(new BigDecimal(jWaterRebatesLevelDto.getMultipleWater())
                                                .multiply(bonusAmount)) : BigDecimal.ZERO;

                                        waters.add(waterCastService.generateOprActWater(account, catIdKey, validBetDto, activity, depotCodes, bonusAmount, auditAmount));
                                        settlementValidBetDtoList.addAll(validBetDtos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(bonusAmountTotal.compareTo(activity.getMinAmount())<Constants.EVNumber.zero){
            throw new R200Exception("????????????????????????????????????"+activity.getMinAmount()+"?????????????????????");
        }
        Map mapValidBet = new HashMap(2);
        mapValidBet.put("settlementValidBet", settlementValidBetDtoList);
        mapValidBet.put("waterIds", getWaterIds(waters));
        return mapValidBet;
    }

    private List<Integer> getWaterIds(List<OprActWater> waters){
        List<Integer> waterIds = Lists.newArrayList();
        for(OprActWater water : waters){
            actWaterMapper.insert(water);
            waterIds.add(water.getId());
        }
        return waterIds;
    }


    public List<SettlementValidBetDto> getValidBetGB(List<String> sitePrefix, String username, List<String> gameCategorys,
                                                     String startTime, String endTime, List<String> depots, String water, String bonusId) {

        BoolQueryBuilder query = QueryBuilders.boolQuery();
        // 1.??????????????????query
        query.must(QueryBuilders.termsQuery("userName", username));
        query.must(QueryBuilders.termsQuery("sitePrefix", sitePrefix));
        if (Collections3.isNotEmpty(gameCategorys)) {
            query.must(QueryBuilders.termsQuery("gameCategory", gameCategorys));
        }
        if (StringUtils.isNotEmpty(water)) {
            query.must(QueryBuilders.termsQuery("water", water));
        }
        if (StringUtils.isNotEmpty(bonusId)) {
            query.must(QueryBuilders.termsQuery("bonusId", bonusId));
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
        query.must(pfbuilder);  // ??????????????????

        // 2. ????????????builder
        SearchRequestBuilder searchRequestBuilder =
                connection.client.prepareSearch("report")
                        .setQuery(query).setSize(ElasticSearchConstant.SEARCH_COUNT)
                        .setFetchSource(new String[]{"id", "validBet", "platform"}, null);

        try {
            List<SettlementValidBetDto> validBetDtoList = Lists.newArrayList();    // ????????????list

            // 3. ?????????????????????????????????????????????????????????_scroll_id???????????????15??????
            String str = searchRequestBuilder.toString();
            Response response = connection.restClient_Read.performRequest(
                    "GET", "/" + ElasticSearchConstant.RPT_WATER
                            + "/" + ElasticSearchConstant.RPT_BET_WATERR_TYPE + "/_search?scroll=15m",
                    Collections.singletonMap("_source", "true"),
                    new NStringEntity(str, ContentType.APPLICATION_JSON));

            // ??????????????????
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            String scrollId = String.valueOf(map.get("_scroll_id"));                // ??????_scroll_id
            JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));   // ????????????
            for (Object obj : hits) {
                Map objmap = (Map) obj;
                SettlementValidBetDto validBetDto = JSON.parseObject(objmap.get("_source").toString(), SettlementValidBetDto.class);
                validBetDtoList.add(validBetDto);
            }
            // ??????????????????
            queryScrollValidBet(scrollId, validBetDtoList);
            return validBetDtoList;
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
        }
        return null;
    }

    private void queryScrollValidBet(String scrollId, List<SettlementValidBetDto> validBetDtoList) throws IOException {
        if (StringUtils.isNotEmpty(scrollId)) {
            while (true) {
                String scrollQuerySql = String.format("{\"scroll\":\"%s\",\"scroll_id\":\"%s\"}", "15m", scrollId);
                Response sroresponse = connection.restClient_Read.performRequest(
                        "GET", "/_search/scroll", Collections.emptyMap(),
                        new NStringEntity(scrollQuerySql, ContentType.APPLICATION_JSON));
                Map map = (Map) JSON.parse(EntityUtils.toString(sroresponse.getEntity()));
                JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));
                if (hits.size() == 0) {
                    String scrollDeleteSql = String.format("{\"scroll_id\":\"%s\"}", scrollId);
                    searchConnection.restClient.performRequest(
                            "DELETE", "/_search/scroll", Collections.emptyMap(),
                            new NStringEntity(scrollDeleteSql, ContentType.APPLICATION_JSON));
                    break;
                }
                for (Object obj : hits) {
                    Map objmap = (Map) obj;
                    SettlementValidBetDto validBetDto = JSON.parseObject(objmap.get("_source").toString(), SettlementValidBetDto.class);
                    validBetDtoList.add(validBetDto);
                }
            }
        }
    }


    public String getUpdateWaterJson(Integer bonusId) {
        return String.format("{\"doc\" : {\"bonusId\" : \"%s\",\"water\":\"%s\"}}", bonusId, "1");
    }

    public String getUpdateBounsJson() {
        return String.format("{\"doc\" : {\"bonusId\" : \"%s\",\"water\":\"%s\"}}", "0", "0");
    }

    public void esUpdateWaterBybonusId(String siteCode, String loginName, Integer bonusId) {
        List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(siteCode);
        List<SettlementValidBetDto> settlementValidBetDtos =
                getValidBetGB(sitePrefix, loginName, null, null, null, null, "1", bonusId.toString());
        settlementValidBetDtos.stream().forEach(st -> esUpdateByQuery(getUpdateBounsJson(), st.getId()));
    }

    public void esUpdateByQuery(String str, String id) {
        HttpEntity entity = new NStringEntity(str, ContentType.APPLICATION_JSON);
        try {
            searchConnection.restClient.performRequest("POST",
                    "/" + ElasticSearchConstant.RPT_WATER + "/"
                            + ElasticSearchConstant.RPT_BET_WATERR_TYPE + "/" + id
                            + "/_update", Collections.singletonMap("pretty", "true"), entity);
        } catch (Exception e) {
            log.error("??????????????????", e);
            throw new RRException("??????????????????");
        }
    }

    public BigDecimal findRuleRateLimit(){
        List<OprActRule> oprActRuleList = activityMapper.findRuleRate();
        BigDecimal minLimitAmount = BigDecimal.ZERO;
        if(CollectionUtils.isNotEmpty(oprActRuleList)){
            OprActRule rule = oprActRuleList.get(0);
            if(nonNull(rule)){
                if(Constants.EVNumber.one == rule.getIsLimit()){
                    minLimitAmount = isNull(rule.getMinAmount()) ? BigDecimal.ZERO:rule.getMinAmount();
                }
            }
        }
        return minLimitAmount;
    }

    public List<WaterDepotDto> findAccountWaterRateYestday(Integer accountId, String siteCode) {
//        SimpleDateFormat format = new SimpleDateFormat("HH");
//        String time = format.format(new Date());
//        if (Integer.parseInt(time)>=14){  //????????????????????????(14) ????????????0, ???????????????????????????????????????0
//            return new ArrayList<>();
//        }

        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        List<OprActRule> oprActRuleList = activityMapper.findRuleRate();
        List<WaterDepotDto> waterDepotDtoList = operateMapper.findDepotAndCatNameList(siteCode);
        if (Collections3.isNotEmpty(oprActRuleList)) {
            JWaterRebatesDto dto = new Gson().fromJson(oprActRuleList.get(0).getRule(), JWaterRebatesDto.class);
//            List<JWaterRebatesLevelDto> levelDtoList = dto.getRebatesNeDto().getLevelDtoList();
            List<JWaterRebatesLevelDto> levelDtoList = waterCastService.getWaterRebatesLevelList( account, dto.getRebatesNeDto());
            Optional<JWaterRebatesLevelDto> rebatesLevelDto = levelDtoList.stream().filter(e ->
                    e.getAccountLevel().equals(account.getActLevelId())).findAny();
            if (rebatesLevelDto.isPresent()) {
                List<JWaterRebatesLevelListDto> rebatesLevelListDtos = rebatesLevelDto.get().getRebatesLevelListDtos();
                waterDepotDtoList.stream().forEach(wt -> {
                    rebatesLevelListDtos.stream().forEach(rs -> {
                        if (rs.getCatId().equals(wt.getCatId()) && rs.getDepotId().equals(wt.getDepotId())) {
                            wt.setDonateRatio(rs.getDonateRatio());
                        }
                    });
                });
            }
            Map<String, String> timeMap = getQueryTimeYestDay(dto.getPeriod());
            List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(siteCode);

            // ??????????????????????????????????????????????????????????????????????????????
            List<MbrAuditAccount> list = auditMapper.findAuditAccountPreferentialEx(account.getId(), preferentialCode);
            if (Objects.nonNull(list) && list.size()> 0) {  // ?????????????????????
                if(Integer.valueOf(Constants.EVNumber.zero).equals(list.get(0).getStatus())){
                    log.info("????????????????????????????????????????????????????????????!???" + siteCode + accountId + "???");
                    return waterDepotDtoList;
                }
                // ????????????????????????????????????????????????
                String passTime = list.get(0).getPassTime();    // ??????????????????
                if(!StringUtil.isEmpty(passTime)){
                    passTime = passTime.substring(0,19);
                    String tmpStartTime = DateUtil.formatEsDateToTime(timeMap.get("startTime"));
                    log.info("?????????????????????????????????????????????" + passTime + ",????????????????????????" + tmpStartTime  + ",?????????"+ account.getLoginName() + "???");
                    int result = DateUtil.timeCompare(passTime,tmpStartTime,DateUtil.FORMAT_18_DATE_TIME);
                    if(result > Constants.EVNumber.zero){   // ??????????????????????????????????????????,????????????????????????
                        timeMap.put("startTime",DateUtil.formatEsDate(passTime));
                        log.info("????????????????????????"+ account.getLoginName() + "????????????????????????" + timeMap.get("startTime"));
                    }
                }
            }

            return depotWaterValidBetBatchFuture(waterDepotDtoList, account.getLoginName(), sitePrefix, timeMap);
        }
        return new ArrayList<>();
    }
}
