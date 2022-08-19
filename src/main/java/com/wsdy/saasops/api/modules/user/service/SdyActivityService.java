package com.wsdy.saasops.api.modules.user.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.wsdy.saasops.api.modules.user.dto.SdyActivity.*;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ActivityConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.log.mapper.LogMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrActivityLevelMapper;
import com.wsdy.saasops.modules.member.entity.AffVaildbet;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrActivityLevel;
import com.wsdy.saasops.modules.member.mapper.AuditMapper;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.AccountAutoCastService;
import com.wsdy.saasops.modules.member.service.AccountWaterCastService;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dto.*;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.TGmCat;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.operate.mapper.SdyActivityMapper;
import com.wsdy.saasops.modules.operate.service.*;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import com.wsdy.saasops.modules.task.dao.TaskConfigMapper;
import com.wsdy.saasops.modules.task.dto.TaskCenterDto;
import com.wsdy.saasops.modules.task.entity.TaskBonus;
import com.wsdy.saasops.modules.task.entity.TaskConfig;
import com.wsdy.saasops.modules.task.mapper.TaskMapper;
import com.wsdy.saasops.modules.task.service.TaskAccountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.constants.OrderConstants.HR_TASK_ACTIVITY;
import static com.wsdy.saasops.common.constants.OrderConstants.ZL_TASK_ACTIVITY;
import static com.wsdy.saasops.common.constants.SystemConstants.DOWNGRADA_DAY;
import static com.wsdy.saasops.common.constants.SystemConstants.DOWNGRADA_PROMOTION;
import static com.wsdy.saasops.common.utils.DateUtil.*;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
@Transactional
public class SdyActivityService {

    @Autowired
    private TaskAccountService accountService;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private OprActActivityService actActivityService;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private MbrActivityLevelMapper activityLevelMapper;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private AccountWaterCastService accountWaterCastService;
    @Autowired
    private SdyActivityMapper sdyActivityMapper;
    @Autowired
    private OprApplyfirstDepositService applyfirstDepositService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private TGmGameService tGmGameService;
    @Autowired
    private OperateActivityMapper activityMapper;
    @Autowired
    private OprBirthdayActivityService birthdayActivityService;
    @Autowired
    private OprDepositSentService oprDepositSentService;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private AccountAutoCastService autoCastService;
    @Autowired
    private LogMapper logMapper;
    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private AuditMapper auditMapper;
    @Autowired
    private TaskConfigMapper taskConfigMapper;

    public TaskCenterDto accountSignInfo(Integer accountId) {
        // 获得任务配置及处理后的返回数据
        TaskCenterDto taskCenterDto = accountService.taskCenter(accountId, HR_TASK_ACTIVITY);
        // 获得获取历史奖励数据
        if (nonNull(taskCenterDto)) {
            List<TaskConfig> taskConfigs = taskCenterDto.getTaskConfigs();
            if (Collections3.isEmpty(taskConfigs)) {
                return taskCenterDto;
            }
            if (taskConfigs.size() > 0) {
                taskConfigs.stream().forEach(ts -> {
                    List<TaskBonus> taskBonuses = Lists.newArrayList();
                    String day = ts.getRewardDto().getDay();
                    if (StringUtils.isNotEmpty(day)) {  // 有领取过活跃奖励，则获取最近一次周期内历史奖励数据
                        taskBonuses = taskMapper.findHRTaskBonusList(accountId, ts.getId(), ts.getRewardDto().getNum());
                    }
                    if (taskBonuses.size() > 0) {
                        // 历史奖励数据倒序排序
                        Collections.sort(taskBonuses, Comparator.comparing(TaskBonus::getNum).reversed());
                    }
                    ts.getRewardDto().setTaskBonuses(taskBonuses);
                });
            }
        }
        return taskCenterDto;
    }

    public TaskCenterDto completeMaterial(Integer accountId) {
        return accountService.taskCenter(accountId, ZL_TASK_ACTIVITY);
    }

    public OprActActivity firstDeposit(Integer accountId, Byte terminal) {
        PageUtils pageUtils = actActivityService.webActivityList(1, 10, null,
                accountId, terminal, Constants.EVNumber.one, null, preferentialCode, null, null);
        OprActActivity oprActActivity = new OprActActivity();
        if (nonNull(pageUtils) && pageUtils.getList().size() > 0) {
            MbrAccount account = mbrMapper.findMbrAccount(accountId, null, null);
            oprActActivity = pageUtils.getList().size() > 0 ? (OprActActivity) pageUtils.getList().get(0) : null;
            Byte buttonShow = (byte) verificationActActivity(oprActActivity, account);
            oprActActivity.setButtonShow(buttonShow);
        }
        return oprActActivity;
    }

    private int verificationActActivity(OprActActivity actActivity, MbrAccount account) {
        JPreferentialDto dto = jsonUtil.fromJson(actActivity.getRule(), JPreferentialDto.class);
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId(), dto.getScope());
        if (isNull(ruleScopeDto)) {
            return Constants.EVNumber.two;
        }
        if (ruleScopeDto.getDepositType() == Constants.EVNumber.three) {
            Boolean isSign = isLatestWeek(parse(account.getRegisterTime()), new Date(), ruleScopeDto.getDay());
            if (Boolean.FALSE.equals(isSign)) {
                return Constants.EVNumber.two;
            }
        }
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(account, dto.getScope(), ruleScopeDto.getIsName(),
                ruleScopeDto.getIsBank(), ruleScopeDto.getIsMobile(), ruleScopeDto.getIsMail(), ruleScopeDto.getIsApp());
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(isAccountMsg)) {
            return Constants.EVNumber.two;
        }
        return applyfirstDepositService.checkoutFirstDepositBonus(ruleScopeDto, actActivity.getId(), account.getId());
    }

    public VipInfoDto accountVipInfo(Integer accountId) {
        VipInfoDto vipInfoDto = new VipInfoDto();
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        if (nonNull(account)) {

            MbrActivityLevel activityLevel = activityLevelMapper.selectByPrimaryKey(account.getActLevelId());
            vipInfoDto.setLoginName(account.getLoginName());
            vipInfoDto.setBirthday(account.getBirthday());
            vipInfoDto.setTierName(activityLevel.getTierName());
            vipInfoDto.setAccountLevel(activityLevel.getAccountLevel());

            BigDecimal validbet = taskMapper.findValidbetTsk(account.getLoginName(), null);
            BigDecimal depositBigDecimal = fundMapper.sumFundDepositByAccountId(account.getId(), null, null);

            AffVaildbet affVaildbet = autoCastService.getAffVaildbet(account.getLoginName());
            validbet = validbet.add(affVaildbet.getTotalvalidbet());
            depositBigDecimal = depositBigDecimal.add(affVaildbet.getTotaldp());
            vipInfoDto.setValidbet(validbet);
            vipInfoDto.setDepositAmount(depositBigDecimal);

            int downgradePromotion = sysSettingService.findAutomatic(DOWNGRADA_PROMOTION);
            vipInfoDto.setDowngradePromotion(downgradePromotion);
            int downgradePromotionDay = sysSettingService.findAutomatic(DOWNGRADA_DAY);
            vipInfoDto.setDowngradePromotionDay(downgradePromotionDay);

            List<ActivityLevelDto> activityLevels = sdyActivityMapper.findAcocountActivityLevel(null);
            vipInfoDto.setActivityLevelList(activityLevels);

            vipInfoDto.setActivityLevelCatDtos(setActivityLevelCatDto(activityLevels));
            vipInfoDto.setUpgradeBonusLevelDtos(getUpgradeBonus(activityLevels));
            vipInfoDto.setMonthlyBonus(getMonthlyBonus(activityLevels));
            vipInfoDto.setBirthdayBonusList(getBirthdayBonusList(activityLevels));

            // 获取<豪礼赠送>与<活动规则与详情>
            TaskConfig hlzs = taskConfigMapper.selectOne(new TaskConfig(){{
                setFinancialCode(OrderConstants.HLZS_TASK_ACTIVITY);
            }});
            if (hlzs != null) {
                vipInfoDto.setHlzs(hlzs.getRule());
            }
            TaskConfig hdgz = taskConfigMapper.selectOne(new TaskConfig(){{
                setFinancialCode(OrderConstants.HDGZ_TASK_ACTIVITY);
            }});
            if (hdgz != null) {
                vipInfoDto.setHdgz(hdgz.getRule());
            }
        }
        return vipInfoDto;
    }

    private List<JbirthdayInfoDto> getBirthdayBonusList(List<ActivityLevelDto> activityLevels) {
        SdyBirthdayDto birthdayDto = new SdyBirthdayDto();
        birthdayDto.setIsBirthday(Boolean.FALSE);
        OprActActivity actActivity = getOprActActivity(birthdayCode);
        if (nonNull(actActivity)) {
            JbirthdayDto dto = jsonUtil.fromJson(actActivity.getRule(), JbirthdayDto.class);
            List<JbirthdayInfoDto> upgradeBonusLevelDtos = Lists.newArrayList();
            dto.getRuleScopeDtos().stream().forEach(d -> {
                Optional<ActivityLevelDto> levelDtoOptional = activityLevels.stream().filter(e ->
                        e.getId().equals(d.getActLevelId())).findAny();
                if (levelDtoOptional.isPresent()) {
                    upgradeBonusLevelDtos.add(d);
                }
            });

            upgradeBonusLevelDtos.forEach(us -> {
                Optional<ActivityLevelDto> levelDtoOptional = activityLevels.stream().filter(e ->
                        e.getId().equals(us.getActLevelId())).findAny();
                us.setAccountLevel(levelDtoOptional.get().getAccountLevel());
                us.setTierName(levelDtoOptional.get().getTierName());
            });
            Collections.sort(upgradeBonusLevelDtos, Comparator.comparing(JbirthdayInfoDto::getAccountLevel));
            return upgradeBonusLevelDtos;
        }
        return null;
    }

    private VipMonthlyBonusDto getMonthlyBonus(List<ActivityLevelDto> activityLevels) {
        OprActActivity actActivity = getOprActActivity(vipRedenvelopeCode);
        if (nonNull(actActivity)) {
            MemDayGiftDto giftDto = jsonUtil.fromJson(actActivity.getRule(), MemDayGiftDto.class);
            if (nonNull(giftDto)) {
                VipMonthlyBonusDto bonusDto = new VipMonthlyBonusDto();
                bonusDto.setScope(giftDto.getScope());
                if (giftDto.getScope() == 0) {
                    List<MemDayRuleScopeDto> ruleScopeDtoList = giftDto.getRuleScopeDtos();
                    bonusDto.setRuleScopeDtoList(ruleScopeDtoList);
                } else {
                    List<MemDayRuleScopeDto> scopeDtoList = Lists.newArrayList();
                    giftDto.getRuleScopeDtos().stream().forEach(d -> {
                        Optional<ActivityLevelDto> levelDtoOptional = activityLevels.stream().filter(e ->
                                e.getId().equals(d.getActLevelId())).findAny();
                        if (levelDtoOptional.isPresent()) {
                            scopeDtoList.add(d);
                        }
                    });
                    scopeDtoList.forEach(us -> {
                        Optional<ActivityLevelDto> levelDtoOptional = activityLevels.stream().filter(e ->
                                e.getId().equals(us.getActLevelId())).findAny();
                        us.setAccountLevel(levelDtoOptional.get().getAccountLevel());
                        us.setTierName(levelDtoOptional.get().getTierName());
                    });
                    Collections.sort(scopeDtoList, Comparator.comparing(MemDayRuleScopeDto::getAccountLevel));
                    bonusDto.setRuleScopeDtoList(scopeDtoList);
                }
                return bonusDto;
            }
        }
        return null;
    }

    public List<JUpgradeBonusLevelDto> getUpgradeBonus(List<ActivityLevelDto> activityLevels) {
        OprActActivity actActivity = getOprActActivity(upgradeBonusCode);
        if (nonNull(actActivity)) {
            JUpgradeBonusDto dto = jsonUtil.fromJson(actActivity.getRule(), JUpgradeBonusDto.class);
            if (nonNull(dto)) {
                List<JUpgradeBonusLevelDto> upgradeBonusLevelDtos = Lists.newArrayList();
                dto.getRuleScopeDtos().stream().forEach(d -> {
                    Optional<ActivityLevelDto> levelDtoOptional = activityLevels.stream().filter(e ->
                            e.getId().equals(d.getActLevelId())).findAny();
                    if (levelDtoOptional.isPresent()) {
                        upgradeBonusLevelDtos.add(d);
                    }
                });
                upgradeBonusLevelDtos.forEach(us -> {
                    Optional<ActivityLevelDto> levelDtoOptional = activityLevels.stream().filter(e ->
                            e.getId().equals(us.getActLevelId())).findAny();
                    us.setAccountLevel(levelDtoOptional.get().getAccountLevel());
                    us.setTierName(levelDtoOptional.get().getTierName());
                });
                Collections.sort(upgradeBonusLevelDtos, Comparator.comparing(JUpgradeBonusLevelDto::getAccountLevel));
                return upgradeBonusLevelDtos;
            }
        }
        return null;
    }

    public OprActActivity getOprActActivity(String tmplCode) {
        OprActActivity activity = new OprActActivity();
        activity.setUseStart(getCurrentDate(FORMAT_10_DATE));
        activity.setTmplCode(tmplCode);
        List<OprActActivity> actActivities = activityMapper.findWaterActivity(activity);
        if (Collections3.isNotEmpty(actActivities)) {
            return actActivities.get(0);
        }
        return null;
    }

    private List<ActivityLevelCatDto> setActivityLevelCatDto(List<ActivityLevelDto> activityLevels) {
        List<ActivityLevelCatDto> levelCatDtos = Lists.newArrayList();
        List<OprActActivity> activities = accountWaterCastService.getOprActActivitys(null);
        if (Collections3.isNotEmpty(activities)) {
            List<TGmCat> gmCatList = tGmGameService.findGameType();
            JWaterRebatesDto dto = new Gson().fromJson(activities.get(0).getRule(), JWaterRebatesDto.class);
            if (nonNull(dto) && nonNull(dto.getRebatesNeDto())) {
                List<JWaterRebatesLevelDto> rebatesLevelDtos = Lists.newArrayList();
                dto.getRebatesNeDto().getLevelDtoList().stream().forEach(d -> {
                    Optional<ActivityLevelDto> levelDtoOptional = activityLevels.stream().filter(e ->
                            e.getId().equals(d.getAccountLevel())).findAny();
                    if (levelDtoOptional.isPresent()) {
                        rebatesLevelDtos.add(d);
                    }
                });

                Collections.sort(rebatesLevelDtos, Comparator.comparing(JWaterRebatesLevelDto::getAccountLevel));
                for (JWaterRebatesLevelDto rebatesLevelDto : rebatesLevelDtos) {

                    Map<Integer, List<JWaterRebatesLevelListDto>> rebatesLevelListGroupingBy =
                            rebatesLevelDto.getRebatesLevelListDtos().stream().collect(
                                    Collectors.groupingBy(JWaterRebatesLevelListDto::getCatId));
                    for (Integer catIdKey : rebatesLevelListGroupingBy.keySet()) {
                        ActivityLevelCatDto levelCatDto = new ActivityLevelCatDto();
                        levelCatDto.setAccountLevelId(rebatesLevelDto.getAccountLevel());
                        List<JWaterRebatesLevelListDto> rebatesLevelList = rebatesLevelListGroupingBy.get(catIdKey);
                        JWaterRebatesLevelListDto rebatesLevelListDto = rebatesLevelList.stream().max(Comparator.comparing(JWaterRebatesLevelListDto::getDonateRatio)).get();
                        levelCatDto.setDonateRatio(rebatesLevelListDto.getDonateRatio());

                        TGmCat tGmCat = gmCatList.stream().filter(e ->
                                e.getId().equals(rebatesLevelListDto.getCatId())).findAny().get();
                        levelCatDto.setCatName(tGmCat.getCatName());

                        Optional<ActivityLevelDto> levelDtoOptional = activityLevels.stream().filter(e ->
                                e.getId().equals(rebatesLevelDto.getAccountLevel())).findAny();
                        levelCatDto.setTierName(levelDtoOptional.get().getTierName());
                        levelCatDtos.add(levelCatDto);
                    }
                }
            }
        }
        return levelCatDtos;
    }

    public SdyBirthdayDto accountBirthday(Integer accountId) {
        SdyBirthdayDto birthdayDto = new SdyBirthdayDto();
        birthdayDto.setIsBirthday(Boolean.FALSE);
        OprActActivity actActivity = getOprActActivity(birthdayCode);
        if (nonNull(actActivity)) {
            MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
            JbirthdayDto dto = jsonUtil.fromJson(actActivity.getRule(), JbirthdayDto.class);
            if (nonNull(dto)) {
                String day = getCurrentDate(FORMAT_5_DATE);
                if (StringUtils.isEmpty(account.getBirthday())) {
                    return birthdayDto;
                }
                String birthday = account.getBirthday().substring(5, 10);
                if (!day.equals(birthday)) {
                    return birthdayDto;
                }
                if (nonNull(dto.getDay()) && dto.getDay() > 0) {
                    int num = daysBetween(account.getRegisterTime(), getCurrentDate(FORMAT_18_DATE_TIME));
                    if (num <= dto.getDay()) {
                        return birthdayDto;
                    }
                }
                Boolean isBonus = birthdayActivityService.checkoutBirthdayBonus(actActivity, account);
                if (!isBonus) {
                    return birthdayDto;
                }
                JbirthdayInfoDto ruleScopeDto = birthdayActivityService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId());
                if (isNull(ruleScopeDto)) {
                    return birthdayDto;
                }
                birthdayDto.setIsBirthday(Boolean.TRUE);
                birthdayDto.setActivityId(actActivity.getId());
                birthdayDto.setDonateAmount(ruleScopeDto.getDonateAmount());
                birthdayDto.setMultipleWater(ruleScopeDto.getMultipleWater());
            }
        }
        return birthdayDto;
    }

    public OprActActivity accountVipPrivileges(Integer accountId) {
        // 查询VIP晋级优惠活动
        PageUtils pageUtils = actActivityService.webActivityList(1, 10, null,
                accountId, null, Constants.EVNumber.one, null, vipPrivilegesCode, null, null);

        OprActActivity oprActActivity = new OprActActivity();
        if (nonNull(pageUtils) && pageUtils.getList().size() > 0) {
            MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
            oprActActivity = pageUtils.getList().size() > 0 ? (OprActActivity) pageUtils.getList().get(0) : null;
            // 校验规则，获取按钮状态，此处只校验领取次数+优惠稽核，未达到/无规则 返回立即领取1 ， 达到限制返回3 已领取   4 优惠稽核未通过 灰化的立即领取
            Byte buttonShow = (byte) verificationVipPrivileges(oprActActivity, account);
            oprActActivity.setButtonShow(buttonShow);
            oprActActivity.setActLevelId(account.getActLevelId());
        }
        return oprActActivity;
    }

    private int verificationVipPrivileges(OprActActivity actActivity, MbrAccount account) {
        JDepositSentDto dto = jsonUtil.fromJson(actActivity.getRule(), JDepositSentDto.class);
        // 范围是层级会员的处理
        if (dto.getScope() == 1) {
            // 所有等级数据
            List<ActivityLevelDto> activityLevels = sdyActivityMapper.findAcocountActivityLevel(null);
            List<RuleScopeDto> rebatesLevelDtos = Lists.newArrayList();
            dto.getRuleScopeDtos().stream().forEach(d -> {
                Optional<ActivityLevelDto> levelDtoOptional = activityLevels.stream().filter(e ->
                        e.getId().equals(d.getActLevelId())).findAny();
                if (levelDtoOptional.isPresent()) {
                    rebatesLevelDtos.add(d);
                }
            });
            // 设置每个层级的等级别名/等级
            rebatesLevelDtos.forEach(us -> {
                Optional<ActivityLevelDto> levelDtoOptional = activityLevels.stream().filter(e ->
                        e.getId().equals(us.getActLevelId())).findAny();
                us.setAccountLevel(levelDtoOptional.get().getAccountLevel());
                us.setTierName(levelDtoOptional.get().getTierName());
            });
            // 保存层级规则
            actActivity.setRule(JSON.toJSONString(dto));
        }
        // 获取会员所在等级的层级规则
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId(), dto.getScope());
        if (isNull(ruleScopeDto)) {
            return Constants.EVNumber.one;
        }
        Integer actLevelId = null;  // 全部会员规则：为null
        if (dto.getScope() == Constants.EVNumber.one) {
            actLevelId = account.getActLevelId();   // 层级会员规则：为会员的层级
        }
        // 校验统计周期内是否已领取达到次数限制
        Boolean isDepositSent = oprDepositSentService.checkoutDepositSent(ruleScopeDto, actActivity.getId(), account.getId(), actLevelId);
        // 已达到领取限制
        if (Boolean.FALSE.equals(isDepositSent)) {
            return Constants.EVNumber.three;
        }

        // 校验当前是否存在优惠稽核(反水除外，含任务ruleId 888888结尾)
        Integer count = auditMapper.findAuditAccountWithoutWater(account.getId());  // 获取会员当前除反水外，未通过的优惠稽核个数
        if (count.intValue() > 0) {
            return Constants.EVNumber.four;
        }
        return Constants.EVNumber.one;
    }


    public RedPacketRainRespDto redPacketRainInfo() {
        // 查询当下配置的有效的红包雨活动
        OprActActivity actActivity = getOprActActivity(redPacketRainCode);
        if (isNull(actActivity)) {
            throw new R200Exception("活动暂未开启");
        }

        // 获得红包雨规则
        RedPacketRainDto dto = jsonUtil.fromJson(actActivity.getRule(), RedPacketRainDto.class);
        // 校验
        if (Objects.isNull(dto)) {
            throw new R200Exception("规则配置错误！");
        }
        // 设置返回值
        RedPacketRainRespDto ret = new RedPacketRainRespDto();
        ret.setStartTime(dto.getStartTime());   // 活动当日开始时间
        ret.setEndTime(dto.getEndTime());       // 活动当日结束时间
        ret.setValidDates(dto.getValidDates()); // 活动日期 按周1-7 ,如1,5,7  周一周五周日

        return ret;
    }

    public RedPacketRainRespDto redPacketClick(Integer accountId, String ip) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        //校验会员账号是否被添加进所有活动黑名单
        if (actActivityService.isBlackList(accountId, TOpActtmpl.allActivityCode)) {
            throw new R200Exception("您不符合参与红包雨资格");
        }
        //校验会员对应的代理是否被添加进所有活动黑名单
        if (actActivityService.valAgentBackList(mbrAccount, TOpActtmpl.allActivityCode)) {
            throw new R200Exception("您不符合参与红包雨资格");
        }
        RedPacketRainRespDto ret = new RedPacketRainRespDto();

        // 红包活动是否开启
        OprActActivity actActivity = getOprActActivity(redPacketRainCode);
        if (isNull(actActivity)) {
            throw new R200Exception("活动暂未开启");
        }

        // 获得红包雨规则
        RedPacketRainDto dto = jsonUtil.fromJson(actActivity.getRule(), RedPacketRainDto.class);
        if (Objects.isNull(dto)) {
            throw new R200Exception("规则配置错误！");
        }

        // 校验
        // 星期校验
        Boolean dayCheck = checkoutRedPacketDay(dto);
        if (!dayCheck) {
            throw new R200Exception("活动暂未开启!");
        }
        // 时间校验
        Boolean timecheck = checkoutRedPacketTime(dto);
        if (!timecheck) {
            throw new R200Exception("活动暂未开启!!");
        }

        // 档位
        List<RedPacketRainRuleDto> redPacketRainRuleDtos = dto.getRedPacketRainRuleDtos();
        if (Collections3.isEmpty(redPacketRainRuleDtos)) {
            throw new R200Exception("规则配置错误！");
        }

        redPacketRainRuleDtos.sort((r1, r2) -> r2.getMinAmount().compareTo(r1.getMinAmount()));//按最小存款倒序

        // 当日累积存款
        BigDecimal depositAmount = fundMapper.sumFundDepositByAccountId(accountId,
                DateUtil.getTodayStart(FORMAT_10_DATE), DateUtil.getTodayEnd(FORMAT_10_DATE));

        // 筛选出满足存款要求的规则
        List<RedPacketRainRuleDto> depositRules = redPacketRainRuleDtos.stream().filter(
                rule -> depositAmount.compareTo(rule.getMinAmount()) >= 0
        ).collect(Collectors.toList());

        if (Collections3.isEmpty(depositRules)) {
            // 存款不满足
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // 最近三天是否连续登陆
        String endDay = DateUtil.getCurrentDate(FORMAT_10_DATE);
        String startDay = DateUtil.getPastDate(Constants.EVNumber.two, FORMAT_10_DATE);
        Integer loginDays = logMapper.loginDays(accountId, startDay, endDay);

        // 总可抽奖次数
        int total = 0;
        // 根据是否3天连续获得总的红包次数
        if (Integer.valueOf(Constants.EVNumber.three).compareTo(loginDays) == 0) {     // 连续3天登陆
            total = depositRules.stream().mapToInt(RedPacketRainRuleDto::getNumber).sum();
            // 设置满足标志
            redPacketRainRuleDtos.stream().forEach(
                    rule -> {
                        if (depositAmount.compareTo(rule.getMinAmount()) >= 0) {
                            rule.setIsValid(true);
                        } else {
                            rule.setIsValid(false);
                        }
                    }
            );
        } else {  // 未连续3天
            total = depositRules.stream().filter(
                    rule -> !rule.getIsAward()
            ).mapToInt(RedPacketRainRuleDto::getNumber).sum();

            // 设置满足标志
            redPacketRainRuleDtos.stream().forEach(
                    rule -> {
                        if (depositAmount.compareTo(rule.getMinAmount()) >= 0 && !rule.getIsAward()) {
                            rule.setIsValid(true);
                        } else {
                            rule.setIsValid(false);
                        }
                    }
            );
        }

        if (total == Constants.EVNumber.zero) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // 获取当日的红包雨优惠个数
        OprActBonus bonus = new OprActBonus();
        bonus.setAccountId(accountId);
        bonus.setActivityId(actActivity.getId());
        Integer redPacketNum = operateActivityMapper.getTodyRedPacketRainCount(bonus);

        // 校验已领取红包
        if (Integer.valueOf(total).compareTo(redPacketNum) <= 0) {    // 已领取完毕
            ret.setRedPacketRainRuleDtos(redPacketRainRuleDtos);
            ret.setTotal(total);
            ret.setRedPacketNum(redPacketNum);
            return ret;
        }

        // 获取随机红包金额
        List<BigDecimal> randomAmount = dto.getRandomAmount();
        if (Collections3.isEmpty(randomAmount)) {
            throw new R200Exception("规则配置错误！");
        }
        Random random = new Random();
        BigDecimal bonusAmount = randomAmount.get(random.nextInt(randomAmount.size()));
        dto.setBonusAmount(bonusAmount);
        ret.setBonusAmount(bonusAmount);

        // 生成红利
        redPacketRainBonus(dto, actActivity, mbrAccount, ip,
                OrderConstants.ACTIVITY_HBY, Constants.EVNumber.zero, mbrAccount.getLoginName());


        ret.setRedPacketRainRuleDtos(redPacketRainRuleDtos);
        ret.setTotal(total);
        ret.setRedPacketNum(redPacketNum);
        return ret;

    }

    // 星期校验
    public Boolean checkoutRedPacketDay(RedPacketRainDto dto) {
        List<Integer> validDates = dto.getValidDates();
        // 获得当天是当周的星期几
        int forWeek = dayForWeek(getCurrentDate(FORMAT_10_DATE));
        for (Integer date : validDates) {
            if (date.equals(forWeek)) {
                return true;
            }
        }
        return false;
    }

    // 时间校验
    public Boolean checkoutRedPacketTime(RedPacketRainDto dto) {
        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();
        if (StringUtil.isEmpty(startTime) || StringUtil.isEmpty(endTime)) {
            return false;
        }
        String currentTime = getCurrentDate(FORMAT_8_TIME);
        if (currentTime.compareTo(startTime) >= 0 && currentTime.compareTo(endTime) <= 0) {
            return true;
        }
        return false;
    }

    // 红包雨生成红利记录
    public void redPacketRainBonus(RedPacketRainDto dto, OprActActivity actActivity,
                                   MbrAccount account, String ip, String financialCode, Integer source, String createUser) {
        OprActBonus bonus = oprActActivityCastService.setOprActBonus(account.getId(), account.getLoginName(), actActivity.getId(),
                null, null, actActivity.getRuleId());
        bonus.setScope(null);
        bonus.setCreateUser(account.getLoginName());
        bonus.setIp(ip);
        bonus.setDevSource(account.getLoginSource());
        bonus.setDiscountAudit(new BigDecimal(dto.getMultipleWater()));
        bonus.setBonusAmount(dto.getBonusAmount());
        bonus.setAuditAmount(auditAccountService.getAddAuditAmount(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        bonus.setSource(source);
        bonus.setCreateUser(createUser);
        actBonusMapper.insert(bonus);
        if (actActivity.getIsAudit() == Constants.EVNumber.zero) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            bonus.setDepositedAmount(null);
            oprActActivityCastService.auditOprActBonus(bonus, financialCode, actActivity.getActivityName(), Boolean.FALSE);
        }

    }
}
