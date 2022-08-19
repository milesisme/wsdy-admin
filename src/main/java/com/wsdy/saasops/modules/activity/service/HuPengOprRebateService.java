package com.wsdy.saasops.modules.activity.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.api.modules.activity.dto.HuPengLevelRewardDto;
import com.wsdy.saasops.api.modules.activity.dto.HuPengRebateDto;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.FriendRebateConstants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.modules.activity.dao.MbrRebateHuPengMapper;
import com.wsdy.saasops.modules.activity.dao.MbrRebateHuPengRewardMapper;
import com.wsdy.saasops.modules.activity.entity.MbrRebateHuPengReward;
import com.wsdy.saasops.modules.activity.dto.*;
import com.wsdy.saasops.modules.activity.entity.MbrRebateHuPeng;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.activity.mapper.ActivityMapper;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.analysis.entity.RptBetModel;
import com.wsdy.saasops.modules.member.dto.RebateDto;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrRebateFriends;
import com.wsdy.saasops.modules.member.entity.MbrRebateFriendsReward;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import lombok.extern.slf4j.Slf4j;
import ognl.IntHashMap;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_25_DATE_TIME;
import static java.util.Objects.isNull;

@Slf4j
@Service
public class HuPengOprRebateService {

    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    @Autowired
    private ElasticSearchConnection_Read connection;

    @Autowired
    private MbrMapper mbrMapper;

    @Autowired
    private OprActActivityCastService oprActActivityCastService;

    @Autowired
    private MbrRebateHuPengRewardMapper mbrRebateHuPengRewardMapper;

    @Autowired
    private MbrRebateHuPengMapper mbrRebateHuPengMapper;

    @Autowired
    private MbrWalletService walletService;


    private static final String defaultsdf = "yyyy-MM-dd HH:mm:ss"; // SimpleDateFormat线程安全处理；
    private static final String sdf = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String platform ="ISG";

    public PageUtils huPengRebateRewardList(String loginName, String startTime, String endTime, Integer groupId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<RebateHuPengRewardDto> rebateFriendsRewardDtoList = activityMapper.huPengRebateRewardList(loginName, groupId, startTime, endTime);
        return BeanUtil.toPagedResult(rebateFriendsRewardDtoList);
    }


    public PageUtils huPengRebateRewardDetails(String loginName, String startTime, String endTime, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<RebateHuPengDetailsDto> rebateFriendsDetailsDtoList = activityMapper.huPengRebateRewardDetails(loginName, startTime, endTime);
        return BeanUtil.toPagedResult(rebateFriendsDetailsDtoList);
    }


    public BigDecimal huPengRebateRewardDetailsSummary(String loginName, String startTime, String endTime){
        return activityMapper.huPengRebateRewardDetailsSummary(loginName, startTime, endTime);
    }


    public PageUtils huPengFriendsRebateRewardList(String loginName, String startTime, String endTime, Integer groupId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<RebateHuPengFriendsRewardDto> rebateFriendsRewardDtoList = activityMapper.huPengFriendsRebateRewardList(loginName, groupId, startTime, endTime);
        return BeanUtil.toPagedResult(rebateFriendsRewardDtoList);
    }

    public SysFileExportRecord orderExport(String loginName, Integer groupId, String startTime, String endTime, Long userId, String module, String orderReviewExcelPath){
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, module);

        if (null != record) {
            List<RebateHuPengRewardDto> rebateHuPengRewardDtoList = activityMapper.huPengRebateRewardList(loginName, groupId, startTime, endTime);
            String siteCode = CommonUtil.getSiteCode();
            List<Map<String, Object>> list = rebateHuPengRewardDtoList.stream().map(e -> {

                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            sysFileExportRecordService.exportExcel(orderReviewExcelPath, list, userId, module, siteCode);
        }
        return record;
    }

    public R checkFile(String module, String orderReportExport, Long userId){
        // 查询用户的module下载记录
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if(null != record){
            String fileName = "";
            fileName = orderReportExport.substring(orderReportExport.lastIndexOf("/")+1,orderReportExport.length());
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }

    public String accountHuPengRebate(String siteCode, String clacDay){
        try {
            return doAccountHuPengRebate(siteCode, clacDay);
        }catch (Exception e){
            log.error("呼朋推荐计算异常【huPengRebate_" + siteCode + "】:" + e.getMessage(), e);
        }
        return null;
    }

    private String doAccountHuPengRebate(String siteCode, String clacDay){
        // 判断是否有有可用的返利活动
        OprActActivity actActivity = oprActActivityCastService.getHupengRebateAct();
        if (isNull(actActivity) || StringUtil.isEmpty(actActivity.getRule())) {
            log.info("呼朋推荐活动不存在【huPengRebate_" + siteCode + "】");
            return "活动不存在";
        }
        // 获得返利规则
        HuPengRebateDto huPengRebateDto = jsonUtil.fromJson(actActivity.getRule(), HuPengRebateDto.class);
        if (isNull(huPengRebateDto)) {
            log.info("呼朋荐配置解析错误【huPengRebate_" + siteCode + "】");
            return "配置不存在";
        }
        Calendar cal = Calendar.getInstance();
        if(StringUtil.isNotEmpty(clacDay)){
            String[] date = clacDay.split("-");
            cal.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
        }

        String friendCalcDay = DateUtil.format(cal.getTime(), DateUtil.FORMAT_10_DATE);
        cal.add(Calendar.DATE, -1);
        String incomeDay = DateUtil.format(cal.getTime(), FORMAT_10_DATE);
        huPengRebateDto.toMap();
        Map<String, HuPengBetStatistics> huPengBetStatisticsMap = new HashMap<>();
        int pageNo = 0;
        int totalPage = -1;
        int pageSize = 10000;

        do{
            pageNo ++;
            String starTime = huPengRebateDto.getStartTime();
            String endTime = huPengRebateDto.getEndTime();
            BoolQueryBuilder builder = getBuilder(incomeDay + " 00:00:00", incomeDay+ " 23:59:59", starTime, endTime);
            SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
            searchRequestBuilder.addSort(SortBuilders.fieldSort("betTime").order(SortOrder.DESC));
            searchRequestBuilder.setQuery(builder);
            searchRequestBuilder.setFrom((pageNo -1) * pageSize );
            searchRequestBuilder.setSize(pageSize);
            Response response = null;
            try {
                response = connection.restClient_Read.performRequest("GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search", Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
            } catch (IOException e) {
                log.error("请求参数查询异常：huPengRebate_[{}]", searchRequestBuilder.toString(), e);
                e.printStackTrace();
            }

            Map map = null;
            try {
                map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            } catch (IOException e) {
                log.error("解析查询结果异常：huPengRebate_",  e);
                e.printStackTrace();
            }
            JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));
            Integer total = ((Integer) (((Map) map.get("hits")).get("total")));
            log.info("查询总条数：huPengRebate_, total = [{}] hits = [{}]",total,hits.size());
            if(totalPage == -1){
                totalPage = total / pageSize  + 1;
            }
            for (Object obj : hits) {
                Map objmap = (Map) obj;
                RptBetModel rptBetModel = JSON.parseObject(objmap.get("_source").toString(), RptBetModel.class);
                if (StringUtil.isEmpty(rptBetModel.getGameCategory())) {
                    rptBetModel.setGameCategory("Unknown");
                }

                // 下注统计
                if(rptBetModel.getUserName()!=null && rptBetModel.getUserName().length() > 0 ){
                    // 下注类型是体验不参与计算 等于null是兼容老数据
                    if(!"撤单".equals(rptBetModel.getStatus()) && (rptBetModel.getUserBetType() == null || rptBetModel.getUserBetType() == 1) ){
                        HuPengBetStatistics huPengBetStatistics = huPengBetStatisticsMap.get(rptBetModel.getUserName());
                        if(huPengBetStatistics ==null){
                            huPengBetStatistics = new HuPengBetStatistics();
                            huPengBetStatisticsMap.put(rptBetModel.getUserName(), huPengBetStatistics);
                        }
                        // 统计
                        huPengBetStatistics.setBetNum(huPengBetStatistics.getBetNum() + 1);
                        if(rptBetModel.getPayout().compareTo(BigDecimal.ZERO) == 1){
                            huPengBetStatistics.setRewardBetNum(huPengBetStatistics.getRewardBetNum() + 1);
                            huPengBetStatistics.setAmount(huPengBetStatistics.getAmount().add(rptBetModel.getPayout()));
                        }
                    }
                }
            }
        }while(pageNo < totalPage);


        //奖励缓存信息
        Map<String, List<MbrRebateHuPeng>>  mbrRebateHuPengMap = new HashMap<>();

        //奖励配置
        Map<Integer, HuPengLevelRewardDto> huPengLevelRewardDtoMap =  huPengRebateDto.getHuPengLevelRewardDtoMap();
        // 计算返奖明细
        for(Map.Entry<String, HuPengBetStatistics> entry :  huPengBetStatisticsMap.entrySet()){
            String loginName = entry.getKey();
            HuPengBetStatistics huPengBetStatistics = entry.getValue();
            List<HuPengLevelDto> huPengLevelDtos   = mbrMapper.getParentList(loginName);
            BigDecimal amount  =  huPengBetStatistics.getAmount();
            for (int i = 0; i < huPengLevelDtos.size(); i ++){
                HuPengLevelDto huPengLevelDto = huPengLevelDtos.get(i);
                HuPengLevelRewardDto huPengLevelRewardDto = huPengLevelRewardDtoMap.get(huPengLevelDto.getDepth());
                if(huPengLevelRewardDto != null){
                    BigDecimal reward = amount.multiply(huPengLevelRewardDto.getRate()).divide(new BigDecimal(100));
                    if(reward.compareTo(huPengLevelRewardDto.getMaxReward()) == 1){
                        reward = huPengLevelRewardDto.getMaxReward();
                    }
                    // 保留2位有效数字，去掉末尾
                    reward = reward.setScale(2, BigDecimal.ROUND_DOWN);
                    MbrRebateHuPeng mbrRebateHuPeng = new MbrRebateHuPeng();
                    mbrRebateHuPeng.setAccountId(huPengLevelDto.getAccountId());
                    mbrRebateHuPeng.setLoginName(huPengLevelDto.getLoginName());
                    mbrRebateHuPeng.setSubLoginName(loginName);
                    mbrRebateHuPeng.setSubAccountId(huPengLevelDto.getSubAccountId());
                    mbrRebateHuPeng.setActivityId(actActivity.getId());
                    mbrRebateHuPeng.setAmount(amount);
                    mbrRebateHuPeng.setReward(reward);
                    mbrRebateHuPeng.setBetNum(huPengBetStatistics.getBetNum());
                    mbrRebateHuPeng.setRewardNum(huPengBetStatistics.getRewardBetNum());
                    mbrRebateHuPeng.setRate(huPengLevelRewardDto.getRate());
                    mbrRebateHuPeng.setCreateTime(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
                    mbrRebateHuPeng.setCreater(Constants.SYSTEM_USER);
                    mbrRebateHuPeng.setOperationType(0);
                    mbrRebateHuPeng.setIncomeTime(incomeDay);
                    List<MbrRebateHuPeng> rewardList = mbrRebateHuPengMap.get(huPengLevelDto.getLoginName());
                    if(rewardList == null){
                        rewardList = new ArrayList<>();
                        mbrRebateHuPengMap.put(huPengLevelDto.getLoginName(), rewardList);
                    }
                    rewardList.add(mbrRebateHuPeng);
                }
            }
        }

        Map<String, MbrRebateHuPengReward> mbrRebateHuPengRewardMap = new HashMap<>();
        for (Map.Entry<String, List<MbrRebateHuPeng>> entry : mbrRebateHuPengMap.entrySet()){
            List<MbrRebateHuPeng> mbrRebateHuPengList = entry.getValue();
            MbrRebateHuPengReward mbrRebateHuPengReward = new MbrRebateHuPengReward();
            mbrRebateHuPengReward.setRewardNum(0);
            mbrRebateHuPengReward.setIncomeTime(incomeDay);
            mbrRebateHuPengReward.setCreateTime(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
            mbrRebateHuPengReward.setCreater(Constants.SYSTEM_USER);
            mbrRebateHuPengReward.setActivityId(actActivity.getId());
            mbrRebateHuPengReward.setOperationType(0);
            mbrRebateHuPengReward.setBetNum(0);
            for (MbrRebateHuPeng mbrRebateHuPeng :mbrRebateHuPengList){
                mbrRebateHuPengReward.setLoginName(mbrRebateHuPeng.getLoginName());
                mbrRebateHuPengReward.setAccountId(mbrRebateHuPeng.getAccountId());
                mbrRebateHuPengReward.setReward(mbrRebateHuPengReward.getReward().add(mbrRebateHuPeng.getReward()));
                mbrRebateHuPengReward.setRewardNum(mbrRebateHuPengReward.getRewardNum() + mbrRebateHuPeng.getRewardNum());
                mbrRebateHuPengReward.setBetNum(mbrRebateHuPengReward.getBetNum() + mbrRebateHuPeng.getBetNum());
                mbrRebateHuPengReward.setAmount(mbrRebateHuPengReward.getAmount().add(mbrRebateHuPeng.getAmount()));
            }
            mbrRebateHuPengRewardMap.put(mbrRebateHuPengReward.getLoginName(), mbrRebateHuPengReward);
        }
        for(Map.Entry<String, MbrRebateHuPengReward>  entry : mbrRebateHuPengRewardMap.entrySet()){
            Map mapCount =  mbrMapper.findFriendRebateCount(friendCalcDay, entry.getValue().getAccountId());
            Long num = (Long)mapCount.get("num");
            entry.getValue().setInviteNum(num.intValue());
        }
        this.saveAndGiveOut(mbrRebateHuPengMap, mbrRebateHuPengRewardMap);
        return null;
    }


    private BoolQueryBuilder getBuilder(String payOutStrTime, String payOutEndTime, String startTime, String endTime){
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        try{
            List<String> usernameList = mbrMapper.getMemberAccountHuPengNames(startTime, endTime);
            log.info("查询{}-{}获取玩家列表：huPengRebate_[{}]",startTime, endTime, jsonUtil.toJson(usernameList));
            SimpleDateFormat dateFormat = new SimpleDateFormat(defaultsdf);
            SimpleDateFormat dateFormatSdf = new SimpleDateFormat(sdf);
            builder.must(QueryBuilders.rangeQuery("payoutTime").gte(dateFormatSdf.format(dateFormat.parse(payOutStrTime)))
                  .lte(dateFormatSdf.format(dateFormat.parse(payOutEndTime))));
            builder.must(QueryBuilders.matchPhraseQuery("platform", platform));
            builder.must(QueryBuilders.termsQuery("userName", toLowerCase(usernameList)));
        }catch (Exception e){
            log.error("查询{}-{}设置查询玩家条件出错：huPengRebate_",startTime, endTime, e);
            e.printStackTrace();
        }
        return builder;
    }

    public List toLowerCase(List list) {
        List newList = new ArrayList();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            newList.add(String.valueOf(it.next()).toLowerCase());
        }
        return newList;
    }

    private  void saveAndGiveOut(Map<String, List<MbrRebateHuPeng>>  mbrRebateHuPengMap, Map<String, MbrRebateHuPengReward> mbrRebateHuPengRewardMap){
        this.saveHuPengRebate(mbrRebateHuPengMap);
        this.saveAndGiveOutHupengRebateReward(mbrRebateHuPengRewardMap);
    }

    public void saveHuPengRebate(Map<String, List<MbrRebateHuPeng>>  mbrRebateHuPengMap){
        List<MbrRebateHuPeng> list = new ArrayList<>();
        for(Map.Entry<String, List<MbrRebateHuPeng>>  entry : mbrRebateHuPengMap.entrySet()){
            List<MbrRebateHuPeng> mbrRebateHuPengList = entry.getValue();
                for(MbrRebateHuPeng mbrRebateHuPeng : mbrRebateHuPengList){
                    mbrRebateHuPeng.setGiveOutTime(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
                    Integer count = activityMapper.getMbrHupengFriendsCount(mbrRebateHuPeng.getAccountId(), mbrRebateHuPeng.getSubAccountId(),mbrRebateHuPeng.getActivityId(),  mbrRebateHuPeng.getIncomeTime());
                    if(count <= 0){
                        list.add(mbrRebateHuPeng);
                    }
            }
        }
        // 批量保存
        if(list.size() > 0){
            int pageSize = 1000;
            List<MbrRebateHuPeng> saveList = new ArrayList<>();
            for(int i = 1; i <= list.size(); i ++){
                if(i % pageSize == 0){
                    mbrRebateHuPengMapper.insertList(saveList);
                    saveList.clear();
                }else{
                    saveList.add(list.get(i - 1));
                }
            }
            if(saveList.size() > 0){
                mbrRebateHuPengMapper.insertList(saveList);
            }
        }
    }

    public void saveAndGiveOutHupengRebateReward(Map<String, MbrRebateHuPengReward> mbrRebateHuPengRewardMap) {
        List<MbrRebateHuPengReward> mbrRebateHuPengRewards = new ArrayList<>();
        for (Map.Entry<String, MbrRebateHuPengReward> entry : mbrRebateHuPengRewardMap.entrySet()) {
            MbrRebateHuPengReward mbrRebateHuPengReward = entry.getValue();
            Integer count = activityMapper.getMbrHupengFriendsRewardCount(mbrRebateHuPengReward.getAccountId(), mbrRebateHuPengReward.getActivityId(), mbrRebateHuPengReward.getIncomeTime());
            if (count <= 0) {
                mbrRebateHuPengReward.setGiveOutTime(DateUtil.format(new Date(), FORMAT_18_DATE_TIME));
                mbrRebateHuPengRewards.add(mbrRebateHuPengReward);
                MbrWallet mbrWallet = new MbrWallet();
                mbrWallet.setAccountId(mbrRebateHuPengReward.getAccountId());
                mbrWallet.setHuPengBalance(mbrRebateHuPengReward.getReward());


                MbrBillDetail mbrBillDetail = new MbrBillDetail();
                mbrBillDetail.setLoginName(mbrRebateHuPengReward.getLoginName());
                mbrBillDetail.setAccountId(mbrRebateHuPengReward.getAccountId());
                mbrBillDetail.setFinancialCode(OrderConstants.FUND_ORDER_HUPENG_ADD);
                mbrBillDetail.setOrderNo(new SnowFlake().nextId() + "");
                mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
                mbrBillDetail.setDepotId(Constants.SYS_DEPOT_ID);
                mbrBillDetail.setAmount(mbrRebateHuPengReward.getReward());
                walletService.hPWalletAdd(mbrWallet, mbrBillDetail);
            }
        }

        if (mbrRebateHuPengRewards.size() > 0) {
            int pageSize = 1000;
            List<MbrRebateHuPengReward> saveList = new ArrayList<>();
            for (int i = 1; i <= mbrRebateHuPengRewards.size(); i++) {
                if (i % pageSize == 0) {
                    mbrRebateHuPengRewardMapper.insertList(saveList);
                    saveList.clear();
                } else {
                    saveList.add(mbrRebateHuPengRewards.get(i - 1));
                }
            }
            if (saveList.size() > 0) {
                mbrRebateHuPengRewardMapper.insertList(saveList);
            }
        }

    }

}
