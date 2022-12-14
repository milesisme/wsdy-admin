package com.wsdy.saasops.modules.analysis.service;


import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.api.utils.GameTypeEnum;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.analysis.dto.*;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.analysis.entity.RptBetModel;
import com.wsdy.saasops.modules.analysis.mapper.AnalysisMapper;
import com.wsdy.saasops.modules.analysis.vo.RetentionRateDailyActiveReportVo;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.lucene.util.CollectionUtil;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RetentionRateService {

    @Autowired
    private AnalysisMapper analysisMapper;

    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    @Autowired
    private AnalysisService analysisService;

    /**
     * ???????????????service
     */
    @Resource
    private RptBetRcdDayService rptBetRcdDayService;

    @Resource
    private ThreadPoolExecutor retentionRateDailyActiveExecutor;

    public PageUtils list(RetentionRateDto retentionRateDto) {
        String startTime = retentionRateDto.getStartTime();
        String endTime = retentionRateDto.getEndTime();
        Integer pageNo = retentionRateDto.getPageNo();
        Integer pageSize = retentionRateDto.getPageSize();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        PageUtils pageUtils = new PageUtils();
        try {
            Date date = simpleDateFormat.parse(endTime);
            Calendar et = Calendar.getInstance();
            et.setTime(date);
            et.add(Calendar.DATE, -(pageNo - 1) * pageSize);

            et.set(Calendar.HOUR_OF_DAY, 23);
            et.set(Calendar.MINUTE, 59);
            et.set(Calendar.SECOND, 59);

            Calendar st = Calendar.getInstance();
            st.setTime(et.getTime());
            st.add(Calendar.DATE, -pageSize + 1);
            st.set(Calendar.HOUR_OF_DAY, 0);
            st.set(Calendar.MINUTE, 0);
            st.set(Calendar.SECOND, 0);

            Calendar st2 = Calendar.getInstance();
            st2.setTime(simpleDateFormat.parse(startTime));

            if (st.before(st2)) {
                st = st2;
            }

            List<RetentionRateListResultDto> resultDtos = getRetentionRateListResultDtos(retentionRateDto, simpleDateFormat, st, et);

            pageUtils.setCurrPage(pageNo);
            pageUtils.setPageSize(pageSize);
            Date startDate = simpleDateFormat.parse(startTime);
            Date endDate = simpleDateFormat.parse(endTime);
            int days = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 3600 * 24)) + 1;
            pageUtils.setTotalCount(days);
            pageUtils.setTotalPage((days / pageSize) + 1);
            pageUtils.setList(resultDtos);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return pageUtils;
    }

    private List<RetentionRateListResultDto> getRetentionRateListResultDtos(RetentionRateDto retentionRateDto, SimpleDateFormat simpleDateFormat, Calendar st, Calendar et) {
        // ????????????
        Map<String, RetentionRateListResultDto> retentionRateListResultDtoMap = getRetentionRateListResultDto(DateUtil.format(st.getTime(), DateUtil.FORMAT_18_DATE_TIME), DateUtil.format(et.getTime(), DateUtil.FORMAT_18_DATE_TIME), retentionRateDto.getUserName());
        //????????????
        List<RetentionRateListResultDto> resultDtos = new ArrayList<>();

        // ????????????
        while (!st.after(et)) {
            String dt = DateUtil.format(st.getTime(), DateUtil.FORMAT_10_DATE);
            RetentionRateListResultDto retentionRateListResultDto = retentionRateListResultDtoMap.get(dt);

            // ????????????

            Calendar st2 = Calendar.getInstance();
            st2.setTime(st.getTime());

            Calendar et2 = Calendar.getInstance();
            et2.setTime(st.getTime());
            et2.add(Calendar.DATE, 30);

            Calendar ct = Calendar.getInstance();
            if (et2.after(ct)) {
                et2 = ct;
            }

            et2.set(Calendar.HOUR_OF_DAY, 23);
            et2.set(Calendar.MINUTE, 59);
            et2.set(Calendar.SECOND, 59);

            if (retentionRateListResultDto == null) {
                List<RetentionRateResultDto> list = new ArrayList<>();
                retentionRateListResultDto = new RetentionRateListResultDto();
                retentionRateListResultDto.setTime(dt);
                retentionRateListResultDto.setFirstChargeTimeNum(0);
                retentionRateListResultDto.setRetentionRateResultDtoList(list);
                resultDtos.add(retentionRateListResultDto);
                st.add(Calendar.DATE, 1);

                // ?????????
                while (st2.before(et2)) {
                    String dt2 = DateUtil.format(st2.getTime(), DateUtil.FORMAT_10_DATE);
                    RetentionRateResultDto retentionRateResultDto = new RetentionRateResultDto();
                    retentionRateResultDto.setNum(0);
                    retentionRateResultDto.setDate(dt2);
                    list.add(retentionRateResultDto);
                    st2.add(Calendar.DATE, 1);
                }
                continue;
            } else {
                resultDtos.add(retentionRateListResultDto);
            }

            String startPageTime = simpleDateFormat.format(st2.getTime());
            String endPageTime = simpleDateFormat.format(et2.getTime());
            List<RetentionRateResultDto> retentionRateResultDtos = analysisMapper.getRetentionRateResult(startPageTime, endPageTime, retentionRateListResultDto.getRetentionRateAccountIdList());
            List<RetentionRateResultDto> list = new ArrayList<>();
            retentionRateListResultDto.setRetentionRateResultDtoList(list);
            Map<String, RetentionRateResultDto> RetentionRateResultDtoMap = retentionRateResultDtos.stream().collect(Collectors.toMap(RetentionRateResultDto::getDate, a -> a, (k1, k2) -> k1));
            while (st2.before(et2)) {
                String dt2 = DateUtil.format(st2.getTime(), DateUtil.FORMAT_10_DATE);
                RetentionRateResultDto retentionRateResultDto = RetentionRateResultDtoMap.get(dt2);
                if (retentionRateResultDto == null) {
                    retentionRateResultDto = new RetentionRateResultDto();
                    retentionRateResultDto.setNum(0);
                    retentionRateResultDto.setDate(dt2);
                }
                list.add(retentionRateResultDto);
                st2.add(Calendar.DATE, 1);
            }
            st.add(Calendar.DATE, 1);
        }
        Collections.reverse(resultDtos);
        return resultDtos;
    }


    private Map<String, RetentionRateListResultDto> getRetentionRateListResultDto(String startTime, String endTime, String agentName) {
        List<RetentionRatePlayerDto> retentionRatePlayerDtoList = analysisMapper.getRetentionRatePlayer(startTime, endTime, agentName);
        Map<String, RetentionRateListResultDto> map = new HashMap();
        for (RetentionRatePlayerDto retentionRatePlayerDto : retentionRatePlayerDtoList) {
            RetentionRateListResultDto retentionRateListResultDto = map.get(retentionRatePlayerDto.getFirstChargeDate());
            if (retentionRateListResultDto == null) {
                retentionRateListResultDto = new RetentionRateListResultDto();
                retentionRateListResultDto.setTime(retentionRatePlayerDto.getFirstChargeDate());
                retentionRateListResultDto.setRetentionRateAccountIdList(new ArrayList<>());
                retentionRateListResultDto.setFirstChargeTimeNum(0);
                map.put(retentionRatePlayerDto.getFirstChargeDate(), retentionRateListResultDto);
            }
            retentionRateListResultDto.setFirstChargeTimeNum(retentionRateListResultDto.getFirstChargeTimeNum() + 1);
            retentionRateListResultDto.getRetentionRateAccountIdList().add(retentionRatePlayerDto.getAccountId());
        }
        return map;
    }


    public SysFileExportRecord exportRetentionRateReport(RetentionRateDto retentionRateDto, Long userId, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String siteCode = CommonUtil.getSiteCode();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = retentionRateDto.getStartTime();
            String endTime = retentionRateDto.getEndTime();
            try {
                Date startDate = simpleDateFormat.parse(startTime);
                Calendar st = Calendar.getInstance();
                st.setTime(startDate);
                Calendar et = Calendar.getInstance();
                Date endDate = simpleDateFormat.parse(endTime);
                et.setTime(endDate);
                List<RetentionRateListResultDto> resultDtos = getRetentionRateListResultDtos(retentionRateDto, simpleDateFormat, st, et);

                ExportParams exportParams = new ExportParams();
                Map<String, Object> map = new HashMap<>();
                List<ExportRetentionRateDto> data = new ArrayList<>();
                for (RetentionRateListResultDto retentionRateListResultDto : resultDtos) {
                    ExportRetentionRateDto exportRetentionRateDto = new ExportRetentionRateDto();
                    exportRetentionRateDto.setTime(retentionRateListResultDto.getTime());
                    exportRetentionRateDto.setFirstChargeTimeNum(retentionRateListResultDto.getFirstChargeTimeNum());
                    List<RetentionRateResultDto> retentionRateResultDtoList = retentionRateListResultDto.getRetentionRateResultDtoList();
                    for (int i = 1; i <= 30; i++) {
                        String value = "";
                        if (retentionRateResultDtoList != null && i <= retentionRateResultDtoList.size()) {
                            value = retentionRateResultDtoList.get(i - 1).getNum().toString();
                        }

                        Method method = ExportRetentionRateDto.class.getDeclaredMethod("setD" + i, String.class);
                        method.invoke(exportRetentionRateDto, value);
                    }

                    data.add(exportRetentionRateDto);

                }
                map.put("data", data);
                map.put("entity", ExportRetentionRateDto.class);
                map.put("title", exportParams);

                List<Map<String, Object>> sheetsList = new ArrayList<>();
                sheetsList.add(map);
                sysFileExportRecordService.exportMilSheet(sheetsList, userId, module, siteCode);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return record;
    }

    /**
     * ????????????????????????????????????30??????????????????
     * @param retentionRateDto
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public PageUtils retentionRateDailyActiveReport(RetentionRateDailyActiveDto retentionRateDto) throws ExecutionException, InterruptedException {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        String startTime = retentionRateDto.getStartTime();
        String endTime = retentionRateDto.getEndTime();
        String userName = retentionRateDto.getUserName();
        String agentName = retentionRateDto.getAgentName();
        /*
         * ????????????:
         * ???????????????????????????1??????????????????2????????????????????????????????????
         * 1.?????????????????????99??????=1??????????????????
         * 2.?????????????????????99??????=1??????????????????
         * 3.?????????????????????????????????99??????=1???????????????
         */
        Integer rule = retentionRateDto.getRule() == null ? 3: retentionRateDto.getRule();
        Integer pageNo = retentionRateDto.getPageNo() == null ? 1 : retentionRateDto.getPageNo();
        Integer pageSize = retentionRateDto.getPageSize() == null ? 999999 : retentionRateDto.getPageSize();
        PageUtils pageUtils = new PageUtils();
        PageHelper.startPage(pageNo, pageSize);

        // ??????????????????
        List<RetentionRatePlayerDto> retentionRatePlayerDtoList = analysisMapper.getRetentionRateDailyActiveReport(startTime, endTime, userName, agentName);

        pageUtils = BeanUtil.toPagedResult(retentionRatePlayerDtoList);
        if (CollectionUtils.isEmpty(retentionRatePlayerDtoList)) {
            return pageUtils;
        }
        // ?????????????????????????????????????????????
        Map<String, List<RetentionRatePlayerDto>> groupMap = retentionRatePlayerDtoList.stream().collect(Collectors.groupingBy(RetentionRatePlayerDto::getFirstChargeDate));

        List<RetentionRateDailyActiveReportVo> resultList = new ArrayList<>();
        List<CompletableFuture> asynTaskList = new ArrayList<>();

        // ??????????????????
        String siteCode = CommonUtil.getSiteCode();

        // ??????????????????????????????????????????30??????????????????????????????
        groupMap.forEach((firstChargeDate,list)->{
            CompletableFuture<Void> async = CompletableFuture.runAsync(() -> {
                RequestContextHolder.setRequestAttributes(requestAttributes);
                caculateUserDailyActiveData(rule, resultList, list, firstChargeDate, siteCode);
            }, retentionRateDailyActiveExecutor);
            asynTaskList.add(async);
        });
        // ???????????????????????????
        CompletableFuture.allOf(asynTaskList.toArray(new CompletableFuture[asynTaskList.size()])).get();

        List<RetentionRateDailyActiveReportVo> sortedList = resultList.stream().sorted(Comparator.comparing(r -> {
            String fcd = r.getFirstChargeDate();
            LocalDate firstDate = LocalDate.parse(fcd, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return firstDate;
        }, Comparator.reverseOrder())).collect(Collectors.toList());
        // ??????????????????list????????????pageHelper???
        pageUtils.setList(sortedList);

        return pageUtils;
    }



    /**
     * ?????????????????????????????????
     * @param rule
     * @param resultList
     * @param list
     * @param firstChargeDate
     *
     */
    private void caculateUserDailyActiveData(Integer rule, List<RetentionRateDailyActiveReportVo> resultList,
                                             List<RetentionRatePlayerDto> list, String firstChargeDate, String siteCode) {
        // ???????????????
        List<String> userNames = list.stream().map(RetentionRatePlayerDto::getLoginName).collect(Collectors.toList());
        // ??????????????????????????????????????????30?????????????????????????????????
        LocalDate sp = LocalDate.parse(firstChargeDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime startTmp = LocalDateTime.of(sp, LocalTime.MIN);
        LocalDateTime endTmp = LocalDateTime.of(sp, LocalTime.MAX);

        // ????????????30?????????????????????????????????????????????????????????
        LocalDateTime start = startTmp.plusDays(0);
        LocalDateTime end = endTmp.plusDays(29);
        String startStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // ????????????30?????????????????????????????????
        List<DepositOrBetDailyDto> depositDailyList = analysisMapper.getUserDailyDepositAmount(startStr, endStr, userNames);
        // ????????????30?????????????????????????????????
        // List<DepositOrBetDailyDto> validBetDailyList = analysisMapper.getUserDailyBetAmount(startStr, endStr, userNames);
        // List<DepositOrBetDailyDto> validBetDailyList = getUserDailyBetAmount(startStr, endStr, userNames, siteCode);
        List<DepositOrBetDailyDto> validBetDailyList = getUserDailyBetAmountFromES(startStr, endStr, userNames, siteCode);
        // ??????
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // ?????????????????????????????????????????????????????????
        userNames.forEach(uName -> {
            RetentionRateDailyActiveReportVo vo = new RetentionRateDailyActiveReportVo();
            vo.setUserName(uName);
            vo.setFirstChargeDate(startTmp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            // ?????????????????????????????????
            List<RetentionRateDailyActiveReportVo.ActiveStateIn30Days> stateList = new ArrayList<>();
            Long dayStep = 0l;
            LocalDateTime tempDT;
            // ????????????????????????
            LocalDateTime startDT = startTmp.plusDays(0);
            do {
                RetentionRateDailyActiveReportVo.ActiveStateIn30Days state = new RetentionRateDailyActiveReportVo.ActiveStateIn30Days();
                state.setDay("day" + (dayStep + 1));
                tempDT = startDT.plusDays(dayStep);
                Integer active = null;

                // ?????????????????????????????????null??????
                if (tempDT.compareTo(todayStart) <= 0) {
                    LocalDateTime finalTempDT = tempDT;
                    // ??????????????????????????????????????????????????????
                    Optional<DepositOrBetDailyDto> depositAny = findDailyByUserNameAndDate(depositDailyList, uName, finalTempDT);
                    // ??????????????????????????????????????????????????????
                    Optional<DepositOrBetDailyDto> validBetAny = findDailyByUserNameAndDate(validBetDailyList, uName, finalTempDT);
                    // ???????????????????????????????????????????????????????????????1??????????????????2??????????????????3????????????
                    active = getActive(rule, depositAny, validBetAny, dayStep.intValue());
                }

                state.setState(active);
                stateList.add(state);
                dayStep++;
            }while (tempDT.compareTo(end) <= 0);

            vo.setStateList(stateList);

            resultList.add(vo);
        });
    }

    @Autowired
    private ElasticSearchConnection_Read connection;

    private List<DepositOrBetDailyDto> getUserDailyBetAmountFromES(String startStr, String endStr, List<String> userNames, String siteCode) {
        // ??????????????????
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        List<String> apiPrefixBySiteCode = analysisMapper.getApiPrefixBySiteCode(siteCode);
        List<String> prefixList = apiPrefixBySiteCode.stream().map(s -> String.valueOf(s).toLowerCase()).collect(Collectors.toList());

        List<QueryBuilder> conditionList = new ArrayList<>();
        conditionList.add(QueryBuilders.termsQuery("sitePrefix", prefixList));
        conditionList.add(QueryBuilders.termsQuery("userName", userNames));
        conditionList.add(QueryBuilders.rangeQuery("betTime").lte(endStr).gte(startStr)
                .format("yyyy-MM-dd HH:mm:ss"));

        boolBuilder.must().addAll(conditionList);

        // ?????????????????????????????????
        TermsAggregationBuilder aggUserName = AggregationBuilders.terms("agg_username").field("userName").size(userNames.size());
        DateHistogramAggregationBuilder aggBetdate = AggregationBuilders.dateHistogram("agg_betdate").field("betTime")
                .dateHistogramInterval(DateHistogramInterval.DAY).minDocCount(0l);
        SumAggregationBuilder aggSumValidbet = AggregationBuilders.sum("agg_sum_bet").field("bet");
        aggBetdate.subAggregation(aggSumValidbet);
        aggUserName.subAggregation(aggBetdate);

        // ?????????????????????
        SearchSourceBuilder sourceBuilder = SearchSourceBuilder
                .searchSource()
                .fetchSource(new String[]{"id","betTime","bet","validBet","userName","sitePrefix"},null);

        // ??????????????????
        SearchRequestBuilder searchRequestBuilder = connection.client
                .prepareSearch(ElasticSearchConstant.REPORT_INDEX);

        searchRequestBuilder.setTypes(ElasticSearchConstant.REPORT_TYPE);
        searchRequestBuilder.setSource(sourceBuilder);
        searchRequestBuilder.setQuery(boolBuilder)
                .setFrom(0).setSize(10);
        searchRequestBuilder.addAggregation(aggUserName);

        String searchStr = searchRequestBuilder.toString();

        Response response = null;
        Map esResultMap = null;
        try {
            response = connection.restClient_Read.performRequest("GET",
                    "/" + ElasticSearchConstant.REPORT_INDEX
                            + "/" + ElasticSearchConstant.REPORT_TYPE
                            + "/_search",
                    Collections.singletonMap("_source", "true"),
                    new NStringEntity(searchStr, ContentType.APPLICATION_JSON));
            esResultMap = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            log.error("[RetentionRateService.getUserDailyBetAmountFromES] - ?????????????????????????????????",e);
        }

        // ??????es??????????????????????????????????????????
        List<DepositOrBetDailyDto> result = new ArrayList<>();
        SimpleDateFormat strFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map aggs = (Map) esResultMap.get("aggregations");
        Map aggUsername = (Map) aggs.get("agg_username");
        JSONArray userNameBuckets = (JSONArray) aggUsername.get("buckets");

        userNameBuckets.forEach(b->{
            JSONObject bucket = (JSONObject) b;
            String userName = (String) bucket.get("key");
            JSONObject aggBetdate1 = (JSONObject) bucket.get("agg_betdate");

            JSONArray aggBetdateBuckets = (JSONArray) aggBetdate1.get("buckets");
            List<DepositOrBetDailyDto> collect = aggBetdateBuckets.stream().map(bd -> {
                JSONObject betDate = (JSONObject) bd;
                Long key = (Long) betDate.get("key");
                String dateStr = strFormat.format(new Date(key));

                JSONObject aggSumValidBet = (JSONObject) betDate.get("agg_sum_bet");
                BigDecimal sum = (BigDecimal) aggSumValidBet.get("value");

                DepositOrBetDailyDto dto = new DepositOrBetDailyDto();
                dto.setUserName(userName);
                dto.setDate(dateStr);
                dto.setAmountSum(sum);
                return dto;
            }).collect(Collectors.toList());

            result.addAll(collect);
        });

        return result;
    }

    /**
     * ??????????????????????????????????????????
     * @param startStr
     * @param endStr
     * @param userNames
     * @return
     */
    private List<DepositOrBetDailyDto> getUserDailyBetAmount(String startStr, String endStr,
                                                             List<String> userNames, String siteCode) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<DepositOrBetDailyDto> result = new ArrayList<>();
        // ????????????????????????
        userNames.forEach(userName->{

            // ?????????????????????????????????999999999?????????????????????????????????
            GameReportQueryModel model = new GameReportQueryModel();
            model.setSiteCode(siteCode);
            model.setBetStrTime(startStr);
            model.setBetEndTime(endStr);
            model.setLoginName(userName);
            if (Integer.valueOf(Constants.EVNumber.one).equals(model.getIsTip())) {
                model.setGametype(String.valueOf(GameTypeEnum.ENUM_TIPS.getKey()));     // ???????????????
            }
            PageUtils utils = analysisService.getRptBetListPage(1, 199999999, model);
            List<RptBetModel> list = (List<RptBetModel>) utils.getList();

            // ?????????????????????????????????????????????????????????
            Map<String, List<RptBetModel>> map = list.stream()
                    .map(m->{
                        Date betTime = m.getBetTime();
                        Date startTime = m.getStartTime();
                        Date payoutTime = m.getPayoutTime();
                        Date downloadTime = m.getDownloadTime();
                        Date orderDate = m.getOrderDate();

                        return m;
                    })
                    .filter(m->m.getBetTime() != null)
                    .collect(Collectors.groupingBy(m -> {
                Date betTime = m.getBetTime();
                String format1 = format.format(betTime);
                return format1;
            }));
            Set<Map.Entry<String, List<RptBetModel>>> entries = map.entrySet();
            entries.forEach(entry->{
                String dateStr = entry.getKey();
                List<RptBetModel> models = entry.getValue();
                DepositOrBetDailyDto dailyDto = new DepositOrBetDailyDto();
                dailyDto.setDate(dateStr);
                dailyDto.setUserName(userName);
                BigDecimal sum = models.stream().map(RptBetModel::getValidBet).reduce(BigDecimal.ZERO, BigDecimal::add);
                dailyDto.setAmountSum(sum);
                result.add(dailyDto);
            });
        });

        return result;
    }

    /**
     *
     * @param depositDailyList
     * @param uName
     * @param finalTempDT
     * @return
     */
    private Optional<DepositOrBetDailyDto> findDailyByUserNameAndDate(List<DepositOrBetDailyDto> depositDailyList, String uName, LocalDateTime finalTempDT) {
        return depositDailyList.stream().filter(depositDaily -> {
            String dateStr = depositDaily.getDate();
            String nameStr = depositDaily.getUserName();
            LocalDate ld = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDateTime ldt = LocalDateTime.of(ld, LocalTime.MIN);
            return finalTempDT.compareTo(ldt) == 0 && uName.equals(nameStr);
        }).findAny();
    }

    /**
     * ???????????????????????????
     * ???????????????????????????????????????????????????????????????1??????????????????2??????????????????3????????????
     * @param rule
     * @param depositAny
     * @param validBetAny
     * @param day ??????????????????????????????????????????????????????????????????????????????99????????????
     * @return
     */
    private Integer getActive(Integer rule, Optional<DepositOrBetDailyDto> depositAny,
                              Optional<DepositOrBetDailyDto> validBetAny, Integer day) {
        Integer active = 0;
        switch (rule) {
            case 1:
                // ????????????:??????????????????????????????????????????
                active = day == 0 ? 1 : isActive(depositAny);
                break;
            case 2:
                // ????????????
                active = isActive(validBetAny);
                break;
            case 3:
                // ????????????
                Integer a1 = isActive(depositAny);
                Integer a2 = isActive(validBetAny);
                // active = a1 | a2;
                active = (day == 0) ? 1 : (a1 | a2);
                break;
        }
        return active;
    }

    /**
     * ???????????????????????????????????????????????????
     * @param any
     * @return
     */
    private Integer isActive(Optional<DepositOrBetDailyDto> any) {
        return any.isPresent() && any.get()!=null && any.get().getAmountSum().doubleValue() > 99
                ? 1 : 0;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     * @param retentionRateDto
     * @param userId
     * @param module
     * @return
     */
    public SysFileExportRecord exportRetentionRateDailyActiveReport(RetentionRateDailyActiveDto retentionRateDto, Long userId, String module) {
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);
        if (null != record) {
            String siteCode = CommonUtil.getSiteCode();
            try {

                PageUtils pageUtils = retentionRateDailyActiveReport(retentionRateDto);

                List<RetentionRateDailyActiveReportVo> resultList = (List<RetentionRateDailyActiveReportVo>) pageUtils.getList();

                ExportParams exportParams = new ExportParams();
                Map<String, Object> map = new HashMap<>();
                List<ExportRetentionRateDailyActiveDto> data = new ArrayList<>();
                for (RetentionRateDailyActiveReportVo vo : resultList) {
                    ExportRetentionRateDailyActiveDto dto = new ExportRetentionRateDailyActiveDto();
                    dto.setFirstChargeTime(vo.getFirstChargeDate());
                    dto.setUserName(vo.getUserName());
                    List<RetentionRateDailyActiveReportVo.ActiveStateIn30Days> stateList = vo.getStateList();
                    if (!CollectionUtils.isEmpty(stateList)) {
                        for (int i = 0; i < stateList.size()-1; i++) {
                            try {
                                String value = stateList.get(i).getState() == null ? "" : stateList.get(i).getState().toString();
                                Method method = ExportRetentionRateDailyActiveDto.class.getDeclaredMethod("setD" + (i+1), String.class);
                                method.invoke(dto, value);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                    data.add(dto);
                }

                map.put("data", data);
                map.put("entity", ExportRetentionRateDailyActiveDto.class);
                map.put("title", exportParams);

                List<Map<String, Object>> sheetsList = new ArrayList<>();
                sheetsList.add(map);
                sysFileExportRecordService.exportMilSheet(sheetsList, userId, module, siteCode);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return record;
    }
}
