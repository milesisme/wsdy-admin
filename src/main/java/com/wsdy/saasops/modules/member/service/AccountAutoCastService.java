package com.wsdy.saasops.modules.member.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_10_DATE;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static com.wsdy.saasops.common.utils.DateUtil.getPastDate;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.upgradeBonusCode;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.base.mapper.BaseMapper;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.AffVaildbetMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrActivityLevelMapper;
import com.wsdy.saasops.modules.member.entity.AffVaildbet;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAccountLog;
import com.wsdy.saasops.modules.member.entity.MbrActivityLevel;
import com.wsdy.saasops.modules.member.entity.MbrActivityLevelHighest;
import com.wsdy.saasops.modules.member.mapper.MbrActivityLevelHighestMapper;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dto.JUpgradeBonusDto;
import com.wsdy.saasops.modules.operate.dto.JUpgradeBonusLevelDto;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.wsdy.saasops.modules.system.systemsetting.dao.SysSettingMapper;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountAutoCastService {

    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private ElasticSearchConnection_Read connection;
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private MbrActivityLevelMapper activityLevelMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private MbrAccountLogService accountLogService;
    @Autowired
    private OperateActivityMapper activityMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private OprActBonusMapper oprActBonusMapper;
    @Autowired
    private SysSettingMapper sysSettingMapper;
    @Autowired
    private AffVaildbetMapper affVaildbetMapper;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private OprActActivityService oprActActivityService;
    @Autowired
    private MbrActivityLevelHighestMapper mbrActivityLevelHighestMapper;


    private static final String defaultsdf = "yyyy-MM-dd HH:mm:ss";
    private static final String sdf = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * 	会员自动晋升
     * 
     * @param siteCode
     */
    public void isCastAccountAuto(String siteCode) {
        log.info("AccountAutoCastService----开始计算会员自动晋升:" + siteCode);
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        int isAuto = sysSettingService.findAutomatic(SystemConstants.AUTOMATIC_PROMOTION);
        if (isAuto == Constants.EVNumber.zero) {
            log.info("站点:" + siteCode + "没有开启自动晋升");
            return;
        }

        // 降级计算周期
        SysSetting sysSetting1 = new SysSetting();
        sysSetting1.setSyskey(SystemConstants.DOWNGRADA_DAY);
        SysSetting sysSetting = sysSettingMapper.selectOne(sysSetting1);
        
        // 等级恢复计算周期
        SysSetting sysSettingRecover = new SysSetting();
        sysSettingRecover.setSyskey(SystemConstants.RECOVER_PROMOTION_DAY);
        SysSetting recover = sysSettingMapper.selectOne(sysSettingRecover);

        // 是否开启自动降级
        int isDowngrade = sysSettingService.findAutomatic(SystemConstants.DOWNGRADA_PROMOTION);

        // 活动等级周期统计规则 0无限期，1按日，2按周，3按月
        int actLevelStaticsRule = sysSettingService.findActLevelStaticsRule();
        
        // 查询最近三十天登陆的用户
        List<MbrAccount> accountList = mbrMapper.findAccountAndLevelList();
        
        // 站点标识
        List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(siteCode);

        // 升级计算查询时间
        Map<String, String> timeMap = getStartTimeAndEndTime(actLevelStaticsRule);
        log.info(siteCode + "AccountAutoCastService----新一轮自动升级统计>>>" + timeMap.toString());
        
        // 等级列表
        MbrActivityLevel activityLevel = new MbrActivityLevel();
        activityLevel.setAvailable(Constants.EVNumber.one);
        List<MbrActivityLevel> activityLevelList = activityLevelMapper.select(activityLevel);
        
        // 循环处理每个用户
        if (Collections3.isNotEmpty(accountList) && Collections3.isNotEmpty(activityLevelList)) {
            //等级大到小Collections.sort(activityLevelList, Comparator.comparing(MbrActivityLevel::getAccountLevel).reversed());
            Collections.sort(activityLevelList, Comparator.comparing(MbrActivityLevel::getAccountLevel));
            accountList.stream().forEach(at -> castAccountAuto(at, siteCode, activityLevelList, sitePrefix, timeMap, isDowngrade, sysSetting, recover));
        }
    }

    @Async("dispatcherTaskAsyncExecutor")
    @Transactional
    public void castAccountAuto(MbrAccount account, String siteCode, List<MbrActivityLevel> activityLevelList,
                                List<String> sitePrefix, Map<String, String> timeMap, int isDowngrade, SysSetting downgradeSetting, SysSetting sysSettingRecover) {
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        
        // 当前会员的时间见范围内投注额
        BigDecimal betBigDecimal = getValidVet(sitePrefix, account.getLoginName(), timeMap.get("startTime"), timeMap.get("endTime"));
        BigDecimal downgradeDecimal = betBigDecimal;
        // 当前会员的时间见范围内充值额
        BigDecimal depositBigDecimal = fundMapper.sumFundDepositByAccountId(account.getId(), timeMap.get("startTime"), timeMap.get("endTime"));

        // 会员总的投注额，充值额
        AffVaildbet affVaildbet = getAffVaildbet(account.getLoginName());
        betBigDecimal = betBigDecimal.add(affVaildbet.getTotalvalidbet());
        depositBigDecimal = depositBigDecimal.add(affVaildbet.getTotaldp());

        // 当前用户的历史最高等级，以及最后一次恢复等级的时间
        MbrActivityLevelHighest mbrActivityLevelHighest = mbrActivityLevelHighestMapper.selectOne(new MbrActivityLevelHighest(account.getLoginName()));
        log.info("AccountAutoCastService----" + account.getLoginName() + "会员升级有效投注：" + betBigDecimal + ";存款：" + depositBigDecimal);
        // 循环每个会员等级配置
        for (int i = 0; i < activityLevelList.size(); i++) {
            
            MbrActivityLevel mbrActivityLevel = activityLevelList.get(i);
            // 如果开启降级，并且当前的等级等于会员等级，判断奖及条件
            if (isDowngrade == 1 && account.getAccountLevel().equals(mbrActivityLevel.getAccountLevel())) {
            	// 等于0，不可以再降级
                if (account.getAccountLevel() == 0) {
                    continue;
                }
                // 最后恢复等级时间不超过三十天，不进行掉级，循环下一个等级
                if (mbrActivityLevelHighest != null && mbrActivityLevelHighest.getRecoverTime() != null) {
                	// 查询当前会员是否三十天内被恢复等级，不进行掉级
                	Date recoverTime = mbrActivityLevelHighest.getRecoverTime();
                	// 恢复时间 + 30天 大于当前时间，即：不进行掉级
                	if (DateUtil.getDateAfter(recoverTime, 30).compareTo(new Date()) > 0) {
                		continue;
                	}
                }
                
                // 所有的投注金额
                BigDecimal downgradeBet = downgradeDecimal;
                // 降级计算周期，如果有配置，获取时间范围内的投注金额
                if (nonNull(downgradeSetting) && StringUtils.isNotEmpty(downgradeSetting.getSysvalue())) {
                    int value = Integer.parseInt(downgradeSetting.getSysvalue());
                    if (value > 0) {
                        String startTime = DateUtil.getPastDate(value, DateUtil.FORMAT_10_DATE) + " 00:00:00";
                        String endTime = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
                        // 降级之时间范围的投注额
                        downgradeBet = getValidVet(sitePrefix, account.getLoginName(), startTime, endTime);
                    }
                }
                // 如果降级时间范围的投注额小于累计投注配置
                if (downgradeBet.compareTo(mbrActivityLevel.getDowngradeBet()) == -1) {
                    // 判断用户30个自然日是否有降级记录，如果有则不降级
                    String last30days = DateUtil.format(DateUtil.getDateAfter(new Date(), -30), FORMAT_18_DATE_TIME);
                    MbrAccountLog malexsit = new MbrAccountLog();
                    malexsit.setAccountId(account.getId());
                    malexsit.setCreateTime(last30days);
                    malexsit.setModuleName(MbrAccountLog.ACCOUNT_AUTO_DOWNGRADE);
                    Integer is30Downgrade = mbrMapper.getLastDaysMbrAccountLog(malexsit);
                    if (is30Downgrade > 0) {
                        log.info("AccountAutoCastService----" + account.getLoginName() + "会员30天内有降级记录，本地不进行降级操作");
                    } else {
                        MbrActivityLevel activityLevel = null;
                        if (i == 0) {
                            activityLevel = mbrActivityLevel;
                        } else {
                            activityLevel = activityLevelList.get(i - 1);
                        }
                        // 给用户降级，结束循环，处理下一个用户
                        account.setActLevelId(activityLevel.getId());
                        accountMapper.updateByPrimaryKeySelective(account);
                        accountLogService.accountAutoDowngradeLog(account.getId(), account.getLoginName(), account.getTierName(), activityLevel.getTierName());
                        log.info("AccountAutoCastService----" + account.getLoginName() + "会员降级成功：" + activityLevel.getTierName() + "有效投注" + downgradeBet);
                        break;
                    }
                }
            }
            
            // 升级逻辑开始
            // 如果当前等级 是会员等级的上一级
            if (mbrActivityLevel.getAccountLevel() - account.getAccountLevel() == 1) {
                // 会员历史最高大于当前会员等级,即：等级恢复
                if (mbrActivityLevelHighest != null && mbrActivityLevelHighest.getAccountlevel() > account.getAccountLevel()) {
                	// 恢复等级的计算周期
                	String startTimeRecover = "";
                	// 最后一次掉级的时间
                    String downgradeDate = mbrMapper.getLastDowngradeDays(account.getId(), MbrAccountLog.ACCOUNT_AUTO_DOWNGRADE);
                	// 掉到0级：获取掉级时间到当前时间，只要满足了恢复条件就恢复1级
                	if (account.getAccountLevel() == Constants.EVNumber.zero && StringUtils.isNotEmpty(downgradeDate)) {
                		startTimeRecover = downgradeDate;
                	} else {
                		// 没有调到0级，配置的恢复等级周期
                		int recoverPromotionDay = Integer.parseInt(sysSettingRecover.getSysvalue());
                		startTimeRecover = DateUtil.getPastDate(recoverPromotionDay, DateUtil.FORMAT_10_DATE) + " 00:00:00";
                	}
                	
                	// 查询恢复等级recoverPromotionDay天内的用户投注额
                	String endTimeRecover = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
                	// 恢复期间的投注额
                	BigDecimal betRecover = getValidVet(sitePrefix, account.getLoginName(), startTimeRecover, endTimeRecover);
                	if (betRecover == null) {
                		betRecover = BigDecimal.ZERO;
                	}
                	// 如果恢复期间的投注额 大于等于 恢复的配置投注额，给当前用户升级
                	if (betRecover.compareTo(mbrActivityLevel.getRecoverBet()) != -1) {
                		updateAccountLevel(account, mbrActivityLevel, true);
                		// 更新最新的恢复等级时间，30天内不掉级
                		mbrActivityLevelHighestMapper.updateRecoverTime(account.getLoginName());
                		log.info("AccountAutoCastService----等级恢复会员:{} ,{}天内投注额为:{},恢复的条件额度为:{},原来最高等级为:{}, 恢复升级到:{}", 
                				account.getLoginName(), sysSettingRecover.getSysvalue(), betRecover, mbrActivityLevel.getRecoverBet(),
                				mbrActivityLevelHighest.getAccountlevel(), mbrActivityLevel.getAccountLevel());
                		break;
                	}
                }
                // 会员历史最高等级不大于当前会员等级,即：升级逻辑
                else {
                	Boolean isSucceed = Boolean.FALSE;
                	// 投注条件是否满足
                	Boolean isValidbet = isValidbetPass(mbrActivityLevel, betBigDecimal);
                	// 充值条件是否满足
                	Boolean isDeposit = isDepositPass(mbrActivityLevel, depositBigDecimal);
                	
                	// 晋级条件 满足条件： 0 全部 1任意
                	if (mbrActivityLevel.getConditions() == Constants.EVNumber.zero) {
                		if (mbrActivityLevel.getPromoteSign() == Constants.EVNumber.two &&
                				Boolean.TRUE.equals(isValidbet) && Boolean.TRUE.equals(isDeposit)) {
                			isSucceed = Boolean.TRUE;
                		}
                	}
                	if (mbrActivityLevel.getConditions() == Constants.EVNumber.one) {
                		if (mbrActivityLevel.getPromoteSign() == Constants.EVNumber.two &&
                				(Boolean.TRUE.equals(isValidbet) || Boolean.TRUE.equals(isDeposit))) {
                			isSucceed = Boolean.TRUE;
                		}
                	}
                	
                	// 晋级条件勾选状态：勾选 0 累计投注 1 累计充值 2全部勾选
                	if (mbrActivityLevel.getPromoteSign() == Constants.EVNumber.zero
                			&& Boolean.TRUE.equals(isValidbet)) {
                		isSucceed = Boolean.TRUE;
                	}
                	if (mbrActivityLevel.getPromoteSign() == Constants.EVNumber.one
                			&& Boolean.TRUE.equals(isDeposit)) {
                		isSucceed = Boolean.TRUE;
                	}
                	// 满足条件，升级
                	if (Boolean.TRUE.equals(isSucceed)) {
                		// 升级
                		updateAccountLevel(account, mbrActivityLevel, false);
                		// 新增/更新 到用户历史最高等级记录
                		mbrActivityLevelHighestMapper.updateLevelHighest(account.getLoginName(), mbrActivityLevel.getAccountLevel());
                		accountUpgradeBonus(account, mbrActivityLevel);
                		log.info("AccountAutoCastService----会员升级" + account.getLoginName() + mbrActivityLevel.getTierName() +
                				"sign:" + mbrActivityLevel.getPromoteSign() + "cond:" + mbrActivityLevel.getConditions());
                		break;
                	}
                }
            }
        }
    }

    public AffVaildbet getAffVaildbet(String loginName) {
        AffVaildbet affVaildbet = new AffVaildbet();
        affVaildbet.setLoginName(loginName);
        AffVaildbet vaildbet = affVaildbetMapper.selectOne(affVaildbet);
        affVaildbet.setTotalvalidbet(BigDecimal.ZERO);
        affVaildbet.setTotaldp(BigDecimal.ZERO);
        if (nonNull(vaildbet)) {
            affVaildbet.setTotalvalidbet(nonNull(vaildbet.getTotalvalidbet()) ? vaildbet.getTotalvalidbet() : BigDecimal.ZERO);
            affVaildbet.setTotaldp(nonNull(vaildbet.getTotaldp()) ? vaildbet.getTotaldp() : BigDecimal.ZERO);
        }
        return affVaildbet;
    }

    private void updateAccountLevel(MbrAccount account, MbrActivityLevel mbrActivityLevel, boolean isRecover) {
        if (!account.getActLevelId().equals(mbrActivityLevel.getId())){ // 只有当有变更才更新
            account.setActLevelId(mbrActivityLevel.getId());
            accountMapper.updateByPrimaryKeySelective(account);
            accountLogService.accountAutoLog(account.getId(), account.getLoginName(), account.getTierName(), mbrActivityLevel.getTierName(), isRecover);
        }

    }

    public void accountUpgradeBonus(MbrAccount account, MbrActivityLevel activityLevel) {

        //活动黑名单会员
        if(oprActActivityService.isBlackList(account.getId(), TOpActtmpl.upgradeBonusCode)||oprActActivityService.isBlackList(account.getId(), TOpActtmpl.allActivityCode)){
            log.info("AccountAutoCastService----" + account.getLoginName()+"是自动升级活动黑名单会员");
            return;
        }

        //判断是否存在vip活动代理黑名单
        if (oprActActivityService.valAgentBackList(account,TOpActtmpl.upgradeBonusCode)){
            log.info("AccountAutoCastService----" + account.getLoginName()+"上级代理存在自动升级黑名单");
            return;
        }
        //判断是否存在所有活动黑名单
        if (oprActActivityService.valAgentBackList(account,TOpActtmpl.allActivityCode)){
            log.info("AccountAutoCastService----" + account.getLoginName()+"上级代理存在所有活动黑名单");
            return;
        }
        

        OprActActivity activity = new OprActActivity();
        activity.setUseStart(getPastDate(Constants.EVNumber.one, FORMAT_10_DATE));
        activity.setTmplCode(upgradeBonusCode);
        List<OprActActivity> actActivities = activityMapper.findWaterActivity(activity);
        if (Collections3.isNotEmpty(actActivities)) {
            OprActActivity actActivity = actActivities.get(0);
            JUpgradeBonusDto dto = jsonUtil.fromJson(actActivity.getRule(), JUpgradeBonusDto.class);
            if (isNull(dto)) {
                return;
            }
            // 申请条件校验
            String isAccountMsg = oprActActivityCastService.checkoutAccountMsg(account,
                    Constants.EVNumber.zero, dto.getIsName(),
                    dto.getIsBank(), dto.getIsMobile(), false, false);
            if (StringUtils.isNotEmpty(isAccountMsg)) {
                log.info("AccountAutoCastService----" + account.getLoginName() + "晋级优惠错误" + isAccountMsg);
                return;
            }
            // 是否领取校验
            Boolean isBonus = checkoutUpgradeBonus(actActivity, account, activityLevel);
            if (!isBonus) {
                log.info("AccountAutoCastService----" + account.getLoginName() + "晋级优惠错误已领取" );
                return;
            }
            Optional<JUpgradeBonusLevelDto> levelDto = dto.getRuleScopeDtos().stream().filter(
                    d -> d.getActLevelId().equals(activityLevel.getId())).findAny();
            if (levelDto.isPresent()) {
                JUpgradeBonusLevelDto bonusLevelDto = levelDto.get();
                if (nonNull(bonusLevelDto.getDonateAmount())) {
                    accountMemDayGift(bonusLevelDto, actActivity, account, null);
                }
            }
        }
    }

    // 会员日生成红利记录
    private void accountMemDayGift(JUpgradeBonusLevelDto ruleDto, OprActActivity actActivity, MbrAccount account, String ip) {
        OprActBonus bonus = oprActActivityCastService.setOprActBonus(account.getId(), account.getLoginName(), actActivity.getId(),
                null, null, actActivity.getRuleId());
        bonus.setScope(null);
        bonus.setCreateUser(account.getLoginName());
        bonus.setIp(ip);
        bonus.setDevSource(account.getLoginSource());
        bonus.setDiscountAudit(new BigDecimal(ruleDto.getMultipleWater().intValue()));
        bonus.setBonusAmount(ruleDto.getDonateAmount());
        bonus.setAuditAmount(auditAccountService.getAddAuditAmount(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        bonus.setSource(Constants.EVNumber.one);
        bonus.setCreateUser(Constants.SYSTEM_USER);
        bonus.setActLevelId(account.getActLevelId());
        oprActBonusMapper.insert(bonus);
        if (actActivity.getIsAudit() == Constants.EVNumber.zero) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            oprActActivityCastService.auditOprActBonus(bonus,
                    OrderConstants.ACTIVITY_SJL, actActivity.getActivityName(),
                    Boolean.TRUE);
        }
    }

    private Boolean checkoutUpgradeBonus(OprActActivity actActivity, MbrAccount mbrAccount, MbrActivityLevel mbrActivityLevel) {
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(actActivity.getId());
        bonus.setAccountId(mbrAccount.getId());
        bonus.setActLevelId(mbrActivityLevel.getId());
        int count = oprActBonusMapper.selectCount(bonus);
        if (count > 0) {
            return false;
        }
        return true;
    }


    public Map<String, String> getStartTimeAndEndTime(Integer rule) {
    	Map<String, String> result = new HashMap<>(4);
        String startTime = null;
        String endTime = null;
        if (Constants.EVNumber.one == rule) {
            startTime = DateUtil.getPastDate(1, DateUtil.FORMAT_10_DATE) + " 00:00:00";
            endTime = DateUtil.getTodayStart(DateUtil.FORMAT_10_DATE);
        } else if (Constants.EVNumber.two == rule) {
            startTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME, 0, 0);//本周第一天
            endTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME, -1, 0);//下周第一天
        } else if (Constants.EVNumber.three == rule) {
            startTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME, 0, 0);//本月第一天
            endTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME, -1, 0);//下月第一天
        }
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        
        return result;
    }

    private Boolean isValidbetPass(MbrActivityLevel activityLevel, BigDecimal betBigDecimal) {
    	// 设置的等级条件投注额为空，或者用户投注额小于配置的投注额返回 false
        if (isNull(activityLevel.getValidbetMin()) || betBigDecimal.compareTo(activityLevel.getValidbetMin()) == -1) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private Boolean isDepositPass(MbrActivityLevel activityLevel, BigDecimal depositBigDecimal) {
    	// 设置的等级条件充值额为空，或者用户充值额小于配置的投注额返回 false
        if (isNull(activityLevel.getDepositMin()) || depositBigDecimal.compareTo(activityLevel.getDepositMin()) == -1) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public BigDecimal getValidVet(List<String> sitePrefix, String username, String startTime, String endTime) {

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
                                    AggregationBuilders.sum("originalValidBet").field("originalValidBet"));

            String str = searchRequestBuilder.toString();
            Response response = connection.restClient_Read.performRequest(
                    "GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/"
                            + ElasticSearchConstant.REPORT_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"),
                    new NStringEntity(str, ContentType.APPLICATION_JSON));

            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            Map validBet = (Map) ((Map) map.get("aggregations")).get("originalValidBet");
            if (Objects.nonNull(validBet)) {
                return new BigDecimal(validBet.get("value").toString());
            }
        } catch (Exception e) {
            log.error("AccountAutoCastService----自动晋升，获取会员所有投注额失败，username:" + username , e);
        }
        return BigDecimal.ZERO;
    }

    @Async("dispatcherTaskAsyncExecutor")
    @Transactional
    public void accountAutoOffline(String siteCode) {
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        List<MbrAccount> accountList = mbrMapper.findAccountOnlineOutOfTime();
        if (Collections3.isNotEmpty(accountList)) {
            List<Integer> accountIds = accountList.stream().map(MbrAccount::getId).collect(Collectors.toList());
            log.info("批量踢线在线超时会员======accountAutoOffline，" + accountIds.toString());
            mbrMapper.batchUpdateOnline(accountIds);
            mbrMapper.batchUpdateLoginOutTime(accountIds);
            log.info("完成批量踢线=======accountAutoOffline");
        }
    }

}
