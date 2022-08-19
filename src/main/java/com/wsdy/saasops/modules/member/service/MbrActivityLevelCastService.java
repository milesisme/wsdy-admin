package com.wsdy.saasops.modules.member.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.base.mapper.BaseMapper;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrActivityLevelMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrActivityLevel;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Slf4j
@Service
@Transactional
public class MbrActivityLevelCastService {

    @Autowired
    private MbrActivityLevelMapper activityLevelMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrAccountLogService accountLogService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private ElasticSearchConnection_Read connection;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    BaseMapper baseMapper;
    // SimpleDateFormat线程安全处理；
    private static final String defaultsdf = "yyyy-MM-dd HH:mm:ss";
    private static final String sdf = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public List<MbrActivityLevel> mbrActivityLevelList() {
        return mbrMapper.findActivityLevelList();
    }

    public MbrActivityLevel mbrActivityLevelInfo(Integer id) {
        return activityLevelMapper.selectByPrimaryKey(id);
    }

    public void deleteMbrActivityLevel(Integer id) {
        MbrAccount account = new MbrAccount();
        account.setActLevelId(id);
        int count = accountMapper.selectCount(account);
        if (count > 0) {
            throw new R200Exception("删除失败，请先移出会员");
        }
        activityLevelMapper.deleteByPrimaryKey(id);
    }

    public void insertMbrActivityLevel(MbrActivityLevel activityLevel) {
        checkoutAccountLevel(activityLevel);
        activityLevel.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        activityLevelMapper.insert(activityLevel);
    }

    private void checkoutAccountLevel(MbrActivityLevel activityLevel) {
        MbrActivityLevel activityLevel1 = new MbrActivityLevel();
        activityLevel1.setAccountLevel(activityLevel.getAccountLevel());
        int count = activityLevelMapper.selectCount(activityLevel1);
        if (count > 0) {
            throw new R200Exception("会员等级已经存在");
        }
    }

    public void updateAvailableMbrActivityLevel(MbrActivityLevel activityLevel) {
        MbrActivityLevel level = new MbrActivityLevel();
        level.setModifyUser(activityLevel.getModifyUser());
        level.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        level.setId(activityLevel.getId());
        level.setAvailable(activityLevel.getAvailable());
        activityLevelMapper.updateByPrimaryKeySelective(level);
    }

    public void updateAvailableMbrActivityLevelBatch(MbrActivityLevel activityLevel) {
        MbrActivityLevel level = new MbrActivityLevel();
        level.setModifyUser(activityLevel.getModifyUser());
        level.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        level.setIds(activityLevel.getIds());
        level.setAvailable(activityLevel.getAvailable());
        activityLevelMapper.updateMbrActivityLevel(level);
    }

    /**
     * 	更新会员等级配置
     * 
     * @param activityLevel
     */
    public void updateMbrActivityLevel(MbrActivityLevel activityLevel) {
        MbrActivityLevel activityLevel1 = mbrActivityLevelInfo(activityLevel.getId());
        if (!activityLevel1.getAccountLevel().equals(activityLevel.getAccountLevel())) {
            checkoutAccountLevel(activityLevel);
        }
        if(activityLevel.getAccountLevel() == 0){
            activityLevel1.setTierName(activityLevel.getTierName());
            activityLevel1.setDescription(activityLevel.getDescription());
            activityLevel1.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            activityLevel1.setModifyUser(activityLevel.getModifyUser());
            activityLevel1.setFeeAvailable(activityLevel.getFeeAvailable());
            activityLevel1.setWithDrawalQuota(activityLevel.getWithDrawalQuota());  // 限额
            activityLevel1.setWithDrawalTimes(activityLevel.getWithDrawalTimes());
            activityLevelMapper.updateByPrimaryKey(activityLevel1);
        }else{
            activityLevel.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            activityLevelMapper.updateByPrimaryKey(activityLevel);
        }

        // 操作日志保存
        accountLogService.updateMbrActivityLevel(activityLevel,activityLevel1);
    }

    // 会员等级晋升数据
    public HashMap<String,Object> getLevelPromoteData(Integer id) {
        // 获取会员信息
        MbrAccount account = accountMapper.selectByPrimaryKey(id);
        if(Objects.isNull(account)){
            throw new R200Exception("会员晋升数据查询异常：查无该会员！");
        }
        // 获取活动晋升周期 0无限期，1按日，2按周，3按月
        int actLevelStaticsRule = sysSettingService.findActLevelStaticsRule();
        Map<String,String> timeMap = getStartTimeAndEndTime(actLevelStaticsRule);
        List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(CommonUtil.getSiteCode());
        // 查出累积投注额/累积充值范围
        BigDecimal betBigDecimal = getValidVet(sitePrefix, account.getLoginName(),timeMap.get("startTime"),timeMap.get("endTime"));
        BigDecimal depositBigDecimal = fundMapper.sumFundDepositByAccountId(account.getId(),timeMap.get("startTime"),timeMap.get("endTime"));

        HashMap<String,Object> map = new HashMap<>(4);
        map.put("actLevelStaticsRule",actLevelStaticsRule);
        map.put("betBigDecimal",betBigDecimal);
        map.put("depositBigDecimal",depositBigDecimal);
        return map;
    }

    private Map<String,String> getStartTimeAndEndTime(Integer rule){
        Map<String,String> timeMap = new HashMap<>(2);
        String startTime = null;
        String endTime = null;
        if(Constants.EVNumber.one == rule){
            startTime = DateUtil.getPastDate(1,DateUtil.FORMAT_10_DATE)+" 00:00:00";
            endTime = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
        }else if(Constants.EVNumber.two == rule){
            startTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME,0,0);//本周第一天
            endTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME,-1,0);//下周第一天
        }else if(Constants.EVNumber.three == rule){
            startTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME,0,0);//本月第一天
            endTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME,-1,0);//下月第一天
        }
        timeMap.put("startTime",startTime);
        timeMap.put("endTime",endTime);
        return timeMap;
    }

    private BigDecimal getValidVet(List<String> sitePrefix, String username,String startTime,String endTime) {
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
}
