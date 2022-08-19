package com.wsdy.saasops.modules.analysis.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.ImmutableMap;
import com.wsdy.saasops.ElasticSearchConnection;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.user.dto.BetDataDto;
import com.wsdy.saasops.api.modules.user.dto.DepotHitsDto;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.GameTypeEnum;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ColumnAuthConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.BigDecimalMath;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.analysis.entity.BounsReportQueryModel;
import com.wsdy.saasops.modules.analysis.entity.FundReportModel;
import com.wsdy.saasops.modules.analysis.entity.FundStatisticsModel;
import com.wsdy.saasops.modules.analysis.entity.GameReportModel;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.analysis.entity.OpenResultModel;
import com.wsdy.saasops.modules.analysis.entity.RptBetDayModel;
import com.wsdy.saasops.modules.analysis.entity.RptBetModel;
import com.wsdy.saasops.modules.analysis.entity.RptBetModelNew;
import com.wsdy.saasops.modules.analysis.entity.RptBetTotalModel;
import com.wsdy.saasops.modules.analysis.entity.RptMemberModel;
import com.wsdy.saasops.modules.analysis.entity.RptWinLostModel;
import com.wsdy.saasops.modules.analysis.entity.SelectModel;
import com.wsdy.saasops.modules.analysis.entity.TransactionModel;
import com.wsdy.saasops.modules.analysis.entity.WinLostEsQueryModel;
import com.wsdy.saasops.modules.analysis.entity.WinLostReport;
import com.wsdy.saasops.modules.analysis.entity.WinLostReportModel;
import com.wsdy.saasops.modules.analysis.mapper.AnalysisMapper;
import com.wsdy.saasops.modules.analysis.mapper.HomeMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.operate.dao.SetGmGameMapper;
import com.wsdy.saasops.modules.operate.dto.GameDepotNameDto;
import com.wsdy.saasops.modules.operate.entity.TGmGame;
import com.wsdy.saasops.modules.sys.dto.ColumnAuthTreeDto;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.service.ColumnAuthProviderService;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AnalysisService {

    @Autowired
    private AnalysisMapper analysisMapper;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private ColumnAuthProviderService columnAuthProviderService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private ElasticSearchConnection_Read connection;
    @Autowired
    private ElasticSearchConnection searchConnection;
    @Autowired
    private RedisService redisService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private HomeMapper homeMapper;
    @Autowired
    private SetGmGameMapper setGmGameMapper;

    private static final String defaultsdf = "yyyy-MM-dd HH:mm:ss"; // SimpleDateFormat线程安全处理；
    private static final String sdf = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /***
     *		注单记录，提供前端用户查询
     */
    public PageUtils getRptBetListPage(Integer pageNo, Integer pageSize, String siteCode, String loginName,
                                       String platform, Integer gameCatId, String betStrTime, String betEndTime, String status) {
        GameReportQueryModel model = new GameReportQueryModel();
        model.setSiteCode(siteCode);
        model.setLoginName(loginName);
        model.setPlatform(platform);
        model.setBetStrTime(betStrTime);
        model.setBetEndTime(betEndTime);
        model.setGametype(String.valueOf((gameCatId == null) ? "" : gameCatId));
        model.setStatus(String.valueOf((status == null) ? "" : status));
        // 查询注单
        PageUtils pageUtils = getRptBetListPage(pageNo, pageSize, model);

        List<RptBetModelNew> newList = Lists.newArrayList();
        DecimalFormat df1 = new DecimalFormat("0.00");
        try {
            for (RptBetModel model1 : (List<RptBetModel>) pageUtils.getList()) {
                Map map = JSONObject.parseObject(JSONObject.toJSONString(model1), Map.class);
                RptBetModelNew modelNew = JSON.parseObject(JSON.toJSONString(map), RptBetModelNew.class);
                if (nonNull(model1.getBet())) {
                    modelNew.setBet(df1.format(model1.getBet()));
                }
                if (nonNull(model1.getValidBet())) {
                	modelNew.setValidBet(df1.format(model1.getValidBet()));
                }
                if (nonNull(model1.getPayout())) {
                    modelNew.setPayout(df1.format(model1.getPayout()));
                }
                newList.add(modelNew);
            }
            pageUtils.setList(newList);
        } catch (Exception e) {
            log.error("getRptBetListPage error", e);
        }
        return pageUtils;
    }
    
    /***
     *提供前端用户查询
     */
    public Map getRptBetListReport(String siteCode, String loginName,
                                   String platform, Integer gameCatId, String betStrTime, String betEndTime, String status) {
        GameReportQueryModel model = new GameReportQueryModel();
        model.setSiteCode(siteCode);
        model.setLoginName(loginName);
        model.setPlatform(platform);
        model.setBetStrTime(betStrTime);
        model.setBetEndTime(betEndTime);
        model.setGametype(String.valueOf((gameCatId == null) ? "" : gameCatId));
        model.setStatus(String.valueOf((status == null) ? "" : status));
        Map map = getRptBetListReport(model);

        DecimalFormat df1 = new DecimalFormat("0.00");
        map.put("bet", df1.format(map.get("bet")));
        map.put("payout", df1.format(map.get("payout")));
        map.put("validBet", df1.format(map.get("validBet")));
        return map;
    }

    public PageUtils getRptBetListReport(BetDataDto betDataDto, Integer pageNo, Integer pageSize) {
        GameReportQueryModel model = getGameReportQueryModel(betDataDto);
        return getRptBetListReport(model, pageNo, pageSize);
    }

    public GameReportQueryModel getGameReportQueryModel(BetDataDto betDataDto) {
        GameReportQueryModel model = new GameReportQueryModel();
        model.setBetid(betDataDto.getBetNum());
        model.setResult(betDataDto.getResult());
        model.setStatus(betDataDto.getStatus());
        model.setTableNo(betDataDto.getTableNo());
        model.setSerialId(betDataDto.getSerialId());
        model.setSiteCode(betDataDto.getSiteCode());
        model.setGamename(betDataDto.getGameName());
        model.setPlatform(betDataDto.getDepotName());
        model.setGameCatId(betDataDto.getGameCatId());
        model.setLoginName(betDataDto.getLoginName());
        model.setGametype(betDataDto.getGameCatName());
        model.setBetStrTime(betDataDto.getBetStrTime());
        model.setBetEndTime(betDataDto.getBetEndTime());
        return model;
    }

    /**
     * 查询各个站点的投注信息(根据站点、时间分组)
     *
     * @param model
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils getRptBetListReport(GameReportQueryModel model, Integer pageNo, Integer pageSize) {
        Map rsmap = new HashMap();
        try {
            BoolQueryBuilder builder = setEsQuery(model);
            SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
            searchRequestBuilder.addAggregation(AggregationBuilders.terms("website")
                    .field("website").subAggregation(AggregationBuilders.dateHistogram("betTime")
                            .field("betTime").format("yyyy-MM-dd HH:mm:ss").dateHistogramInterval(DateHistogramInterval.DAY)
                            .subAggregation(AggregationBuilders.cardinality("userName").field("userName"))
                            .subAggregation(AggregationBuilders.sum("bet").field("bet"))
                            .subAggregation(AggregationBuilders.sum("payout").field("payout"))
                            .subAggregation(AggregationBuilders.sum("validBet").field("validBet"))
                            .subAggregation(AggregationBuilders.sum("jackpotPayout").field("jackpotPayout"))));

            searchRequestBuilder.setQuery(builder).setFrom((pageNo - 1) * pageSize).setSize(pageSize);
            long star = System.currentTimeMillis();
            Response response = connection.restClient_Read.performRequest("POST",
                    "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
            long end = System.currentTimeMillis();
            log.info("站点运营报表查询时间" + (end - star));
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            List mapBuckets = (List) ((Map) ((Map) map.get("aggregations")).get("website")).get("buckets");
            List<DepotHitsDto> depotHitsDtos = new ArrayList<>();
            long star1 = System.currentTimeMillis();
            for (int i = 0; i < mapBuckets.size(); i++) {
                rsmap.put("website", ((Map) mapBuckets.get(i)).get("key"));
                rsmap.put("betTime", ((Map) mapBuckets.get(i)).get("betTime"));
                rsmap.put("docSiteCount", ((Map) mapBuckets.get(i)).get("doc_count"));
                List bucketsList = (List) ((Map) rsmap.get("betTime")).get("buckets");
                for (int j = 0; j < bucketsList.size(); j++) {
                    rsmap.put("betTime", ((Map) bucketsList.get(j)).get("betTime"));
                    rsmap.put("bet", ((Map) bucketsList.get(j)).get("bet"));
                    rsmap.put("key", ((Map) bucketsList.get(j)).get("key"));
                    rsmap.put("payout", ((Map) bucketsList.get(j)).get("payout"));
                    rsmap.put("validBet", ((Map) bucketsList.get(j)).get("validBet"));
                    rsmap.put("jackpotPayout", ((Map) bucketsList.get(j)).get("jackpotPayout"));
                    DepotHitsDto depotHitsDto = new DepotHitsDto();
                    rsmap.put("userNameCount", ((Map) bucketsList.get(j)).get("userName"));
                    rsmap.put("key_as_string", ((Map) bucketsList.get(j)).get("key_as_string"));
                    rsmap.put("doc_count", ((Map) bucketsList.get(j)).get("doc_count"));
                    depotHitsDto.setWebsite((String) rsmap.get("website"));
                    depotHitsDto.setSitedocCount((Integer) rsmap.get("docSiteCount"));
                    depotHitsDto.setBet((BigDecimal) ((Map) rsmap.get("bet")).get("value"));
                    depotHitsDto.setPayout((BigDecimal) ((Map) rsmap.get("payout")).get("value"));
                    depotHitsDto.setValidBet((BigDecimal) ((Map) rsmap.get("validBet")).get("value"));
                    depotHitsDto.setUserNameCount(((Map) rsmap.get("userNameCount")).get("value").toString());
                    depotHitsDto.setDocCount((Integer) rsmap.get("doc_count"));
                    depotHitsDto.setJackpotPayout((BigDecimal) ((Map) rsmap.get("jackpotPayout")).get("value"));
                    depotHitsDto.setBetDate((String) rsmap.get("key_as_string"));
                    Assert.isNull(rsmap.get("website"), "站点code不能为空！");
                    depotHitsDtos.add(depotHitsDto);
                }
            }
            long end1 = System.currentTimeMillis();
            log.info("站点运营报表解析数据时间" + (end1 - star1));
            return getPageUtils(pageNo, pageSize, map, depotHitsDtos);
        } catch (Exception e) {
            log.error("getRptBetListReport", e);
            throw new RRException("查询异常!");
        }
    }

    public PageUtils getPageUtils(Integer pageNo, Integer pageSize, Map map, List<DepotHitsDto> depotHitsDtos) {
        //TODO 总记录数
        Long total = Long.valueOf(depotHitsDtos.size());
        if (pageNo * pageSize > depotHitsDtos.size()) {
            PageUtils page = BeanUtil.toPagedResult(depotHitsDtos.subList((pageNo - 1) * pageSize, depotHitsDtos.size()));
            page.setTotalCount(total);
            page.setPageSize(pageSize);
            //TODO 设置总页数
            page.setTotalPage(BigDecimalMath.ceil(total.intValue(), pageSize));
            //TODO 设置当前页数
            page.setCurrPage(pageNo);
            return page;
        }
        PageUtils page = BeanUtil.toPagedResult(depotHitsDtos.subList((pageNo - 1) * pageSize, pageNo * pageSize));
        page.setTotalCount(total);
        page.setPageSize(pageSize);
        //TODO 设置总页数
        page.setTotalPage(BigDecimalMath.ceil(total.intValue(), pageSize));
        //TODO 设置当前页数
        page.setCurrPage(pageNo);
        return page;
    }

    /***
     *提供前端用户查询
     */
    public Map getMbrBetListReport(String siteCode, String loginName,
                                   String platform, String betStrTime, String betEndTime, Integer roleId) {
        GameReportQueryModel model = new GameReportQueryModel();
        model.setSiteCode(siteCode);
        model.setLoginName(loginName);
        model.setPlatform(platform);
        model.setBetStrTime(betStrTime);
        model.setBetEndTime(betEndTime);
        Map map = getRptBetListReport(model);

        //查询用户具备得列权限功能
        List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuth(roleId, ColumnAuthConstants.MEMBER_ASSET_DATA_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE);
        if (Collections3.isEmpty(menuList)) {
            throw new AuthorizationException("没有权限，请联系管理员授权");
        }

        for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
            //判断非空，并且不包含列则删除key
            if (!StringUtil.isEmpty(columnAuthTreeDto.getColumnName()) && !map.containsKey(columnAuthTreeDto.getColumnName())) {
                map.remove(columnAuthTreeDto.getColumnName());
            }
        }
        return map;
    }

    /***
     *代理前端调用
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils getAgentBkRptBetListPage(Integer pageNo, Integer pageSize, GameReportQueryModel model) {
        PageUtils pageUtils = getRptBetListPage(pageNo, pageSize, model);
        return pageUtils;
    }

    /***
     * V2后台调用，北京前端不能调用
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils getBkRptBetListPage(Integer pageNo, Integer pageSize, GameReportQueryModel model) {

        PageUtils pageUtils = getRptBetListPage(pageNo, pageSize, model);
        List<RptBetModel> list = (List<RptBetModel>) pageUtils.getList();
        if (list.size() != 0) {
            //获取小计
            list.add(getSubtotal(list));
            //获取总计
            list.add(getTotal(model));
        }
        return pageUtils;
    }


    /***
     * 代理后台调用
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils getBkRptBetListPageForAgent(Integer pageNo, Integer pageSize, GameReportQueryModel model) {
        PageUtils pageUtils = getRptBetListPage(pageNo, pageSize, model);
        List<RptBetModel> list = (List<RptBetModel>) pageUtils.getList();
        if (list.size() != 0) {
            //获取小计
            RptBetModel rptBetModel = getSubtotal(list);
            rptBetModel.setGameName("");
            rptBetModel.setStatus("小计");
            list.add(rptBetModel);
            //获取总计
            RptBetModel rptBetModel2 = getTotal(model);
            rptBetModel2.setGameName("");
            rptBetModel2.setStatus("总计");
            list.add(rptBetModel2);
        }
        return pageUtils;
    }

    /***
     * 查询所有注单
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils getRptBetListPage(Integer pageNo, Integer pageSize, GameReportQueryModel model) {
        List<RptBetModel> list = new ArrayList<>();
        JSONArray hits = new JSONArray();
        try {
            BoolQueryBuilder builder = setEsQuery(model);
            SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
            searchRequestBuilder.addSort(SortBuilders.fieldSort("betTime").order(SortOrder.DESC));
            searchRequestBuilder.setQuery(builder)
                    .setFrom((pageNo - 1) * pageSize)
                    .setSize(pageSize);
            Response response = connection.restClient_Read.performRequest("GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search", Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));
            // 获取返回前端的RptBetModel List
            for (Object obj : hits) {
                Map objmap = (Map) obj;
                RptBetModel rptBetModel = JSON.parseObject(objmap.get("_source").toString(), RptBetModel.class);
                if (StringUtil.isEmpty(rptBetModel.getGameCategory())) {
                    rptBetModel.setGameCategory("Unknown");
                }
                if (Boolean.TRUE.equals(model.getIsSdyNet())) {
                    if ("未结算".equals(rptBetModel.getStatus())) {
                        rptBetModel.setStatus("0");
                    } else if ("已结算".equals(rptBetModel.getStatus()) || "拒绝".equals(rptBetModel.getStatus())) {
                        rptBetModel.setStatus("1");
                    } else {
                        rptBetModel.setStatus("2");
                    }
                }
                list.add(rptBetModel);
            }
            // 获取GameType和tagencyId
            if (Collections3.isNotEmpty(list)) {
                List<String> loginNames = list.stream().map(RptBetModel::getUserName).collect(Collectors.toList());
                List<Map> tagencyIdList = getMbrTagencyIds(ImmutableMap.of("loginNames", loginNames));

                list.stream().forEach(ls -> {
                	if ("Combination".equalsIgnoreCase(ls.getGameType())) {
                		ls.setIsCombination(true);
                	}
                	
                    ls.setCatid(String.valueOf(Constants.depotCatGameTypeMap.get(ls.getGameCategory())));
                    // 设置游戏类别
                    ls.setGameType(Constants.depotCatStrMap.get(ls.getGameCategory()));
                    // 设置代理
                    for (Map obj2 : tagencyIdList) {
                        if (ls.getUserName().equals(obj2.get("loginName"))) {
                            ls.setTagencyId(Integer.valueOf(String.valueOf(obj2.get("tagencyId"))));
                            ls.setAgyAccount(String.valueOf(obj2.get("agyaccount")));
                        }
                    }
                    // 处理两位小数
                    if(Objects.nonNull(ls.getBet())){
                        ls.setBet(ls.getBet().setScale(2, BigDecimal.ROUND_DOWN));
                    }
                    if(Objects.nonNull(ls.getValidBet())){
                        ls.setValidBet(ls.getValidBet().setScale(2, BigDecimal.ROUND_DOWN));
                    }
                    if(Objects.nonNull(ls.getPayout())){
                        ls.setPayout(ls.getPayout().setScale(2, BigDecimal.ROUND_DOWN));
                    }
                });
            }
            //前端统一显示depotNamme（转换）
            depotCodeConverDepotNamme(list);
            //注单详情数据封装
            setOpenResultDetail(list);
            //总记录数
            Long total = Long.parseLong(((Map) map.get("hits")).get("total") + "");
            PageUtils page = BeanUtil.toPagedResult(list);
            page.setPageSize(pageSize);
            page.setTotalCount(total);
            //总页数
            page.setTotalPage(BigDecimalMath.ceil(total.intValue(), pageSize));
            //当前页数
            page.setCurrPage(pageNo);
            // 特殊处理
            page.setPageTotalCount(total);
            if (total > 10000) {
                page.setPageTotalCount(10000);
            }

            return page;
        } catch (Exception e) {
            log.error("查询结果:" + hits.toJSONString());
            log.error("getRptBetListPage", e);
            throw new RRException("查询异常!");
        }
    }


    private void queryScrollRptBet(String scrollId, List<RptBetModel> list) throws IOException {
        int num = 0;
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(scrollId)) {
            while (true) {
                log.info("export==查询注单==游标查询剩余==scrollId==" + scrollId + "==num==" + num);
                Long startTime = System.currentTimeMillis();

                String scrollQuerySql = String.format("{\"scroll\":\"%s\",\"scroll_id\":\"%s\"}", "15m", scrollId);
                Response sroresponse = connection.restClient_Read.performRequest(
                        "GET", "/_search/scroll", Collections.emptyMap(),
                        new NStringEntity(scrollQuerySql, ContentType.APPLICATION_JSON));
                Map map = (Map) JSON.parse(EntityUtils.toString(sroresponse.getEntity()));
                JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));

                log.info("export==查询注单==游标查询剩余==scrollId==" + scrollId + "==num==" + num + "==size==" + hits.size() + "==time==" + (System.currentTimeMillis() - startTime));
                if (hits.size() == 0) {
                    log.info("export==查询注单==游标查询剩余==scrollId==" + scrollId + "==num==" + num + "==scrollDelete==start");
                    Long startTime1 = System.currentTimeMillis();
                    String scrollDeleteSql = String.format("{\"scroll_id\":\"%s\"}", scrollId);
                    searchConnection.restClient.performRequest(
                            "DELETE", "/_search/scroll", Collections.emptyMap(),
                            new NStringEntity(scrollDeleteSql, ContentType.APPLICATION_JSON));
                    log.info("export==查询注单==游标查询剩余==scrollId==" + scrollId + "==num==" + num + "==scrollDelete==end==time==" + (System.currentTimeMillis() - startTime));
                    break;
                }
                for (Object obj : hits) {
                    Map objmap = (Map) obj;
                    RptBetModel dto = JSON.parseObject(objmap.get("_source").toString(), RptBetModel.class);
                    list.add(dto);
                }
                num++;
            }
        }
    }

    /***
     * 查询所有注单
     * @return
     */
    public List<RptBetModel> getRptBetList(GameReportQueryModel model) throws Exception {
        // 查询结果list
        List<RptBetModel> list = new ArrayList<>();

        // 1.设置查询条件query
        BoolQueryBuilder builder = setEsQuery(model);

        // 2. 设置查询builder
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        searchRequestBuilder.addSort(SortBuilders.fieldSort("betTime").order(SortOrder.DESC));
        searchRequestBuilder.setQuery(builder)
                .setFrom(0)
                .setSize(10000);

        // 3. 执行游标查询请求，初始化游标查询，获取_scroll_id：游标保存15分钟
        log.info("export==查询注单==初始化游标==start");
        Long startTime = System.currentTimeMillis();
        Response response = connection.restClient_Read.performRequest(
                "GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE
                        + "/_search?scroll=15m",
                Collections.singletonMap("_source", "true"),
                new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON)
        );
        log.info("export==查询注单==初始化游标==end==" + (System.currentTimeMillis() - startTime));
//        log.error("-----------------------------------请求参数" + searchRequestBuilder.toString() + "--------------------------------------");

        // 查询结果处理
        Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
        String scrollId = String.valueOf(map.get("_scroll_id"));                // 游标_scroll_id
        //总记录数
        Long total = Long.parseLong(((Map) map.get("hits")).get("total") + "");
        if (total > 250000L) {
            throw new R200Exception("导出数量超过25W条，请更新搜索条件后再进行导出！");
        }
        JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));   // 命中数据
        log.info("export==查询注单==初始化游标==end==size==" + hits.size());
        for (Object obj : hits) {
            Map objmap = (Map) obj;
            RptBetModel rptBetModel = JSON.parseObject(objmap.get("_source").toString(), RptBetModel.class);
            list.add(rptBetModel);
        }

        // 游标查询剩余
        log.info("export==查询注单==游标查询剩余==start");
        Long startTime1 = System.currentTimeMillis();
        queryScrollRptBet(scrollId, list);
        log.info("export==查询注单==游标查询剩余==end==" + (System.currentTimeMillis() - startTime1));

        // 返回数据list其他处理
        log.info("export==查询注单==返回数据list其他处理==start");
        Long startTime2 = System.currentTimeMillis();
        if (Collections3.isNotEmpty(list)) {

            List<Map> tagencyIdList = Lists.newArrayList();
            if (Boolean.TRUE.equals(model.getIsEgBetDetauls())) {
                List<String> loginNames = list.stream().map(RptBetModel::getUserName).collect(Collectors.toList());
                Map<String, Object> loginNameMap = new HashMap<>();
                loginNameMap.put("loginNames", loginNames);
                tagencyIdList = getMbrTagencyIds(loginNameMap);
            }

            List<Map> finalTagencyIdList = tagencyIdList;
            list.stream().forEach(ls -> {
                // 设置游戏类别
                ls.setGameType(Constants.depotCatStrMap.get(ls.getGameCategory()));
                // 设置代理
                for (Map obj2 : finalTagencyIdList) {
                    if (ls.getUserName().equals(obj2.get("loginName"))) {
                        ls.setTagencyId(Integer.valueOf(String.valueOf(obj2.get("tagencyId"))));
                        ls.setAgyAccount(String.valueOf(obj2.get("agyaccount")));
                    }
                }
            });
        }
        log.info("export==查询注单==返回数据list其他处理==end==time==" + (System.currentTimeMillis() - startTime2));
        //前端统一显示depotNamme（转换）
//            depotCodeConverDepotNamme(list);
        //注单详情数据封装
        setOpenResultDetail(list);
        return list;
    }

    public Map getRptBetListReport(GameReportQueryModel model) {
        Map<String, Object> rsmap = new HashMap<>();

        try {
            BoolQueryBuilder builder = setEsQuery(model);
            SumAggregationBuilder betaggs = AggregationBuilders.sum("bet").field("bet");
            SumAggregationBuilder validbetaggs = AggregationBuilders.sum("validBet").field("validBet");
            SumAggregationBuilder rewardaggs = AggregationBuilders.sum("payout").field("payout");
            SumAggregationBuilder jackpotBetAggs = AggregationBuilders.sum("jackpotBet").field("jackpotBet");
            SumAggregationBuilder jackpotPayoutAggs = AggregationBuilders.sum("jackpotPayout").field("jackpotPayout");
            CardinalityAggregationBuilder userNameAggs = AggregationBuilders.cardinality("userName").field("userName");


            SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
            searchRequestBuilder.addAggregation(betaggs);
            searchRequestBuilder.addAggregation(validbetaggs);
            searchRequestBuilder.addAggregation(rewardaggs);
            searchRequestBuilder.addAggregation(jackpotBetAggs);
            searchRequestBuilder.addAggregation(jackpotPayoutAggs);
            searchRequestBuilder.addAggregation(userNameAggs);
            searchRequestBuilder.setQuery(builder);

            Response response = connection.restClient_Read.performRequest("GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search", Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
//            log.info(builder.toString());
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            Integer counts = (Integer) ((Map) map.get("hits")).get("total");

            if (null != (Map) map.get("aggregations")) {
                BigDecimal bet = (BigDecimal) ((Map) ((Map) map.get("aggregations")).get("bet")).get("value");
                BigDecimal reward = (BigDecimal) ((Map) ((Map) map.get("aggregations")).get("payout")).get("value");
                BigDecimal validbet = (BigDecimal) ((Map) ((Map) map.get("aggregations")).get("validBet")).get("value");
                BigDecimal jackpotBet = (BigDecimal) ((Map) ((Map) map.get("aggregations")).get("jackpotBet")).get("value");
                BigDecimal jackpotPayout = (BigDecimal) ((Map) ((Map) map.get("aggregations")).get("jackpotPayout")).get("value");
                Integer betCount = (Integer) ((Map) ((Map) map.get("aggregations")).get("userName")).get("value");
                rsmap.put("counts", counts);
                //总投注
                rsmap.put("bet", bet.setScale(2, BigDecimal.ROUND_DOWN));
                //总派彩
                rsmap.put("payout", reward.setScale(2, BigDecimal.ROUND_HALF_UP));
                //有效投注
                rsmap.put("validBet", validbet.setScale(2, BigDecimal.ROUND_HALF_UP));
                rsmap.put("jackpotBet", jackpotBet.setScale(2, BigDecimal.ROUND_DOWN));
                rsmap.put("jackpotPayout", jackpotPayout.setScale(2, BigDecimal.ROUND_DOWN));
                rsmap.put("betCount", betCount);
            } else {
                rsmap.put("counts", counts);
                //总投注
                rsmap.put("bet", BigDecimal.ZERO);
                //总派彩
                rsmap.put("payout", BigDecimal.ZERO);
                //有效投注
                rsmap.put("validBet", BigDecimal.ZERO);
                rsmap.put("jackpotBet", BigDecimal.ZERO);
                rsmap.put("jackpotPayout", BigDecimal.ZERO);
                rsmap.put("betCount", 0);
            }

            return rsmap;
        } catch (Exception e) {
            log.error("getRptBetListReport", e);
            throw new RRException("查询异常!");
        }
    }

    /***
     * 查询财务费用注单，既奖池投注
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils getJackpotBetListPage(Integer pageNo, Integer pageSize, GameReportQueryModel model) {
        List<RptBetModel> list = new ArrayList<>();
        try {
            BoolQueryBuilder builder = setEsQuery(model);
            SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
            searchRequestBuilder.addSort(SortBuilders.fieldSort("betTime").order(SortOrder.DESC));
            searchRequestBuilder.setQuery(builder)
                    .setFrom((pageNo - 1) * pageSize)
                    .setSize(pageSize);
            Response response = connection.restClient_Read.performRequest("GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search", Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
//            log.info(builder.toString());
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));
            for (Object obj : hits) {
                Map objmap = (Map) obj;
                list.add(JSON.parseObject(objmap.get("_source").toString(), RptBetModel.class));
            }
            //总记录数
            Long total = Long.parseLong(((Map) map.get("hits")).get("total") + "");
            PageUtils page = BeanUtil.toPagedResult(list);
            page.setTotalCount(total);
            //总页数
            page.setTotalPage(BigDecimalMath.ceil(total.intValue(), pageSize));
            //当前页数
            page.setCurrPage(pageNo);
            return page;
        } catch (Exception e) {
            log.error("getJackpotBetListPage", e);
            throw new RRException("查询异常!");
        }
    }

    public PageUtils getRptBetDay(Integer pageNo, Integer pageSize, Integer parentAgentid, Integer agentid, Integer groupid,
                                  String loginName, String platform, String gametype, String betStrTime, String betEndTime, String orderBy, String group) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        StringBuffer sb = new StringBuffer();
        if (group != null) {
            String[] gs = group.split(",");
            try {
                for (String groupStr : gs) {
                    sb.append(groupStr.replace("topAgent", "topagt.agyAccount as topAgent").replace("agent", "agt.agyAccount as agent"));
                    sb.append(",");
                }
            } catch (Exception e) {
                log.error("getRptBetDay", e);
                throw new RRException("查询异常!");
            }
        }
        List<RptBetDayModel> list = analysisMapper.getRptBetDay(parentAgentid, agentid, groupid, loginName, platform, gametype, betStrTime, betEndTime, group, sb.toString().length() > 0 ? sb.toString().substring(0, sb.length() - 1) : "");
        return BeanUtil.toPagedResult(list);
    }

    public Map getFundStatistics(Integer parentAgentid, Integer agentid, Integer groupid,
                                 String loginName, String platform, String gametype, String betStrTime, String betEndTime) {
        List<FundStatisticsModel> fundlist = analysisMapper.getFundStatistics(parentAgentid, agentid, groupid, loginName, platform, gametype, betStrTime, betEndTime);
        Map resultMap = new HashMap();
        Map legend = new HashMap();
        Map xAxis = new HashMap();
        Map yAxis = new HashMap();
        Map[] maps = new Map[4];
        Map payoutMap = new HashMap();
        Map depositsMap = new HashMap();
        Map withdrawMap = new HashMap();
        Map profitMap = new HashMap();
        BigDecimal[] payouts = new BigDecimal[fundlist.size()];
        BigDecimal[] deposits = new BigDecimal[fundlist.size()];
        BigDecimal[] withdraws = new BigDecimal[fundlist.size()];
        BigDecimal[] profits = new BigDecimal[fundlist.size()];
        String[] xAxisData = new String[fundlist.size()];
        for (int i = 0; i < fundlist.size(); i++) {
            FundStatisticsModel fund = fundlist.get(i);

            xAxisData[i] = fund.getDate();
            payouts[i] = fund.getPayout();
            deposits[i] = fund.getDeposit();
            withdraws[i] = fund.getWithdraw();
            profits[i] = fund.getProfit();

        }
        payoutMap.put("name", "派彩");
        payoutMap.put("data", payouts);
        depositsMap.put("name", "存款");
        depositsMap.put("data", deposits);
        withdrawMap.put("name", "提款");
        withdrawMap.put("data", withdraws);
        profitMap.put("name", "优惠");
        profitMap.put("data", profits);
        maps[0] = payoutMap;
        maps[1] = depositsMap;
        maps[2] = withdrawMap;
        maps[3] = profitMap;
        legend.put("data", new String[]{"派彩", "存款", "提款", "优惠"});
        xAxis.put("data", xAxisData);
        resultMap.put("legend", legend);
        resultMap.put("xAxis", xAxis);
        resultMap.put("yAxis", yAxis);
        resultMap.put("series", maps);
        return resultMap;
    }

    public FundReportModel getFundReport(Integer parentAgentid, Integer agentid, Integer groupid, String betStrTime, String betEndTime) {
        FundReportModel nowFund = analysisMapper.getFundReport(parentAgentid, agentid, groupid, betStrTime, betEndTime);
        FundReportModel oldFund = analysisMapper.getFundReport(parentAgentid, agentid, groupid, lessYear(betStrTime), lessYear(betEndTime));
        nowFund.setPayoutPercent(getPercent(nowFund.getPayout(), oldFund.getPayout()));
        nowFund.setMemberWithdrawPercent(getPercent(nowFund.getMemberWithdraw(), oldFund.getMemberWithdraw()));
        nowFund.setAgyWithdrawPercent(getPercent(nowFund.getAgyWithdraw(), oldFund.getAgyWithdraw()));
        nowFund.setCommissionPercent(getPercent(nowFund.getCommission(), oldFund.getCommission()));
        nowFund.setDiscountPercent(getPercent(nowFund.getDiscount(), oldFund.getDiscount()));
        nowFund.setProfitPercent(getPercent(nowFund.getProfit(), oldFund.getProfit()));
        return nowFund;
    }


    /***
     * 生成es查询组合
     * @return
     * @throws Exception
     */
    public BoolQueryBuilder setEsQuery(GameReportQueryModel model) throws Exception {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        /**传入查询的前缀**/
        builder.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(model.getSiteCode()))));
        if (model.getParentAgentid() != null || model.getAgentid() != null || model.getGroupid() != null || model.getSubCagencyId() != null
                || StringUtil.isNotEmpty(model.getSupLoginName())
                || !StringUtil.isEmpty(model.getTagencyIdList()) || !StringUtil.isEmpty(model.getCagencyIdList())) {
            MbrAccount mbr = new MbrAccount();
            mbr.setTagencyId(model.getParentAgentid());
            mbr.setCagencyId(model.getAgentid());
            mbr.setGroupId(model.getGroupid());
            mbr.setTagencyIdList(model.getTagencyIdList());
            mbr.setCagencyIdList(model.getCagencyIdList());
            mbr.setSubCagencyId(model.getSubCagencyId());
            List<String> usernameList;
            //获取符合代理线和会员组的会员名称
            if (StringUtil.isNotEmpty(model.getSupLoginName())) {
                mbr.setLoginName(model.getSupLoginName());
                List<MbrAccount> subMbrList = mbrMapper.findSubAccounts(mbr);
                usernameList = subMbrList.stream().map(MbrAccount::getLoginName).collect(Collectors.toList());
            } else {
                usernameList = mbrMapper.getMemberAccountNames(mbr);
            }
            builder.must(QueryBuilders.termsQuery("userName", toLowerCase(usernameList)));
        }
        if (model.getLoginName() != null && !"".equals(model.getLoginName())) {
            builder.must(QueryBuilders.termsQuery("userName", model.getLoginName().toLowerCase()));
        }
        if (model.getPlatform() != null && !"".equals(model.getPlatform())) {
            builder.must(QueryBuilders.matchPhraseQuery("platform", analysisMapper.getDepotNameToDepotCode(model.getPlatform())));
        }
        if (model.getGametype() != null && !"".equals(model.getGametype())) {
            String gameTypeValue = null;
            if (GameTypeEnum.ENUM_ONE.getKey() == Integer.parseInt(model.getGametype())) {
                gameTypeValue = GameTypeEnum.ENUM_ONE.getValue();
//                List<String> gameTypeValues = Lists.newArrayList(GameTypeEnum.ENUM_ONE.getValue(),"esport");
                builder.must(QueryBuilders.termsQuery("gameCategory", gameTypeValue));
            } else if (GameTypeEnum.ENUM_THREE.getKey() == Integer.parseInt(model.getGametype())) {
                gameTypeValue = GameTypeEnum.ENUM_THREE.getValue();
                builder.must(QueryBuilders.termsQuery("gameCategory", gameTypeValue));
            } else if (GameTypeEnum.ENUM_FIVE.getKey() == Integer.parseInt(model.getGametype())) {
                gameTypeValue = GameTypeEnum.ENUM_FIVE.getValue();
                builder.must(QueryBuilders.termsQuery("gameCategory", gameTypeValue));
            } else if (GameTypeEnum.ENUM_SIX.getKey() == Integer.parseInt(model.getGametype())) {
                gameTypeValue = GameTypeEnum.ENUM_SIX.getValue();
                builder.must(QueryBuilders.termsQuery("gameCategory", gameTypeValue));
            } else if (GameTypeEnum.ENUM_EIGHT.getKey() == Integer.parseInt(model.getGametype())) {
                gameTypeValue = GameTypeEnum.ENUM_EIGHT.getValue();
                builder.must(QueryBuilders.termsQuery("gameCategory", gameTypeValue));
            } else if (GameTypeEnum.ENUM_TWELVE.getKey() == Integer.parseInt(model.getGametype())) {
                gameTypeValue = GameTypeEnum.ENUM_TWELVE.getValue();
                builder.must(QueryBuilders.termsQuery("gameCategory", gameTypeValue));
            } else if (GameTypeEnum.ENUM_NINE.getKey() == Integer.parseInt(model.getGametype())) {
                gameTypeValue = GameTypeEnum.ENUM_NINE.getValue();
                builder.must(QueryBuilders.termsQuery("gameCategory", gameTypeValue));
            } else if (GameTypeEnum.ENUM_TIPS.getKey() == Integer.parseInt(model.getGametype())) {      // 打赏tip
                gameTypeValue = GameTypeEnum.ENUM_TIPS.getValue();
                builder.must(QueryBuilders.termsQuery("gameCategory", gameTypeValue));
            } else if (GameTypeEnum.ENUM_FORTY_NINE.getKey() == Integer.parseInt(model.getGametype())) { // 斗鸡
                gameTypeValue = GameTypeEnum.ENUM_FORTY_NINE.getValue();
                builder.must(QueryBuilders.termsQuery("gameCategory", gameTypeValue));
            }

        }
        /*if ((model.getPlatform() != null && !model.getPlatform().equals("")) || (model.getGametype() != null && !model.getGametype().equals("")) || (model.getGamename() != null && !model.getGamename().equals(""))) {
            builder.must(createGameQuery(model));
        }*/
        if (model.getOrigin() != null && !"".equals(model.getOrigin())) {
            builder.must(QueryBuilders.matchPhraseQuery("origin", model.getOrigin()));
        }
        /**注单ID**/
        if (model.getBetid() != null && !"".equals(model.getBetid())) {
//            builder.must(QueryBuilders.queryStringQuery(model.getBetid().toString()).defaultField("id"));
            builder.filter(QueryBuilders.matchPhraseQuery("id", model.getBetid().toString()));  // filter不计算权重，性能高于must
            //builder.filter(QueryBuilders.termsQuery("id", Splitter.on(",").trimResults().splitToList(model.getBetid())));
        }
        /**状态**/
        if (model.getStatus() != null && !"".equals(model.getStatus())) {
//            builder.must(QueryBuilders.queryStringQuery(Integer.parseInt(model.getStatus()) == 0 ? "未" : "已").defaultField("status"));
        	List statusList = Integer.parseInt(model.getStatus()) == 0 ? Arrays.asList("未") : Arrays.asList("已", "拒");
            builder.must(QueryBuilders.termsQuery("status", statusList));
        }
        /**结果：输、赢**/
        if (model.getResult() != null && !"".equals(model.getResult())) {
            builder.must(QueryBuilders.termsQuery("result", model.getResult()));
        }
        /**彩金下注**/
        if (model.getGtJpBet() != null) {
            builder.must(QueryBuilders.rangeQuery("jackpotBet").gte(model.getGtJpBet()));
        }
        if (model.getLtJpBet() != null) {
            builder.must(QueryBuilders.rangeQuery("jackpotBet").lte(model.getLtJpBet()));
        }
        /**彩金中奖**/
        if (model.getGtJpReward() != null) {
            builder.must(QueryBuilders.rangeQuery("jackpotPayout").gte(model.getGtJpReward()));
        }
        if (model.getLtJpReward() != null) {
            builder.must(QueryBuilders.rangeQuery("jackpotPayout").lte(model.getLtJpReward()));
        }
        /**下注**/
        if (model.getGtBet() != null) {
            builder.must(QueryBuilders.rangeQuery("bet").gte(model.getGtBet()));
        }
        /**下注**/
        if (model.getLtBet() != null) {
            builder.must(QueryBuilders.rangeQuery("bet").lte(model.getLtBet()));
        }
        /**有效下注**/
        if (model.getGtValidBet() != null) {
            builder.must(QueryBuilders.rangeQuery("validBet").gte(model.getGtValidBet()));
        }
        /**有效下注**/
        if (model.getLtValidBet() != null) {
            builder.must(QueryBuilders.rangeQuery("validBet").lte(model.getLtValidBet()));
        }
        /**派彩**/
        if (model.getGtReward() != null) {
            builder.must(QueryBuilders.rangeQuery("payout").gte(model.getGtReward()));
        }
        /**派彩**/
        if (model.getLtReward() != null) {
            builder.must(QueryBuilders.rangeQuery("payout").lte(model.getLtReward()));
        }
        /**大于开始时间**/
        if (StringUtil.isNotEmpty(model.getBetStrTime())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
            SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
            builder.must(QueryBuilders.rangeQuery("betTime").gte(dateFormatSdf.format(dateFormat.parse(model.getBetStrTime()))));
        }
        /**小于结束时间**/
        if (StringUtil.isNotEmpty(model.getBetEndTime())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
            SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
            builder.must(QueryBuilders.rangeQuery("betTime").lte(dateFormatSdf.format(dateFormat.parse(model.getBetEndTime()))));
        }
        /**大于派彩开始时间**/
        if (StringUtil.isNotEmpty(model.getPayOutStrTime())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
            SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
            builder.must(QueryBuilders.rangeQuery("payoutTime").gte(dateFormatSdf.format(dateFormat.parse(model.getPayOutStrTime())))
                    .lte(dateFormatSdf.format(dateFormat.parse(model.getPayOutEndTime()))));
        }
        /**小于派彩结束时间**/
        if (StringUtil.isNotEmpty(model.getPayOutEndTime())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
            SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
            builder.must(QueryBuilders.rangeQuery("payoutTime").lte(dateFormatSdf.format(dateFormat.parse(model.getPayOutEndTime()))));
        }
        /**大于下载开始时间**/
        if (StringUtil.isNotEmpty(model.getDownloadStrTime())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
            SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
            builder.must(QueryBuilders.rangeQuery("downloadTime").gte(dateFormatSdf.format(dateFormat.parse(model.getDownloadStrTime())))
                    .lte(dateFormatSdf.format(dateFormat.parse(model.getDownloadEndTime()))));
        }
        /**小于下载结束时间**/
        if (StringUtil.isNotEmpty(model.getDownloadEndTime())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
            SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
            builder.must(QueryBuilders.rangeQuery("downloadTime").lte(dateFormatSdf.format(dateFormat.parse(model.getDownloadEndTime()))));
        }
        if (model.getTableNo() != null && !"".equals(model.getTableNo())) {
            builder.must(QueryBuilders.termsQuery("tableNo", model.getTableNo().toLowerCase()));
        }
        if (model.getSerialId() != null && !"".equals(model.getSerialId())) {
            builder.must(QueryBuilders.matchPhraseQuery("serialId", model.getSerialId().toLowerCase()));
        }
        if (org.apache.commons.lang.StringUtils.isNotEmpty(model.getGamename())) {
            builder.must(QueryBuilders.matchPhrasePrefixQuery("gameName", model.getGamename()));
        }
        return builder;
    }

    /***
     * 输赢报表会员查询（精确到时/分/秒）生成es查询组合
     * @return
     * @throws Exception
     */
    private BoolQueryBuilder setEsQuery(WinLostEsQueryModel model) throws Exception {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        List<String> list = new ArrayList<>();
        if (StringUtil.isNotEmpty(model.getAccountId())) {
            list.add(mbrAccountMapper.selectByPrimaryKey(Integer.parseInt(model.getAccountId())).getLoginName());
            /**传入查询的会员名**/
            builder.must(QueryBuilders.termsQuery("userName", toLowerCase(list)));
        }
        /**传入查询的前缀**/
        builder.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(model.getSiteCode()))));

        /**大于开始时间**/
        if (model.getStartTime() != null && !"".equals(model.getStartTime())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
            SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
            builder.must(QueryBuilders.rangeQuery("betTime").gte(dateFormatSdf.format(dateFormat.parse(model.getStartTime()))));
        }
        /**小于结束时间**/
        if (model.getEndTime() != null && !"".equals(model.getEndTime())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
            SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
            builder.must(QueryBuilders.rangeQuery("betTime").lte(dateFormatSdf.format(dateFormat.parse(model.getEndTime()))));
        }
        if ((model.getDepotId() != null && !"".equals(model.getDepotId())) || (model.getCatId() != null && !"".equals(model.getCatId())) || (model.getSubCatId() != null && !"".equals(model.getSubCatId()))) {
            builder.must(createGameQuery(model));
        }
        if (model.getCatId() != null && !"".equals(model.getCatId())) {
            builder.must(QueryBuilders.matchPhraseQuery("gameType", analysisMapper.getCatName(Integer.parseInt(model.getCatId()))));
        }
        if (model.getSubCatId() != null && !"".equals(model.getCatId())) {
            builder.must(QueryBuilders.matchPhraseQuery("platform", model.getSubCatId()));
        }
        return builder;
    }


    private BoolQueryBuilder createGameQuery(WinLostEsQueryModel model) {
        BoolQueryBuilder gameQuery = QueryBuilders.boolQuery();
        //根据游戏分类查询该分类下的游戏
        List<TGmGame> games = analysisMapper.getGameCodeByCat((StringUtil.isNotEmpty(model.getDepotId())) ? analysisMapper.getDepotName(Integer.parseInt(model.getDepotId())) : "", model.getCatId(), model.getSubCatId());
        Map depotMap = new HashMap();
        List<String> depotList = new ArrayList();
        for (TGmGame gmGame : games) {
            depotMap.put(gmGame.getDepotName(), null);
        }
        Iterator iterator = depotMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object key = entry.getKey();
            depotList.add(key.toString());
        }
        for (String depot : depotList) {
            BoolQueryBuilder gameQueryList = new BoolQueryBuilder();
            for (TGmGame game : games) {
                if (game.getDepotName().equals(depot)) {
                    gameQueryList.should(QueryBuilders.queryStringQuery(game.getGameCode().toLowerCase()).defaultField("gameType"));
                }
            }
            BoolQueryBuilder depotQuery = QueryBuilders.boolQuery().must(QueryBuilders.queryStringQuery(depot.toLowerCase().toLowerCase()).defaultField("platform")).must(gameQueryList);
            gameQuery.should(depotQuery);
        }
        return gameQuery;
    }

    /**
     * 根据游戏平台及游戏类型获取有效投注
     *
     * @param sitePrefix
     * @param userlist
     * @param startTime
     * @param endTime
     * @param gamelist
     * @param status
     * @param platForms
     * @param bounsCount 未通过的优惠稽核个数
     *                   1. 大于0表示存在未通过优惠稽核，则不反水游戏列表不计算稽核，取validBet
     *                   2. 空或为0，表示不存在未通过优惠稽核或已通过优惠稽核，则不反水游戏列表计算稽核，取originalValidBet
     * @return
     */
    public List<RptBetModel> getValidBet(String sitePrefix, List<String> userlist, String startTime,
                                         String endTime, List<TGmGame> gamelist, String status,
                                         List<String> platForms, Long bounsCount) {
        List<RptBetModel> rslist = new ArrayList<>();
        TermsAggregationBuilder agg = AggregationBuilders.terms("userName").field("userName").
                subAggregation(AggregationBuilders.sum("validBet").field("validBet"))
                .subAggregation(AggregationBuilders.sum("originalValidBet").field("originalValidBet"))
                .subAggregation(AggregationBuilders.sum("payout").field("payout"));
        agg.size(ElasticSearchConstant.SEARCH_COUNT);

        BoolQueryBuilder query = QueryBuilders.boolQuery();//查询组合
        query.must(QueryBuilders.rangeQuery("betTime").gte(startTime).lt(endTime));
        query.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(sitePrefix))));

        if (Collections3.isNotEmpty(userlist)) {
            query.must(QueryBuilders.termsQuery("userName", toLowerCase(userlist)));
        }
        if (StringUtil.isNotEmpty(status)) {
            query.must(QueryBuilders.termsQuery("status", status));
        }

        BoolQueryBuilder pfbuilder = QueryBuilders.boolQuery();
        platForms.forEach(pf -> {
            pfbuilder.should(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("platform", pf.toLowerCase())));
        });
        query.must(pfbuilder);

        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        if (Collections3.isNotEmpty(gamelist)) {
            gamelist.stream().forEach(gmGame -> {
                builder.should(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termsQuery("platform", gmGame.getDepotName().toLowerCase()))
                        .must(QueryBuilders.termsQuery("gameType", gmGame.getGameCode().toLowerCase())));
            });
        }
        query.must(builder);
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        searchRequestBuilder.setQuery(query);
        searchRequestBuilder.addAggregation(agg);
        String str = searchRequestBuilder.toString();
//        log.info("稽核获取有效投注额请求参数【" + str + "】");
        try {
            Response response = connection.restClient_Read.performRequest("GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"), new NStringEntity(str, ContentType.APPLICATION_JSON));
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            for (Object obj : (JSONArray) ((Map) ((Map) map.get("aggregations")).get("userName")).get("buckets")) {
                Map rs = new HashMap();
                Map objmap = (Map) obj;
                RptBetModel rb = new RptBetModel();
                rb.setUserName(objmap.get("key").toString());
               /* rb.setValidBet(((BigDecimal) ((Map) objmap.get("validBet")).get("value"))
                        .setScale(2, BigDecimal.ROUND_DOWN));
                if (isNull(bounsCount) || bounsCount == 0) {
                    rb.setValidBet(((BigDecimal) ((Map) objmap.get("originalValidBet")).get("value"))
                            .setScale(2, BigDecimal.ROUND_DOWN));
                }*/
                rb.setValidBet(((BigDecimal) ((Map) objmap.get("originalValidBet")).get("value"))
                        .setScale(2, BigDecimal.ROUND_DOWN));
                rb.setPayout(((BigDecimal) ((Map) objmap.get("payout")).get("value"))
                        .setScale(2, BigDecimal.ROUND_DOWN));
                rslist.add(rb);
            }
        } catch (Exception e) {
            log.error("getValidBet", e);
        }
        log.info("稽核获取有效投注额【" + JSON.toJSON(rslist) + "】");
        return rslist;
    }

    /****
     * 根据游戏平台、游戏集合获取总输赢
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Map> getUserWinLoss(String sitePrefix, List<String> userlist, String startTime, String endTime) {
        List<Map> rslist = new ArrayList<>();
        /** 统计派彩和奖池派彩**/
        TermsAggregationBuilder agg = AggregationBuilders.terms("userName").field("userName").
                subAggregation(AggregationBuilders.sum("payout").field("payout"))
                .subAggregation(AggregationBuilders.sum("jackpotPayout").field("jackpotPayout"))
                .subAggregation(AggregationBuilders.sum("bet").field("bet"));
        agg.size(ElasticSearchConstant.SEARCH_COUNT);
        //查询组合
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (Objects.nonNull(startTime)) {
            query.must(QueryBuilders.rangeQuery("orderDate").gte(startTime).lt(endTime));
        }
        query.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(sitePrefix))));
        query.must(QueryBuilders.termsQuery("userName", toLowerCase(userlist)));
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        query.must(builder);
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        searchRequestBuilder.setQuery(query);
        searchRequestBuilder.addAggregation(agg);
        String str = searchRequestBuilder.toString();
        try {
            Response response = connection.restClient_Read.performRequest("GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"), new NStringEntity(str, ContentType.APPLICATION_JSON));
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            for (Object obj : (JSONArray) ((Map) ((Map) map.get("aggregations")).get("userName")).get("buckets")) {
                Map rs = new HashMap();
                Map objmap = (Map) obj;
                rs.put(objmap.get("key"), ((BigDecimal) ((Map) objmap.get("payout")).get("value")).add(((BigDecimal) ((Map) objmap.get("jackpotPayout")).get("value")))
                        .setScale(2, BigDecimal.ROUND_DOWN));
                rslist.add(rs);
            }
        } catch (Exception e) {
            log.error("getUserWinLoss", e);
        }
        return rslist;
    }


    /****
     * 根据游戏平台、分类、游戏集合获取总输赢 投注开始时间和结束时间
     * @param startTime
     * @param endTime
     * @return
     */
    public List<RptBetTotalModel> getGameCategoryReport(String sitePrefix, String userName, List<String> platforms, String startTime, String endTime) {
        List<RptBetTotalModel> rslist = new ArrayList<>();
        Map<String, Map> pfMap = initGameTree(analysisMapper.getGameList(platforms));

        TermsAggregationBuilder agg = AggregationBuilders.terms("platform").field("platform")
                .subAggregation(AggregationBuilders.sum("payout").field("payout"))
                .subAggregation(AggregationBuilders.sum("validBet").field("validBet"))
                .subAggregation(AggregationBuilders.sum("bet").field("bet"))
                .subAggregation(AggregationBuilders.max("maxTime").field("betTime"))
                .subAggregation(AggregationBuilders.min("minTime").field("betTime"));
        agg.size(ElasticSearchConstant.SEARCH_COUNT);

        pfMap.entrySet().forEach(pf -> {
            String platform = pf.getKey();
            Map<String, List> cateMap = pf.getValue();
            cateMap.entrySet().forEach(cate -> {
                //查询组合
                BoolQueryBuilder query = QueryBuilders.boolQuery();
                if (Objects.nonNull(startTime)) {
                    query.must(QueryBuilders.rangeQuery("betTime").gte(startTime).lt(endTime));
                }
                query.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(sitePrefix))));
                query.must(QueryBuilders.termsQuery("userName", userName.toLowerCase()));
                BoolQueryBuilder builder = QueryBuilders.boolQuery();
                List<TGmGame> games = cate.getValue();
                games.forEach(game -> {
                    builder.should(QueryBuilders.boolQuery()
                            .must(QueryBuilders.termsQuery("platform", platform.toLowerCase()))
                            .must(QueryBuilders.termsQuery("gameType", game.getGameCode().toLowerCase())));
                });
                query.must(builder);
                SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
                searchRequestBuilder.setQuery(query);
                searchRequestBuilder.addAggregation(agg);
                String str = searchRequestBuilder.toString();
                try {
                    Response response = connection.restClient_Read.performRequest("GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search",
                            Collections.singletonMap("_source", "true"), new NStringEntity(str, ContentType.APPLICATION_JSON));
                    Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
                    for (Object obj : (JSONArray) ((Map) ((Map) map.get("aggregations")).get("platform")).get("buckets")) {
                        Map objmap = (Map) obj;
                        RptBetTotalModel betTotal = new RptBetTotalModel();
                        betTotal.setPlatform(platform);
                        betTotal.setGameCategory(cate.getKey());
                        betTotal.setBetTotal(((BigDecimal) ((Map) objmap.get("bet")).get("value"))
                                .setScale(2, BigDecimal.ROUND_DOWN));
                        betTotal.setValidBetTotal(((BigDecimal) ((Map) objmap.get("validBet")).get("value"))
                                .setScale(2, BigDecimal.ROUND_DOWN));
                        betTotal.setPayoutTotal(((BigDecimal) ((Map) objmap.get("payout")).get("value"))
                                .setScale(2, BigDecimal.ROUND_DOWN));
                        SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
                        SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
                        betTotal.setMinTime(dateFormat.format(dateFormatSdf.parse(((Map) objmap.get("minTime")).get("value_as_string").toString())));
                        betTotal.setMaxTime(dateFormat.format(dateFormatSdf.parse(((Map) objmap.get("minTime")).get("value_as_string").toString())));
                        rslist.add(betTotal);
                    }
                } catch (Exception e) {
                    log.error("getGameCategoryReport", e);
                }
                log.info("稽核获取有效投注额【" + JSON.toJSON(rslist) + "】");
            });
        });
        return rslist;
    }

    /***
     * 获取每个平台最新注单的时间
     * @param sitePrefix
     * @param userName
     * @return
     */
    public List<RptBetTotalModel> getPlatformMaxTime(String sitePrefix, String userName, String platform) {
        List<RptBetTotalModel> rslist = new ArrayList<>();
        TermsAggregationBuilder agg = AggregationBuilders.terms("platform").field("platform")
                .subAggregation(AggregationBuilders.max("maxTime").field("betTime"));
        agg.size(ElasticSearchConstant.SEARCH_COUNT);
        //查询组合
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        query.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(sitePrefix))));
        if (Objects.nonNull(userName)) {
            query.must(QueryBuilders.termsQuery("userName", userName.toLowerCase()));
        }
        if (StringUtil.isNotEmpty(platform)) {
            query.must(QueryBuilders.matchPhraseQuery("platform", platform));
        }
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        searchRequestBuilder.setQuery(query);
        searchRequestBuilder.addAggregation(agg);
        String str = searchRequestBuilder.toString();
//        log.info("获取每个平台最新注单的时间【" + str + "】");
        try {
            Response response = connection.restClient_Read.performRequest("GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"), new NStringEntity(str, ContentType.APPLICATION_JSON));
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            for (Object obj : (JSONArray) ((Map) ((Map) map.get("aggregations")).get("platform")).get("buckets")) {
                Map objmap = (Map) obj;
                RptBetTotalModel betTotal = new RptBetTotalModel();
                betTotal.setPlatform(objmap.get("key").toString());
                SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
                SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
                betTotal.setMaxTime(dateFormat.format(dateFormatSdf.parse(((Map) objmap.get("maxTime")).get("value_as_string").toString())));
                rslist.add(betTotal);
            }
            return rslist;
        } catch (Exception e) {
            log.error("getPlatformMaxTime", e);
        }
        return null;
    }


    public List<SelectModel> getPlatForm(String siteCode) {
        return analysisMapper.getPlatForm(siteCode);
    }


    public List<SelectModel> getPlatFormByCatCode(String siteCode, String catCode) {
        List<String> catCodeList = new ArrayList<>();
        if (catCode!=null && catCode.contains(",")) {
            String[] split = catCode.split(",");
            catCodeList.addAll(Arrays.asList(split));
        } else {
            if (org.apache.commons.lang.StringUtils.isNotBlank(catCode)) {
                catCodeList.add(catCode);
            }
        }
        return analysisMapper.getPlatFormByCatCode(siteCode, catCodeList);
    }

    public List<SelectModel> getPlatFormWithOrder(String siteCode) {
        return analysisMapper.getPlatFormWithOrder(siteCode);
    }

    public List<SelectModel> getGameType(String platFormId, Integer parentId, String siteCode) {
        return analysisMapper.getGameType(platFormId, parentId, siteCode);
    }

    /***
     * 获取最后一条投注时间
     */
    public String getBetLastDate(String sitePrefix) throws Exception {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(sitePrefix))));
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        searchRequestBuilder.addSort(SortBuilders.fieldSort("betTime").order(SortOrder.DESC));
        searchRequestBuilder.setQuery(query)
                .setSize(1);
        Response response = connection.restClient_Read.performRequest("GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search", Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
//        log.info(searchRequestBuilder.toString());
        Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
        JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));
        for (Object obj : hits) {
            Map objmap = (Map) obj;
            objmap = JSON.parseObject(objmap.get("_source").toString());
            SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
            SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
            Date betLastDate = dateFormatSdf.parse(objmap.get("betTime").toString());
            String betLastDateStr = dateFormat.format(betLastDate);
            return betLastDateStr;
        }
        return null;
    }

    public static Map<String, Map> initGameTree(List<TGmGame> gmGames) {
        Map<String, Map> pfMap = new HashMap<>();
        gmGames.forEach(pf -> {
            pfMap.put(pf.getDepotName(), null);
        });

        pfMap.keySet().forEach(key -> {
            Map cate = new HashMap();
            gmGames.forEach(g -> {
                if (key.equals(g.getDepotName())) {
                    cate.put(g.getCatName(), null);
                }
            });
            pfMap.put(key, cate);
        });

        pfMap.entrySet().forEach(entry -> {
            Map cateMap = entry.getValue();
            cateMap.keySet().forEach(cate -> {
                List<TGmGame> games = new ArrayList<>();
                gmGames.forEach(g -> {
                    if (cate.equals(g.getCatName()) && entry.getKey().equals(g.getDepotName())) {
                        games.add(g);
                    }
                });
                cateMap.put(cate, games);
            });
            pfMap.put(entry.getKey(), cateMap);
        });
        return pfMap;
    }


    /***
     * 查询输赢
     * @return
     */
    public PageUtils findRptWinLostPage(WinLostReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getRptWinLostList(model);
        /**小计**/
        list.add(totalWinLost(list));
        list.add(analysisMapper.getRptWinLostTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    /***
     * 查询输赢，根据总代 代理 会员组进行分组
     * @return
     */
    public PageUtils findRptWinLostGroupPage(WinLostReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getRptWinLostGroup(model);
        /**小计**/
        list.add(totalWinLost(list));
        list.add(analysisMapper.getRptWinLostTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    /***
     * 查询输赢，根据 代理 进行分组
     * @return
     */
    public PageUtils findWinLostGroupAgentReportPage(WinLostReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getRptWinLostGroupAgent(model);
        /**小计**/
        list.add(totalWinLost(list));
        list.add(analysisMapper.getRptWinLostTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    /***
     * 查询输赢，根据会员 会员组进行分组
     * @return
     */
    public PageUtils findRptWinLostGroupUserPage(WinLostReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getRptWinLostGroupUser(model);
        /**小计**/
        list.add(totalWinLost(list));
        list.add(analysisMapper.getRptWinLostGroupUserTotal(model));
        return BeanUtil.toPagedResult(list);
    }


    public PageUtils findBonusReportPage(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getBonusReportList(model);
        list.add(totalWinLost(list));
        list.add(analysisMapper.getBonusReportListTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBonusGroupTopAgentReportPage(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getBonusGroupTopAgentReportList(model);
        list.add(totalWinLost(list));
        list.add(analysisMapper.getBonusReportListTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBonusGroupAgentReportPage(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getBonusGroupAgentReportList(model);
        list.add(totalWinLost(list));
        list.add(analysisMapper.getBonusReportListTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBonusGroupUserReportPage(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getBonusGroupUserReportList(model);
        list.add(totalWinLost(list));
        list.add(analysisMapper.getBonusReportListTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBonusGroupUserTotal(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getBonusGroupUserTotal(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBonusPage(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<TransactionModel> list = analysisMapper.getBonusList(model);
        list.add(totalBonus(list));
        list.add(analysisMapper.getBonusListTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findRptBetTotalPage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = analysisMapper.getRptBetTotalList(model);
        list.add(totalBetDay(list));    // 小计
        list.add(analysisMapper.getRptBetTotals(model)); // 总计
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findRptBetTotalList(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = analysisMapper.getRptBetTotalList(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBetDayGroupGameTypePage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = analysisMapper.getBetDayGroupGameTypeList(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBetDayGroupTopAgentPage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = analysisMapper.getBetDayGroupTopAgentList(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBetDayGroupAgentPage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = analysisMapper.getBetDayGroupAgentList(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBetDayGroupUserPage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = analysisMapper.getBetDayGroupUserList(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBetDayByAgentPage(GameReportModel model) {
        return BeanUtil.toPagedResult(Collections.singletonList(analysisMapper.getBetDayByAgentTotal(model)));
    }

    /***
     * 小计
     * @param list
     * @return
     */
    public RptWinLostModel totalWinLost(List<RptWinLostModel> list) {
        Integer betCounts = 0;
        Integer depositCounts = 0;
        Integer depositTimes = 0;
        Integer withdrawTimes = 0;
        Integer profitTimes = 0;
        Integer profitCounts = 0;
        BigDecimal deposits = BigDecimal.ZERO;
        BigDecimal withdraws = BigDecimal.ZERO;
        BigDecimal earnings = BigDecimal.ZERO;
        BigDecimal profits = BigDecimal.ZERO;
        RptWinLostModel twl = new RptWinLostModel();
        for (RptWinLostModel total : list) {
            betCounts += total.getBetCounts();
            depositCounts += total.getDepositCounts();
            depositTimes += total.getDepositTimes();
            withdrawTimes += total.getWithdrawTimes();
            profitTimes += total.getProfitTimes();
            deposits = deposits.add(total.getDeposits());
            withdraws = withdraws.add(total.getWithdraws());
            earnings = earnings.add(total.getEarnings());
            profits = profits.add(total.getProfits());
            profitCounts += total.getProfitCounts();
        }
        twl.setStartday("小计");
        twl.setBetCounts(betCounts);
        twl.setDepositCounts(depositCounts);
        twl.setDepositTimes(depositTimes);
        twl.setWithdrawTimes(withdrawTimes);
        twl.setProfitTimes(profitTimes);
        twl.setDeposits(deposits);
        twl.setWithdraws(withdraws);
        twl.setEarnings(earnings);
        twl.setProfits(profits);
        twl.setProfitCounts(profitCounts);
        return twl;
    }

    public RptBetTotalModel totalBetDay(List<RptBetTotalModel> list) {
        RptBetTotalModel rsObj = new RptBetTotalModel();
        Integer times = 0;
        /***总投注额***/
        BigDecimal betTotal = BigDecimal.ZERO;
        /***总有效投注额***/
        BigDecimal validBetTotal = BigDecimal.ZERO;
        /***总派彩额***/
        BigDecimal payoutTotal = BigDecimal.ZERO;
        /***累积投注***/
        BigDecimal jackpotBetTotal = BigDecimal.ZERO;
        /***累积派彩***/
        BigDecimal jackpotPayoutTotal = BigDecimal.ZERO;
        for (RptBetTotalModel bet : list) {
            times += bet.getTimes();
            betTotal = betTotal.add(bet.getBetTotal());
            validBetTotal = validBetTotal.add(bet.getValidBetTotal());
            payoutTotal = payoutTotal.add(bet.getPayoutTotal());
            jackpotBetTotal = jackpotBetTotal.add(bet.getJackpotBetTotal());
            jackpotPayoutTotal = jackpotPayoutTotal.add(bet.getJackpotPayoutTotal());
        }
        rsObj.setStartday("小计");
        rsObj.setTimes(times);
        rsObj.setBetTotal(betTotal);
        rsObj.setValidBetTotal(validBetTotal);
        rsObj.setPayoutTotal(payoutTotal);
        rsObj.setJackpotBetTotal(jackpotBetTotal);
        rsObj.setJackpotPayoutTotal(jackpotPayoutTotal);
        if (validBetTotal.compareTo(BigDecimal.ZERO) == 0) {
            rsObj.setWinRate(BigDecimal.ZERO.floatValue());
        } else {
            rsObj.setWinRate(payoutTotal.divide(validBetTotal, 4).multiply(new BigDecimal(100)).floatValue());
        }
        rsObj.setJackpotWinTotal(jackpotBetTotal.subtract(jackpotPayoutTotal));
        return rsObj;
    }

    public List<Map> getAgentAccount() {
        List<Map> list = analysisMapper.getAgentAccount();
        return list;
    }

    /**
     * 百分比= (now - old / old)*100
     *
     * @param now
     * @param old
     * @return
     */
    private BigDecimal getPercent(BigDecimal now, BigDecimal old) {
        BigDecimal hundred = new BigDecimal(100);
        return (old.compareTo(new BigDecimal("0")) > 0 ? now.subtract(old).divide(old) : now).multiply(hundred);
    }

    private String lessYear(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String str = "2017-12-07";
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(str));
        } catch (ParseException e) {
            return str;
        }
        calendar.add(Calendar.YEAR, -1);
        return sdf.format(calendar.getTime());
    }

    public List toLowerCase(List list) {
        List newList = new ArrayList();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            newList.add(String.valueOf(it.next()).toLowerCase());
        }
        return newList;
    }

    public List<RptMemberModel> getRptMemberList(String type, Integer limit) {
        //查询条数
        String formate = "%Y-%m-%d";
        if ("2".equals(type)) {   // 按月查询
            formate = "%Y-%m";
        }
        if (isNull(limit)) {  // 默认7天
            limit = Constants.EVNumber.seven;
        }
        List<RptMemberModel> list = analysisMapper.getRptMemberList(limit, formate, type);

        // 处理总会员字段：到统计日期为止的会员数
        List<RptMemberModel> rs = new ArrayList<>();
        list.stream().forEach(rm -> {
            if ("2".equals(type)) {   // 按月查询
                rm.setTotalMbrs(analysisMapper.getRegisterCountsByMonth(rm.getStartday()));
            } else {
                rm.setTotalMbrs(analysisMapper.getRegisterCounts(rm.getStartday()));
            }
            rs.add(rm);
        });
        return rs;
    }

    /**
     * 总览列表数据
     *
     * @param type      type: 1 按日展示：普通日期查询 /今日/7日/30日     2 按月展示 ： 近1年   3. 按年展示： 近10年
     * @param startTime yyyy-MM-dd
     * @param endTime
     * @return
     */
    public List<RptMemberModel> getRptMemberListEx(String type, String startTime, String endTime) {
        //查询条数
        String formate = "%Y-%m-%d";    // 默认按日
        if ("2".equals(type)) {         // 按月
            formate = "%Y-%m";
        }
        if ("3".equals(type)) {         // 按年
            formate = "%Y";
        }
        //List<RptMemberModel> list = analysisMapper.getRptMemberListEx(formate, startTime, endTime);
        List<RptMemberModel> list = homeMapper.getRptMemberListEx(formate, startTime, endTime);
        // 会员人数
        setRptMemberModel(list, formate, startTime, endTime);

        // 处理总会员字段：到统计日期为止的会员数
        List<RptMemberModel> rs = new ArrayList<>();
        list.stream().forEach(rm -> {
            if ("2".equals(type)) {   // 按月查询
                rm.setTotalMbrs(analysisMapper.getRegisterCountsByMonth(rm.getStartday()));
            } else {
                rm.setTotalMbrs(analysisMapper.getRegisterCounts(rm.getStartday()));
            }
            rs.add(rm);
        });
        return rs;
    }

    private void setRptMemberModel(List<RptMemberModel> rptMemberModels, String formate, String startTime, String endTime) {
        List<RptMemberModel> otherList = homeMapper.getRptMemberListExother(formate, startTime, endTime);
        for (RptMemberModel memberModel : rptMemberModels) {
            Optional<RptMemberModel> model = otherList.stream().filter(e ->
                    e.getStartday().equals(memberModel.getStartday())).findAny();
            if (model.isPresent()) {
                memberModel.setNewMbrs(model.get().getNewMbrs());
                memberModel.setNewDeposits(model.get().getNewDeposits());
                memberModel.setActiveMbrs(model.get().getActiveMbrs());
                memberModel.setWithdraws(model.get().getWithdraws());
                memberModel.setNewDepositAmount(model.get().getNewDepositAmount());
                memberModel.setNewWithdrawAmount(model.get().getNewWithdrawAmount());
                memberModel.setNewDepositOrderCount(model.get().getNewDepositOrderCount());
                memberModel.setNewWithdrawCount(model.get().getNewWithdrawCount());
                memberModel.setNewWithdrawOrderCount(model.get().getNewWithdrawOrderCount());
                memberModel.setWithdrawCount(model.get().getWithdrawCount());
                memberModel.setWithdrawOrderCount(model.get().getWithdrawOrderCount());
            }
        }
    }

    /***
     * 查询输赢，根据游戏类别
     * @return
     */
    public List<WinLostReport> findWinLostList(WinLostReport winLostReport) {
        List<WinLostReport> list = analysisMapper.findWinLostList(winLostReport);
        /**小计**/
        list.add(winLostTotal(list));
        return list;
    }

    /***
     * 查询输赢，根据总代
     * @return
     */
    public PageUtils findWinLostListOfTagency(WinLostReport winLostReport) {
        PageHelper.startPage(winLostReport.getPageNo(), winLostReport.getPageSize());
        List<WinLostReport> list = analysisMapper.findWinLostListOfTagency(winLostReport);
        /**小计**/
        list.add(winLostTotal(list));
        return BeanUtil.toPagedResult(list);
    }

    /***
     * 查询输赢，根据总代->代理
     * @return
     */
    public PageUtils findWinLostListByTagencyId(WinLostReport winLostReport) {
        PageHelper.startPage(winLostReport.getPageNo(), winLostReport.getPageSize());
        List<WinLostReport> list = analysisMapper.findWinLostListByTagencyId(winLostReport);
        /**小计**/
        list.add(winLostTotal(list));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findWinLostListByCagencyId(WinLostReport winLostReport) {
        PageHelper.startPage(winLostReport.getPageNo(), winLostReport.getPageSize());
        List<WinLostReport> list = analysisMapper.findWinLostListByCagencyId(winLostReport);
        /**小计**/
        list.add(winLostTotal(list));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findWinLostListByAccountId(WinLostEsQueryModel model) {
        List<RptBetModel> rptBetModels = new ArrayList<>();
        try {
            BoolQueryBuilder builder = setEsQuery(model);
            SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
            searchRequestBuilder.addSort(SortBuilders.fieldSort("betTime").order(SortOrder.DESC));
            searchRequestBuilder.setQuery(builder)
                    .setFrom((model.getPageNo() - 1) * model.getPageSize())
                    .setSize(model.getPageSize());
            Response response = connection.restClient_Read.performRequest("GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search", Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
//            log.info(builder.toString());
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));
            for (Object obj : hits) {
                Map objmap = (Map) obj;
                rptBetModels.add(JSON.parseObject(objmap.get("_source").toString(), RptBetModel.class));
            }
            List<WinLostReport> list = getWinLostReportList(rptBetModels);
//            log.info(builder.toString());
            /**小计**/
            WinLostReport winLostReport = winLostTotal(list);
            winLostReport.setLoginName(winLostReport.getLevel());
            list.add(winLostReport);
            //总记录数
            Long total = Long.parseLong(((Map) map.get("hits")).get("total") + "");
            PageUtils page = BeanUtil.toPagedResult(list);
            page.setTotalCount(total);
            //总页数
            page.setTotalPage(BigDecimalMath.ceil(total.intValue(), model.getPageSize()));
            //当前页数
            page.setCurrPage(model.getPageNo());
            return page;
        } catch (Exception e) {
            log.error("findWinLostListByAccountId", e);
            throw new RRException("查询异常!" + e.getMessage());
        }
    }

    public WinLostReport winLostTotal(List<WinLostReport> list) {
        WinLostReport winLostReport = new WinLostReport();
        Long total = 0L;
        BigDecimal betTotal = BigDecimal.ZERO;
        BigDecimal validbetTotal = BigDecimal.ZERO;
        BigDecimal payoutTotal = BigDecimal.ZERO;
        BigDecimal winLostRatio = BigDecimal.ZERO;
        for (WinLostReport w : list) {
            if (w.getTotal() != null && w.getTotal() != 0) {
                total += w.getTotal();
            }
            if (w.getBetTotal() != null && !w.getBetTotal().equals(BigDecimal.ZERO)) {
                betTotal = betTotal.add(w.getBetTotal());
            }
            if (w.getValidbetTotal() != null && !w.getValidbetTotal().equals(BigDecimal.ZERO)) {
                validbetTotal = validbetTotal.add(w.getValidbetTotal());
            }
            if (w.getPayoutTotal() != null && !w.getPayoutTotal().equals(BigDecimal.ZERO)) {
                payoutTotal = payoutTotal.add(w.getPayoutTotal());
            }
            if (w.getWinLostRatio() != null && !w.getWinLostRatio().equals(BigDecimal.ZERO)) {
                winLostRatio = winLostRatio.add(w.getWinLostRatio());
            }
        }
        winLostReport.setLevel("总计");
        winLostReport.setTotal(total);
        winLostReport.setBetTotal(betTotal);
        winLostReport.setValidbetTotal(validbetTotal);
        winLostReport.setPayoutTotal(payoutTotal);
        if (validbetTotal.stripTrailingZeros().equals(BigDecimal.ZERO) || payoutTotal.stripTrailingZeros().equals(BigDecimal.ZERO)) {
            winLostReport.setWinLostRatio(null);
        } else {
            //winLostReport.setWinLostRatio(payoutTotal.divide(validbetTotal, 2, BigDecimal.ROUND_HALF_DOWN).multiply(new BigDecimal("100")));
            winLostReport.setWinLostRatio(payoutTotal.divide(validbetTotal, 4, RoundingMode.DOWN).multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_DOWN));
        }
        winLostReport.setGameTimes(list.size() + "");
        return winLostReport;
    }

    /*public static void main(String[] str){
        WinLostReport winLostReport = new WinLostReport();
        BigDecimal payout = new BigDecimal(4672.72);
        BigDecimal validbet = new BigDecimal(5445.92);
        winLostReport.setWinLostRatio(payout.divide(validbet, 2).setScale(4, BigDecimal.ROUND_DOWN).multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_DOWN));
        //System.out.println(payout.divide(validbet, 2).setScale(4, BigDecimal.ROUND_DOWN).multiply(new BigDecimal("100")));
        System.out.println(winLostReport.getWinLostRatio());
    }*/

    public List<SelectModel> getDepot(String siteCode) {
        return analysisMapper.getDepot(siteCode);
    }

    public Integer getValidBetAccountCounts(WinLostReport winLostReport) {
        return analysisMapper.getValidBetAccountCounts(winLostReport);
    }

    public List<SelectModel> getGameCat(String depotId) {
        return analysisMapper.getGameCat(depotId);
    }

    public List<SelectModel> getSubGameCat(String depotId, String catId) {
        return analysisMapper.getSubGameCat(depotId, catId);
    }

    public List<WinLostReport> getWinLostReportList(List<RptBetModel> rptBetModels) {
        List<WinLostReport> winLostReports = new ArrayList<>();
        for (RptBetModel rptBetModel : rptBetModels) {
            WinLostReport winLostReport = new WinLostReport();
            winLostReport.setLoginName(rptBetModel.getUserName());
            winLostReport.setTagencyId(mbrMapper.getTagencyIdByName(rptBetModel.getUserName()));
            winLostReport.setDepositName(rptBetModel.getPlatform());
            winLostReport.setCatName(getGameCatName(rptBetModel.getPlatform(), rptBetModel.getGameType()));
            winLostReport.setGameName(rptBetModel.getGameName());
            winLostReport.setBetTotal(((rptBetModel.getBet() == null) ? BigDecimal.ZERO : rptBetModel.getBet()).setScale(2, BigDecimal.ROUND_HALF_UP));
            winLostReport.setValidbetTotal(((rptBetModel.getValidBet() == null) ? BigDecimal.ZERO : rptBetModel.getValidBet()).setScale(2, BigDecimal.ROUND_HALF_UP));
            winLostReport.setPayoutTotal(((rptBetModel.getPayout() == null) ? BigDecimal.ZERO : rptBetModel.getPayout()).setScale(2, BigDecimal.ROUND_HALF_UP));
            winLostReport.setGameTimes("1");
            winLostReport.setWinLostRatio((rptBetModel.getValidBet() != null && BigDecimal.ZERO.compareTo(rptBetModel.getValidBet()) != 0) ? (rptBetModel.getPayout().divide(rptBetModel.getValidBet(), 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100)) : BigDecimal.ZERO);
            winLostReports.add(winLostReport);
        }
        return winLostReports;
    }

    @Cacheable(cacheNames = ApiConstants.REDIS_WINLOST_CATCH, key = "#depotCode+'_'+#gameCode")
    public String getGameCatName(String depotCode, String gameCode) {
        return analysisMapper.getGameCatName(depotCode, gameCode);
    }

    private void setOpenResultDetail(List<RptBetModel> list) {
        for (RptBetModel model : list) {
            if (nonNull(model.getOpenResultDetail())){
                log.info("model结果{}", model);
                model.setOpenResultModel(JSON.parseObject(model.getOpenResultDetail(), OpenResultModel.class));
            }else {
                model.setOpenResultModel(null);
            }
        }
        /*list.forEach(e -> {
            if (nonNull(e.getOpenResultDetail())){
                e.setOpenResultModel(JSON.parseObject(e.getOpenResultDetail(), OpenResultModel.class));
            }else {
                e.setOpenResultModel(null);
            }
        });*/
    }

    /**
     * 	设置游戏平台code
     * 
     * @param list
     */
    public void depotCodeConverDepotNamme(List<RptBetModel> list) {
    	if (Collections3.isEmpty(list)) {
    		return;
    	}
    	
    	// 优先获取set_gm_game的DepotName
    	List<GameDepotNameDto> depotnameList = setGmGameMapper.selectSetDepotname();
		for (RptBetModel rptBetModel : list) {
			// 三方注单返回的平台code
			rptBetModel.setPlatformCode(rptBetModel.getPlatform());
			List<GameDepotNameDto> depotnameTargetList = depotnameList.stream()
					.filter(t -> String.valueOf(Constants.depotCatGameTypeMap.get(t.getCatcode()))
							.equals(rptBetModel.getCatid()) && t.getDepotcode().equals(rptBetModel.getPlatform()))
					.collect(Collectors.toList());
			GameDepotNameDto depotnameTarget = Collections3.isEmpty(depotnameTargetList) ? new GameDepotNameDto() : depotnameTargetList.get(0);
			if(depotnameTarget != null && StringUtil.isNotEmpty(depotnameTarget.getDepotname())) {
				rptBetModel.setPlatform(depotnameTarget.getDepotname());
			}
		}    	
    }

    public List<Map> getMbrTagencyIds(Map<String, Object> loginNameMap) {
        List<Map> map = mbrMapper.getTagencyIdByNames(loginNameMap);
        return map;
    }

    /**
     * 返回小计
     */
    public RptBetModel getSubtotal(List<RptBetModel> list) {
        RptBetModel subTotal = new RptBetModel();
        BigDecimal betTotal = BigDecimal.ZERO;
        BigDecimal validBetTotal = BigDecimal.ZERO;
        BigDecimal payoutTotal = BigDecimal.ZERO;
        for (RptBetModel r : list) {
            if (r.getBet() != null) {
                betTotal = betTotal.add(r.getBet());
            }
            if (r.getValidBet() != null) {
                validBetTotal = validBetTotal.add(r.getValidBet());
            }
            if (r.getPayout() != null) {
                payoutTotal = payoutTotal.add(r.getPayout());
            }
        }
        subTotal.setGameName("小计");
        subTotal.setBet(betTotal);
        subTotal.setValidBet(validBetTotal);
        subTotal.setPayout(payoutTotal);
        return subTotal;
    }

    /**
     * 返回总计
     */
    private RptBetModel getTotal(GameReportQueryModel model) {
        RptBetModel total = new RptBetModel();
        Map map = getRptBetListReport(model);
        total.setGameName("总计");
        total.setBet((BigDecimal) map.get("bet"));
        total.setValidBet((BigDecimal) map.get("validBet"));
        total.setPayout((BigDecimal) map.get("payout"));
        return total;
    }

    public SysFileExportRecord betDetailsExportExcel(GameReportQueryModel model, Long userId, String module) {
        // 处理异步下载记录
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecordEx(userId, module);

        if (Objects.nonNull(record) && "success".equals(record.getSaveFlag())) {
            // 异步查询数据并导出
            String siteCode = CommonUtil.getSiteCode();
            model.setSiteCode(siteCode);
            betDetailsExportExcelAsyn(model, userId, module, siteCode);
        }
        return record;
    }

    public void betDetailsExportExcelAsyn(GameReportQueryModel model, Long userId, String module, String siteCode) {

        CompletableFuture.runAsync(() -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            long start = Calendar.getInstance().getTimeInMillis();
            log.info("export==userId==" + userId + "==module==" + module + "==异步查询处理==start");
            Long startTime1 = System.currentTimeMillis();

            String key = RedisConstants.EXCEL_EXPORT + CommonUtil.getSiteCode() + module;
            List<Map<String, Object>> list = null;
            try {
                // 查询注单
                log.info("export==userId==" + userId + "==module==" + module + "==查询注单==start");
                Long startTime = System.currentTimeMillis();
                List<RptBetModel> bets = getRptBetList(model);
                log.info("export==userId==" + userId + "==module==" + module + "==查询注单==end==time==" + (System.currentTimeMillis() - startTime));
//                for (RptBetModel m: bets) {
//                    OpenResultModel  openResultModel= m.getOpenResultModel();
//                    if (nonNull(openResultModel)&&openResultModel.getResultMap().size()>0){
//                        String ret="";
//                        for (Map<String,String> retMap:openResultModel.getResultMap()) {
//                            ret = ret.concat(retMap.get("playOptionName")).concat(":").concat(retMap.get("playName")).concat(",");
//                        }
//                        m.setOpenResultDetail(ret);
//                    }
//                }

                SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sd.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                Long startfor = System.currentTimeMillis();
                list = bets.stream().map(e -> {
                    Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                    if ("3".equals(model.getGametype())) {
                        getZXResult(e, entityMap);
                    }

                    Date betTime = e.getBetTime();
                    Date payOutTime = e.getPayoutTime();
                    if (betTime != null) {
                        entityMap.put("betTime", sd.format(betTime));
                    }
                    if (payOutTime != null) {
                        entityMap.put("payoutTime", sd.format(payOutTime));
                    }

                    OpenResultModel  openResultModel= e.getOpenResultModel();
                    if (nonNull(openResultModel)){
                        String ret="";
                        for (Map<String,String> retMap:openResultModel.getResultMap()) {
                            ret = ret.concat(retMap.get("playOptionName")).concat(":").concat(retMap.get("playName")).concat(",");
                        }
                        // e.setOpenResultDetail(ret);
                        entityMap.put("openResultDetail",ret);
                    }
                    return entityMap;
                }).collect(Collectors.toList());
                Long endfor = System.currentTimeMillis();
                long k = endfor-startfor;
                log.info("第1个双循环耗时k:"+k);
                // 此处显示清理
                bets.clear();
            } catch (R200Exception e) {
                redisService.del(key);
                log.info("export==userId==" + userId + "==module==" + module + "==error==" + e);
            } catch (Exception e) {
                redisService.del(key);
                log.info("export==userId==" + userId + "==module==" + module + "==error==" + e);
            }
            if (CollectionUtils.isEmpty(list)) {
                redisService.del(key);
                log.info("export==userId==" + userId + "==module==" + module + "==error==没有可导出的数据!");
            }
            long end = Calendar.getInstance().getTimeInMillis();
            long r = end-start;
            log.info("查询数据耗时:"+r);

            long startt = Calendar.getInstance().getTimeInMillis();
            // 同步执行生成excel+上传文件:
            log.info("export==userId==" + userId + "==module==" + module + "==同步执行上传文件==start==list.size==" + list.size());
            Long startTime2 = System.currentTimeMillis();
            try {
                sysFileExportRecordService.exportExcelSyn(userId, list, module, siteCode);
                log.info("export==userId==" + userId + "==module==" + module + "==同步执行上传文件==end==time==" + (System.currentTimeMillis() - startTime2));
            } catch (Exception e) {
                redisService.del(key);
                log.info("export==userId==" + userId + "==module==" + module + "==error==同步执行上传文件异常!");
            }
            log.info("export==userId==" + userId + "==module==" + module + "==异步查询处理==end==time==" + (System.currentTimeMillis() - startTime1));
            long endd = Calendar.getInstance().getTimeInMillis();
            long rr = endd-startt;
            log.info("写数据耗时:"+rr);
        });
    }

    private void getZXResult(RptBetModel model, Map<String, Object> entityMap) {
        if (model.getOpenResultModel() != null) {
            List<Map<String, String>> resultMaps = model.getOpenResultModel().getResultMap();
            if (resultMaps != null && resultMaps.size() > 0) {
                Map<String, String> resultMap = resultMaps.get(0);
                entityMap.put("xResult", resultMap.get("xResult"));
                entityMap.put("zResult", resultMap.get("zResult"));
            }
        }
    }

    public SysFileExportRecord exportBonus(BounsReportQueryModel model, SysUserEntity user, String mbrBonusExcelTempPath, String module) {
        // 生成文件导出记录
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(user.getUserId(), module);
        if (null != record) {
            List<TransactionModel> bonusList = analysisMapper.getBonusList(model);
//            if (bonusList.size() > 10000) {
//                throw new R200Exception("导出数量超过1W条，请更新搜索条件后再进行导出！");
//            }
            for (TransactionModel transactionModel : bonusList) {
				if(transactionModel.getIsOnline() != null) {
					if(transactionModel.getIsOnline()) {
						transactionModel.setIsOnlineStr("线上活动");
					} else {
						transactionModel.setIsOnlineStr("线下活动");
					}
				}
				if (StringUtil.isNotEmpty(transactionModel.getSource())) {
					if("0".equals(transactionModel.getSource())) {
						transactionModel.setSource("前台申请");
					} else if ("1".equals(transactionModel.getSource())){
						transactionModel.setSource("后台添加");
					} else if ("2".equals(transactionModel.getSource())){
						transactionModel.setSource("人工增加");
					} else if ("3".equals(transactionModel.getSource())){
						transactionModel.setSource("人工减少");
					}
				}
			}
            List<Map<String, Object>> list = bonusList.stream().map(e -> {
                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(mbrBonusExcelTempPath, list, user.getUserId(), module, siteCode);
        }
        return record;
    }

    /***
     * 小计
     * @param list
     * @return
     */
    public TransactionModel totalBonus(List<TransactionModel> list) {
        TransactionModel transactionModel = new TransactionModel();
        BigDecimal bonusAmount = BigDecimal.ZERO;   // 红利金额

        for (TransactionModel total : list) {
            bonusAmount = bonusAmount.add(total.getBonusAmount());
        }
        transactionModel.setUserName("小计");
        transactionModel.setBonusAmount(bonusAmount);
        return transactionModel;
    }
}