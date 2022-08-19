package com.wsdy.saasops.modules.analysis.service;

import static java.util.Objects.nonNull;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.wsdy.saasops.api.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.wsdy.saasops.ElasticSearchConnection;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.analysis.entity.OpenResultModel;
import com.wsdy.saasops.modules.analysis.mapper.AnalysisMapper;
import com.wsdy.saasops.modules.base.entity.GmDepot;
import com.wsdy.saasops.modules.operate.dao.SetGmGameMapper;
import com.wsdy.saasops.modules.operate.dao.TGameLogoMapper;
import com.wsdy.saasops.modules.operate.entity.SetGmGame;
import com.wsdy.saasops.modules.operate.entity.TGameLogo;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExportBetDetailsService {

    @Autowired
    private ElasticSearchConnection_Read connection;

    @Autowired
    private ElasticSearchConnection searchConnection;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AnalysisMapper analysisMapper;

    @Autowired
    private SetGmGameMapper setGmGameMapper;

    @Autowired
    private TGameLogoMapper gameLogoMapper;

    @Autowired
    private JsonUtil jsonUtil ;


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
            String key = RedisConstants.EXCEL_EXPORT + CommonUtil.getSiteCode() + module;
            try {
                long startTime = System.currentTimeMillis();
                List<Map<String, Object>> list = getRptBetList(model);
                long endTime1 = System.currentTimeMillis();
                log.info("betDetailsExportExcelAsyn 查询数据记录耗时 " + (endTime1 - startTime) / 1000 + "秒,数据总数" + list.size());
                sysFileExportRecordService.exportExcelSynNew(userId, getExcelStream(list), module, siteCode);
                long endTime = System.currentTimeMillis();
                log.info("betDetailsExportExcelAsyn 总耗时 " + (endTime - startTime) / 1000 + "秒");
            } catch (Exception e) {
                log.error("betDetailsExportExcel error", e);
            } finally {
                redisService.del(key);
            }
        });
    }

    private byte[] getExcelStream(List<Map<String, Object>> rowList) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        writer.renameSheet("投注记录");
        writer.addHeaderAlias("userName", "会员名");
        writer.addHeaderAlias("id", "注单号");
        writer.addHeaderAlias("betTime", "投注时间");
        writer.addHeaderAlias("platform", "游戏平台");
        writer.addHeaderAlias("bet", "投注金额");
        writer.addHeaderAlias("validBet", "有效投注");
        writer.addHeaderAlias("payout", "盈亏");
        writer.addHeaderAlias("odds", "赔率");
        writer.addHeaderAlias("oddsType", "赔率类型");
        writer.addHeaderAlias("gameName", "游戏名称");
        writer.addHeaderAlias("playType", "玩法类型");
        writer.addHeaderAlias("leagueName", "联赛名称");
        writer.addHeaderAlias("team", "比赛队伍");
        writer.addHeaderAlias("betScore", "下注时比分");
        writer.addHeaderAlias("resultOpen", "开奖结果");
        writer.addHeaderAlias("status", "结算状态");
        writer.addHeaderAlias("payoutTime", "派彩时间");
        writer.addHeaderAlias("result", "输赢");
        writer.addHeaderAlias("openResultDetail", "投注详情");


        writer.write(rowList, true);
        writer.setOnlyAlias(true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.flush(baos);
        writer.close();
        byte[] dataByte = baos.toByteArray();
        return dataByte;
    }

    private void queryScrollRptBet(String scrollId, List<Map<String, Object>> list) {
        if (StringUtils.isNotEmpty(scrollId)) {
            try {
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
                    log.info("newexport==查询注单==初始化游标==end==size==" + hits.size());
                    for (Object obj : hits) {
                        Map objmap = (Map) obj;
                        Map<String, Object> dto = JSON.parseObject(objmap.get("_source").toString());
                        setMapRptBetModel(dto);
                        list.add(dto);
                    }
                }
            } catch (Exception e) {
                log.error("查询游标出错queryScrollRptBet", e);
            }
        }
    }

    /***
     * 查询所有注单
     * @return
     */
    public List<Map<String, Object>> getRptBetList(GameReportQueryModel model) {
        List<Map<String, Object>> list = Lists.newArrayList();
        String scrollId = StringUtils.EMPTY;
        Map map = null;
        try {
            // 1.设置查询条件query
            BoolQueryBuilder builder = analysisService.setEsQuery(model);
            // 2. 设置查询builder
            SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
            searchRequestBuilder.addSort(SortBuilders.fieldSort("betTime").order(SortOrder.DESC));
            searchRequestBuilder.setQuery(builder)
                    .setFrom(0)
                    .setSize(10000);

            Response response = connection.restClient_Read.performRequest(
                    "GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE
                            + "/_search?scroll=15m",
                    Collections.singletonMap("_source", "true"),
                    new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON)
            );
            // 查询结果处理
            map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            scrollId = String.valueOf(map.get("_scroll_id"));                // 游标_scroll_id
        } catch (Exception e) {
            log.error("查询出错", e);
        }
        //总记录数
        Long total = Long.parseLong(((Map) map.get("hits")).get("total") + "");
        if (total > 250000L) {
            throw new R200Exception("导出数量超过25W条，请更新搜索条件后再进行导出！");
        }
        JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));   // 命中数据

        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sd.setTimeZone(TimeZone.getTimeZone("GMT+0"));

        for (Object obj : hits) {
            Map objmap = (Map) obj;
            Map<String, Object> rptBetModel = JSON.parseObject(objmap.get("_source").toString());
            setMapRptBetModel(rptBetModel);
            list.add(rptBetModel);
        }
        // 游标查询剩余
        queryScrollRptBet(scrollId, list);
        this.depotCodeConverDepotNamme(list);
        return list;
    }

    public void depotCodeConverDepotNamme(List<Map<String, Object>> list) {
        if (Collections3.isEmpty(list)) {
            return;
        }
        // 获取所有的depotCodes
        Set<String> depotCodes = list.stream().map(t -> String.valueOf(t.get("platform"))).collect(Collectors.toSet());

        // 根据depotCodes查询获得总的depotNameList
        List<GmDepot> depotNameList = new ArrayList<>();
        if (depotCodes.size() != 0) {
            Map<String, Object> depotCodeMap = new HashMap<>();
            depotCodeMap.put("depotCodes", depotCodes);
            depotNameList = analysisMapper.getAllDepotCodeToDepotName(depotCodeMap);
            log.error("==ExportBetDetailsService== depotNameList={}", jsonUtil.toJson(depotNameList));
        }

        // 获取set_gm_game表的数据
        Set<Integer> depotIds = depotNameList.stream().map(GmDepot::getId).collect(Collectors.toSet());
        log.error("==ExportBetDetailsService== depotIds={}", jsonUtil.toJson(depotIds));
        List<SetGmGame> setGmGameList = setGmGameMapper.selectByGmDepotIds(depotIds);
        log.error("==ExportBetDetailsService== setGmGameList={}", jsonUtil.toJson(setGmGameList));
        // 获取set_gm_game.gamelogoid 对应的 t_game_logo数据
        Set<Integer> gameLogoIds = setGmGameList.stream().map(SetGmGame::getGameLogoId).collect(Collectors.toSet());
        log.error("==ExportBetDetailsService== gameLogoIds={}", jsonUtil.toJson(gameLogoIds));

        List<TGameLogo> gameLogoList = new ArrayList<>();
        if(gameLogoIds.size() > 0){
            gameLogoList = gameLogoMapper.selectByIdList(gameLogoIds);
        }


        // 比对获取
        for (Map<String, Object> rptBetModel : list) {
            for (GmDepot obj : depotNameList) {
                // 对比key：depotName 是否一致
                if (rptBetModel.get("platform").equals(obj.getDepotCode())) {
                    String thiscatid = String.valueOf(Constants.depotCatGameTypeMap.get(rptBetModel.get("gameCategory")));
                    List<TGameLogo> gameLogoCollect = gameLogoList.stream().filter(t -> String.valueOf(t.getCatId()).equals(thiscatid)).collect(Collectors.toList());
                    TGameLogo gameLogo =  Collections3.isEmpty(gameLogoCollect) ? new TGameLogo() : gameLogoCollect.get(0);

                    // 优先获取set_gm_game的DepotName
                    List<SetGmGame> collect = setGmGameList.stream().filter(t -> t.getDepotId().equals(obj.getId())
                            && t.getGameLogoId().equals(gameLogo.getId())).collect(Collectors.toList());
                    SetGmGame targetSetGmGame = Collections3.isEmpty(collect) ? null : collect.get(0);
                    if (targetSetGmGame != null && StringUtil.isNotEmpty(targetSetGmGame.getDepotName())) {
                        rptBetModel.put("platform", targetSetGmGame.getDepotName());
                    } else {
                        rptBetModel.put("platform", obj.getDepotName());
                    }
                }
            }
        }
    }

    private void setMapRptBetModel(Map<String, Object> rptBetModel) {
        if (nonNull(rptBetModel.get("openResultDetail"))) {
            OpenResultModel openResultModel = JSON.parseObject(rptBetModel.get("openResultDetail").toString(), OpenResultModel.class);
            if (nonNull(openResultModel)) {
                String ret = "";
                for (Map<String, String> retMap : openResultModel.getResultMap()) {
                    if (StringUtils.isNotEmpty(retMap.get("playOptionName")) &&
                            StringUtils.isNotEmpty(retMap.get("playName"))) {
                        ret = ret.concat(retMap.get("playOptionName")).concat(":").concat(retMap.get("playName"));
                    }
                    if (StringUtils.isNotEmpty(retMap.get("scoTypeName")) &&
                    		StringUtils.isNotEmpty(retMap.get("score"))) {
                    	ret = ret.concat(retMap.get("scoTypeName")).concat(":").concat(retMap.get("score"));
                    }
                    if (StringUtils.isNotEmpty(retMap.get("betText")) &&
                    		StringUtils.isNotEmpty(retMap.get("playType"))) {
                    	ret = ret.concat(retMap.get("betText")).concat(":").concat(retMap.get("playType"));
                    } else if (StringUtils.isNotEmpty(retMap.get("betText"))) {
                    	ret = ret.concat(retMap.get("betText"));
                    }
                    
                }
                rptBetModel.put("openResultDetail", ret);
            }
        }
        if (rptBetModel.get("betTime") != null) {
            String time = DateUtil.formatEsDateToTime(rptBetModel.get("betTime").toString());
            rptBetModel.put("betTime", time);
        }
        if (rptBetModel.get("payoutTime") != null) {
            String time = DateUtil.formatEsDateToTime(rptBetModel.get("payoutTime").toString());
            rptBetModel.put("payoutTime", time);
        }
        removeMapKey(rptBetModel);
    }

    private void removeMapKey(Map<String, Object> entityMap) {
        entityMap.remove("agyAccount");
        entityMap.remove("apiPrefix");
        entityMap.remove("balanceAfter");
        entityMap.remove("balanceBefore");
        entityMap.remove("betType");
        entityMap.remove("currency");
        entityMap.remove("downloadTime");
        entityMap.remove("egDetails");
//        entityMap.remove("gameCategory");
        entityMap.remove("gameType");
        entityMap.remove("jackpotBet");
        entityMap.remove("jackpotPayout");
        entityMap.remove("openResultModel");
        entityMap.remove("orderDate");
        entityMap.remove("origin");
        entityMap.remove("roundNo");
        entityMap.remove("serialId");
        entityMap.remove("sitePrefix");
        entityMap.remove("startTime");
        entityMap.remove("tableNo");
        entityMap.remove("tagencyId");
        entityMap.remove("tip");
        entityMap.remove("website");
        entityMap.remove("xOpenResult");
        entityMap.remove("zOpenResult");
        entityMap.remove("originalValidBet");
        entityMap.remove("poundageRates");
        entityMap.remove("poundage");
        entityMap.remove("type");
        entityMap.remove("gameCategory");
        entityMap.remove("gameStartTime");
        entityMap.remove("gameStartTime");
        entityMap.remove("billCount");
        entityMap.remove("reloss");
        entityMap.remove("openResult");
        entityMap.remove("estimatedPayout");
        entityMap.remove("resultScore");
    }
}