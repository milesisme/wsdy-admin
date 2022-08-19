package com.wsdy.saasops.modules.member.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
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
import com.wsdy.saasops.modules.member.entity.MbrAuditAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.mapper.AuditMapper;
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
import com.google.gson.stream.JsonReader;
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.constants.Constants.ONE_HUNDRED;
import static com.wsdy.saasops.common.utils.CommonUtil.adjustScale;
import static com.wsdy.saasops.common.utils.DateUtil.*;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
public class AccountWaterCastService {

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
    private AuditMapper auditMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OprActWaterBetdataMapper oprActWaterBetdataMapper;
    @Autowired
    private OprActActivityService actActivityService;
    @Autowired
    private JsonUtil jsonUtil;

    public void castWaterActivity(String siteCode) {
        log.info("【"+siteCode + "】==castWaterActivity==" + "开始");

        // 参数： 反水注单时间范围、站点线路前缀
        List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(siteCode);
        String startTime = formatEsDate(getPastDate(Constants.EVNumber.one, FORMAT_10_DATE) + HHMMSS);
        String endTime = formatEsDate(getPastDate(Constants.EVNumber.zero, FORMAT_10_DATE) + HHMMSS);

        // 获取反水活动，支持多个反水活动，循环处理
        List<OprActActivity> activities = getOprActActivitys(null);
        for (OprActActivity as : activities) {
            // 1. 根据活动周期，处理反水注单时间范围
            JWaterRebatesDto dto = new Gson().fromJson(as.getRule(), JWaterRebatesDto.class);
            int forWeek = dayForWeek(getCurrentDate(FORMAT_10_DATE));
            if (dto.getPeriod() == 1 && forWeek != Constants.EVNumber.one) {
                log.info("【"+siteCode + "】==castWaterActivity==" + "活动周结算，今天不是周一【" + as.getId() + "】");
                continue;
            }
            if (dto.getPeriod() == 1 && forWeek == Constants.EVNumber.one) {
                startTime = formatEsDate(getPastDate(Constants.EVNumber.seven, FORMAT_10_DATE) + HHMMSS);   //上周周第一天
                endTime = formatEsDate(getPastDate(Constants.EVNumber.zero, FORMAT_10_DATE) + HHMMSS);      //本周第一天
            }
            String finalStartTime = startTime;
            String finalEndTime = endTime;

            // 2.从rpt表获取过去8天内有有效注单的会员列表  （这一句可以提到循环外 TODO）
            List<MbrAccount> accountList = mbrMapper.findAccountAndValidbetList(
                    getPastDate(Constants.EVNumber.three, FORMAT_10_DATE),
                    getPastDate(Constants.EVNumber.one, FORMAT_10_DATE));
            if (Collections3.isEmpty(accountList)) {
                continue;
            }

            List<String> accountNameList = accountList.stream().map(MbrAccount::getLoginName).collect(Collectors.toList());
            log.info("【"+siteCode + "】==castWaterActivity==" + "accountList==(" + jsonUtil.toJson(accountNameList) + ")");

            // 3. 循环计算的反水：会员
            accountList.forEach(account ->
                    castMbrValidbet(account, siteCode, finalStartTime, finalEndTime, as, dto, sitePrefix));
        }
        log.info("【"+siteCode + "】==castWaterActivity==" + "结束");
    }


    public List<OprActActivity> getOprActActivitys(Integer isSelfHelp) {
        OprActActivity activity = new OprActActivity();
        activity.setUseStart(getPastDate(Constants.EVNumber.one, FORMAT_10_DATE));
        activity.setTmplCode(waterRebatesCode);
        activity.setIsSelfHelp(isSelfHelp);
        activity.setIsSelfHelpShow(isSelfHelp);
        return activityMapper.findWaterActivity(activity);
    }



    /**
     *  计算会员反水
     *
     * @param account       会员
     * @param siteCode      站点code
     * @param startTime     注单开始时间
     * @param endTime       注单结束时间
     * @param activity      活动
     * @param dto           反水活动规则
     * @param sitePrefix    站点线路前缀列表
     * @return void
     */
    @Async("waterActivityTaskAsyncExecutor")
    @Transactional
    public void castMbrValidbet(MbrAccount account, String siteCode, String startTime, String endTime, OprActActivity activity, JWaterRebatesDto dto, List<String> sitePrefix) {
        log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==castMbrValidbet==activity==" + activity.getId() + "==startTime==" + startTime + "==endTime==" + endTime + "==开始" );

        String key = RedisConstants.BATCH_WATER_ACCOUNT + activity.getId() + siteCode + account.getLoginName();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, account.getLoginName(), 200, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            ThreadLocalCache.setSiteCodeAsny(siteCode);

            // 3.1 计算前校验
            // 校验1：反水黑名单
            if(actActivityService.isBlackList(account.getId(),waterRebatesCode)||actActivityService.isBlackList(account.getId(), TOpActtmpl.allActivityCode)){
                log.info("【"+siteCode + "】自动反水==account==" +  account.getLoginName()+"==是返水黑名单会员");
                return;
            }


            //判断是否存在vip活动代理黑名单
            if (actActivityService.valAgentBackList(account,waterRebatesCode)){
                log.info("【"+siteCode + "】自动反水==account==" +  account.getLoginName()+"==上级代理存在返水黑名单");
                return;
            }
            //判断是否存在所有活动黑名单
            if (actActivityService.valAgentBackList(account,TOpActtmpl.allActivityCode)){
                log.info("【"+siteCode + "】自动反水==account==" +  account.getLoginName()+"==上级代理存在所有活动黑名单");
                return;
            }
            // 校验2：首存送未通过
            int count = auditMapper.findAuditAccountPreferential(account.getId(), preferentialCode);
            if (count > 0) {
                log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==会员首存稽核不通过，会员不发放返水" );
                return;
            }
            // 校验3：今日opr_act_bonus存在反水记录的不再继续处理
            int countWater = activityMapper.findValidbetCount(activity.getId(), getCurrentDate(FORMAT_10_DATE), Constants.SYSTEM_USER, account.getId());
            if (countWater > 0) {
                log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==该会员今日已经返水");
                return;
            }

            // 3.2 处理反水注单时间范围： 首存送稽核完成之前的注单不做反水
            String oldStartTime = startTime.toString();     // 保留旧的开始时间，用于记录数据库
            /*List<MbrAuditAccount> list = auditMapper.findAuditAccountPreferentialEx(account.getId(), preferentialCode);
            if (Objects.nonNull(list) && list.size()> 0) {  // 存在首存送稽核
                if(Integer.valueOf(Constants.EVNumber.zero).equals(list.get(0).getStatus())){
                    log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==会员首存稽核不通过，会员不发放返水" );
                    return;
                }
                // 通过首存稽核的，判断稽核通过时间
                String passTime = list.get(0).getPassTime();    // 稽核通过时间
                if(!StringUtil.isEmpty(passTime)) {
                    passTime = passTime.substring(0,19);
                    String tmpStartTime = DateUtil.formatEsDateToTime(startTime);
                    String tmpEndTime = DateUtil.formatEsDateToTime(endTime);
                    log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==会员首存稽核通过时间为：" + passTime + ",反水开始时间为：" + tmpStartTime + ",反水截止时间为：" + tmpEndTime );
                    int result = DateUtil.timeCompare(passTime, tmpEndTime, DateUtil.FORMAT_18_DATE_TIME);
                    if (result >= Constants.EVNumber.zero) {   // 稽核通过时间大于反水截止时间
                        log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==会员首存稽核通过时间为：" + passTime + ",反水截止时间为：" + tmpEndTime + ",会员不发放反水" );
                        return;
                    }
                    result = DateUtil.timeCompare(passTime, tmpStartTime, DateUtil.FORMAT_18_DATE_TIME);
                    if (result > Constants.EVNumber.zero) {   // 稽核通过时间大于反水开始时间,修改反水开始时间
                        startTime = DateUtil.formatEsDate(passTime);
                        log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==修改会员反水开始时间为" + startTime);
                    }
                }
            }*/

            // 3.3 获取ES注单数据并计算反水，保存反水记录表opr_act_water
            List<Integer> waterIds = accountValidBetDetail(account, startTime, endTime, activity, dto, sitePrefix, siteCode);

            // 3.4 保存优惠opr_act_bonus，并处理相关表：反水批次opr_act_water_betdate，帐变mbr_bill_detail
            if (waterIds.size() > 0) {
                activity.setWaterStart(oldStartTime);
                activity.setWaterEnd(endTime);
                log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==insertOprActBonus==开始" );
                OprActBonus bonus = insertOprActBonus(account, activity, siteCode, Constants.SYSTEM_USER, waterIds, RedisConstants.OPRACT_WATER_BATCHINFO);
                log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==insertOprActBonus==结束");
                if (activity.getIsAudit() == Constants.EVNumber.zero && nonNull(bonus)) {
                    bonus.setStatus(Constants.EVNumber.one);
                    bonus.setAuditUser(Constants.SYSTEM_USER);
                    bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==grantOprActBonus==开始" );
                    grantOprActBonus(bonus, siteCode);
                    log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==grantOprActBonus==结束" );
                }
            }
            redisService.del(key);
        }

        log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==castMbrValidbet==activity==" + activity.getId() + "==startTime==" + startTime + "==endTime==" + endTime + "==结束" );
    }

    public void grantOprActBonus(OprActBonus bonus, String siteCode) {

        MbrBillDetail billDetail = walletService.castWalletAndBillDetail(bonus.getLoginName(),
                bonus.getAccountId(), OrderConstants.ACTIVITY_WATERBONUS,
                bonus.getBonusAmount(), bonus.getOrderNo(), Boolean.TRUE,null,null);
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
            waterBonus.setSource(Constants.EVNumber.one);
            waterBonus.setPrizetype(Constants.EVNumber.zero);
            waterBonus.setCreateUser(Constants.SYSTEM_USER);
            actBonusMapper.insert(waterBonus);

            activityMapper.updateOprActWater(waterBonus.getId(), waterIds);
            return waterBonus;
        }
        return null;
    }

    public Integer insertOprActWaterBatchInfo(OprActActivity activity, String siteCode, String prefixKey) {
        String waterEnd = StringUtil.substring(activity.getWaterEnd(), 0, 10);
//        waterEnd = getLastOneDayEnd(waterEnd);  // 处理时间为前一天的23:59:59
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

    /**
     *  获取ES注单数据并计算反水，保存反水记录表opr_act_water
     * @param account       会员
     * @param startTime     注单开始时间
     * @param endTime       注单结束时间
     * @param activity      活动
     * @param dto           反水活动规则
     * @param sitePrefix    站点线路前缀列表
     * @param siteCode      站点code
     * @return  null
     */
    private List<Integer> accountValidBetDetail(MbrAccount account, String startTime, String endTime,
                                                OprActActivity activity, JWaterRebatesDto dto, List<String> sitePrefix,
                                                String siteCode) {
        log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==accountValidBetDetail==开始1=="
                + "id==" + account.getId() + "==actLevelId==" + account.getActLevelId() + "==cagencyId==" + account.getCagencyId() + "==tagencyId==" + account.getTagencyId());

        List<Integer> waterIds = Lists.newArrayList();      // 会员反水记录表ids
        // 层次：代理 -> 活动等级 -> 游戏类别 -> 游戏平台
        // 3.3.1 获取符合该会员的层级规则：按代理，未配置代理取默认
        List<JWaterRebatesLevelDto> levelDtoList = getWaterRebatesLevelList(account,dto.getRebatesNeDto());
        if (Collections3.isNotEmpty(levelDtoList)) {
            log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==accountValidBetDetail==开始2");

            // 3.3.2 获取符合该会员活动等级的返水优惠比例设置
            Optional<JWaterRebatesLevelDto> rebatesLevelDto = levelDtoList.stream().filter(e ->
                    e.getAccountLevel().equals(account.getActLevelId())).findAny();
            if (rebatesLevelDto.isPresent()) {
                log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==accountValidBetDetail==开始3");
                JWaterRebatesLevelDto jWaterRebatesLevelDto = rebatesLevelDto.get();    // 该会员活动等级的返水优惠比例设置
                if (Collections3.isNotEmpty(jWaterRebatesLevelDto.getRebatesLevelListDtos())) {
                    log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==accountValidBetDetail==开始4");

                    // 3.3.3 获取ES注单数据并计算反水： 按类别循环获取该类别下平台的有效投注   catId：1体育， 3真人，5电子，6棋牌，8捕鱼，12彩票, 9电竞
                    Map<Integer, List<JWaterRebatesLevelListDto>> rebatesLevelListGroupingBy =
                            jWaterRebatesLevelDto.getRebatesLevelListDtos().stream().collect(
                                    Collectors.groupingBy(JWaterRebatesLevelListDto::getCatId));
                    for (Integer catIdKey : rebatesLevelListGroupingBy.keySet()) {
                        List<JWaterRebatesLevelListDto> rebatesLevelList = rebatesLevelListGroupingBy.get(catIdKey);

                        // 获取配置比例的平台code
                        List<Integer> depotIds = rebatesLevelList.stream().map(JWaterRebatesLevelListDto::getDepotId).collect(Collectors.toList());
                        List<TGmDepot> depotCodes = baseMapper.findDepotCodesById(depotIds);
                        List<String> codes = depotCodes.stream().map(TGmDepot::getDepotCode).collect(Collectors.toList());
                        // 获取配置比例的es类别
//                        List<String> gameCategorys = catIdKey == Constants.EVNumber.one
//                                ? Lists.newArrayList(Constants.depotCatMap.get(catIdKey),
//                                Constants.depotCatMap.get(Constants.EVNumber.nine))
//                                : Lists.newArrayList(Constants.depotCatMap.get(catIdKey));
                        List<String> gameCategorys = Lists.newArrayList(Constants.depotCatMap.get(catIdKey));

                        // 3.3.3.1 查询es获取有效投注： 线路/会员名/es类别/平台code/时间范围/是否已经反水
                        log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()
                                + "==sitePrefix==" + jsonUtil.toJson(sitePrefix) + "==gameCategorys==" + jsonUtil.toJson(gameCategorys) + "==codes==" + jsonUtil.toJson(codes));

                        List<WaterValidBetDto> validBetDtoList = getValidBet(sitePrefix, account.getLoginName(), gameCategorys, startTime, endTime, codes, "0");

                        if(Objects.isNull(validBetDtoList)){
                            log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==validBetDtoList==null" );
                        }else if(validBetDtoList.size() == 0){
                            log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==validBetDtoList==0" );
                        }

                        if (validBetDtoList.size() > 0) {
                            log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+ "==validBetDtoList==" + jsonUtil.toJson(validBetDtoList) );

                            // 3.3.3.2 计算该类别该平台下的有效投注的反水
                            for (WaterValidBetDto rs : validBetDtoList) {
                                // 匹配配置的平台id --> 获取平台的比例设置
                                Integer depotId = depotCodes.stream().filter(
                                        d -> d.getDepotCode().equalsIgnoreCase(rs.getDepotCode())).findAny().get().getId();
                                JWaterRebatesLevelListDto rebatesLevelListDto = rebatesLevelList.stream().filter(
                                        d -> d.getDepotId().equals(depotId)).findAny().get();

                                log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()
                                        + "==validBetDtoList==rebatesLevelListDto==" +jsonUtil.toJson(rebatesLevelListDto)  + "==WaterValidBetDto==" + jsonUtil.toJson(rs));

                                if (nonNull(rebatesLevelListDto.getDonateRatio())) {
                                    BigDecimal bonusAmount = adjustScale(rebatesLevelListDto.getDonateRatio().divide(
                                            new BigDecimal(ONE_HUNDRED)).multiply(rs.getValidBet()));
                                    log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()
                                            + "==平台==" + rs.getDepotCode() + "==有效投注==" + rs.getValidBet() + "==反水==" + bonusAmount) ;

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
        log.info("【"+siteCode + "】自动反水==account==" + account.getLoginName()+  "==accountValidBetDetail==waterIds==(" + jsonUtil.toJson(waterIds) +")==结束");
        return waterIds;
    }

    public List<JWaterRebatesLevelDto> getWaterRebatesLevelList(MbrAccount account,JWaterRebatesNeDto dto){

        List<JWaterRebatesAgentDto> agentDtoList = dto.getAgentDtoList();
        List<JWaterRebatesLevelDto> defaultLevelList = dto.getLevelDtoList();
        if(Collections3.isNotEmpty(agentDtoList)){
            Optional<JWaterRebatesAgentDto> cAgentLevelDto = agentDtoList.stream().filter(agentDto ->
                    account.getCagencyId().equals(agentDto.getAgentId())).findFirst();
            if(cAgentLevelDto.isPresent()){
                log.info("getWaterRebatesLevelList==account==" + account.getLoginName() +"==cagencyId==" + cAgentLevelDto.get().getAgentAccount());
                return cAgentLevelDto.get().getLevelDtoList();
            }
            Optional<JWaterRebatesAgentDto> tAgentLevelDto = agentDtoList.stream().filter(agentDto ->
                    account.getTagencyId().equals(agentDto.getAgentId())).findFirst();
            if(tAgentLevelDto.isPresent()){
                log.info("getWaterRebatesLevelList==account==" + account.getLoginName() +"==tagencyId==" + tAgentLevelDto.get().getAgentAccount());
                return tAgentLevelDto.get().getLevelDtoList();
            }
        }
        return defaultLevelList;
    }

    //全部
    private Boolean allAccountValidBet(JWaterRebatesDto dto, MbrAccount account, String startTime, String endTime, OprActActivity activity, List<String> sitePrefix) {
        List<AuditCat> auditCatList = dto.getRebatesNeDto().getAuditCats();
        Boolean isValidBet = false;

        List<WaterValidBetDto> validBetSumList = getValidBet(sitePrefix, account.getLoginName(),
                null, startTime, endTime, null, "0");
        if (validBetSumList.size() == 0) {
            return isValidBet;
        }

        Optional<BigDecimal> validBetSum = validBetSumList.stream()
                .filter(p -> nonNull(p.getValidBet()))
                .map(WaterValidBetDto::getValidBet).reduce(BigDecimal::add);

        WaterRebatesRuleListDto rebatesRuleListDto = getWaterRebatesRuleListDto(account,
                dto, validBetSum.isPresent() ? validBetSum.get() : BigDecimal.ZERO);

        if (isNull(rebatesRuleListDto)) {
            return isValidBet;
        }

        for (AuditCat at : auditCatList) {

            List<Integer> depotIds = at.getDepots().stream().map(AuditDepot::getDepotId).collect(Collectors.toList());
            if (depotIds.size() == 0) {
                continue;
            }
            List<TGmDepot> depotCodes = baseMapper.findDepotCodesById(depotIds);
            List<String> codes = depotCodes.stream().map(TGmDepot::getDepotCode).collect(Collectors.toList());

//            List<String> gameCategorys = at.getCatId() == Constants.EVNumber.one
//                    ? Lists.newArrayList(Constants.depotCatMap.get(at.getCatId()),
//                    Constants.depotCatMap.get(Constants.EVNumber.nine))
//                    : Lists.newArrayList(Constants.depotCatMap.get(at.getCatId()));

            List<String> gameCategorys =Lists.newArrayList(Constants.depotCatMap.get(at.getCatId()));

            List<WaterValidBetDto> validBetDtoList = getValidBet(sitePrefix, account.getLoginName(), gameCategorys, startTime, endTime, codes, "0");
            for (WaterValidBetDto vt : validBetDtoList) {
                BigDecimal bonusAmount = adjustScale(rebatesRuleListDto.getDonateRatio().divide(
                        new BigDecimal(ONE_HUNDRED)).multiply(vt.getValidBet()));

                BigDecimal auditAmount = nonNull(rebatesRuleListDto.getMultipleWater())
                        ? adjustScale(new BigDecimal(rebatesRuleListDto.getMultipleWater())
                        .multiply(bonusAmount)) : BigDecimal.ZERO;

                if (bonusAmount.compareTo(BigDecimal.ZERO) == 1) {
                    insertOprActWater(account, at.getCatId(), vt, activity, depotCodes, bonusAmount, auditAmount);
                    isValidBet = Boolean.TRUE;
                }
            }
        }
        return isValidBet;
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

    public OprActWater generateOprActWater(MbrAccount account, Integer catId, WaterValidBetDto validBetDto,
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
        return actWater;
    }

    private WaterRebatesRuleListDto getWaterRebatesRuleListDto(MbrAccount account, JWaterRebatesDto dto, BigDecimal validBet) {
        List<WaterRebatesRuleListDto> ruleDtos = dto.getRebatesNeDto().getRuleListDtos();
        ruleDtos.sort((r1, r2) -> r2.getValidAmountMax().compareTo(r1.getValidAmountMax()));
        for (WaterRebatesRuleListDto rs : ruleDtos) {
            Boolean isActivity = compareAmount(validBet, rs.getValidAmountMin(), rs.getValidAmountMax());
            if (Boolean.TRUE.equals(isActivity)) {
                return rs;
            }
        }
        log.info("有效投注额不符合参加活动，会员【" + account.getLoginName() + "】，有效投注额【" + validBet + "】");
        return null;
    }

    private Boolean compareAmount(BigDecimal validBet, BigDecimal validAmountMin, BigDecimal validAmountMax) {
        if (validBet.compareTo(validAmountMax) == 0 || validBet.compareTo(validAmountMax) == 1) {
            return Boolean.TRUE;
        }
        if (validBet.compareTo(validAmountMin) == 0 || validBet.compareTo(validAmountMin) == 1) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     *  查询es获取有效投注： 根据线路/会员名/es类别/平台code/时间范围
     * @param sitePrefix        线路前缀
     * @param username          会员名
     * @param gameCategorys     es类别
     * @param startTime         注单开始时间
     * @param endTime           注单结束时间
     * @param depots            平台code
     * @param water             是否已经反水 0未反水 1已反水
     * @return
     */
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
            log.info("【getValidBet】==username==" + username + "==str==" + str );
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
            log.error("【getValidBet】==username==" + username + "==返水获取会员所有投注额失败",e );
        }
        return null;
    }

}
