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
        // ?????????????????????????????????????????????
        TaskCenterDto taskCenterDto = accountService.taskCenter(accountId, HR_TASK_ACTIVITY);
        // ??????????????????????????????
        if (nonNull(taskCenterDto)) {
            List<TaskConfig> taskConfigs = taskCenterDto.getTaskConfigs();
            if (Collections3.isEmpty(taskConfigs)) {
                return taskCenterDto;
            }
            if (taskConfigs.size() > 0) {
                taskConfigs.stream().forEach(ts -> {
                    List<TaskBonus> taskBonuses = Lists.newArrayList();
                    String day = ts.getRewardDto().getDay();
                    if (StringUtils.isNotEmpty(day)) {  // ???????????????????????????????????????????????????????????????????????????
                        taskBonuses = taskMapper.findHRTaskBonusList(accountId, ts.getId(), ts.getRewardDto().getNum());
                    }
                    if (taskBonuses.size() > 0) {
                        // ??????????????????????????????
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

            // ??????<????????????>???<?????????????????????>
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
        // ??????VIP??????????????????
        PageUtils pageUtils = actActivityService.webActivityList(1, 10, null,
                accountId, null, Constants.EVNumber.one, null, vipPrivilegesCode, null, null);

        OprActActivity oprActActivity = new OprActActivity();
        if (nonNull(pageUtils) && pageUtils.getList().size() > 0) {
            MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
            oprActActivity = pageUtils.getList().size() > 0 ? (OprActActivity) pageUtils.getList().get(0) : null;
            // ???????????????????????????????????????????????????????????????+????????????????????????/????????? ??????????????????1 ??? ??????????????????3 ?????????   4 ????????????????????? ?????????????????????
            Byte buttonShow = (byte) verificationVipPrivileges(oprActActivity, account);
            oprActActivity.setButtonShow(buttonShow);
            oprActActivity.setActLevelId(account.getActLevelId());
        }
        return oprActActivity;
    }

    private int verificationVipPrivileges(OprActActivity actActivity, MbrAccount account) {
        JDepositSentDto dto = jsonUtil.fromJson(actActivity.getRule(), JDepositSentDto.class);
        // ??????????????????????????????
        if (dto.getScope() == 1) {
            // ??????????????????
            List<ActivityLevelDto> activityLevels = sdyActivityMapper.findAcocountActivityLevel(null);
            List<RuleScopeDto> rebatesLevelDtos = Lists.newArrayList();
            dto.getRuleScopeDtos().stream().forEach(d -> {
                Optional<ActivityLevelDto> levelDtoOptional = activityLevels.stream().filter(e ->
                        e.getId().equals(d.getActLevelId())).findAny();
                if (levelDtoOptional.isPresent()) {
                    rebatesLevelDtos.add(d);
                }
            });
            // ?????????????????????????????????/??????
            rebatesLevelDtos.forEach(us -> {
                Optional<ActivityLevelDto> levelDtoOptional = activityLevels.stream().filter(e ->
                        e.getId().equals(us.getActLevelId())).findAny();
                us.setAccountLevel(levelDtoOptional.get().getAccountLevel());
                us.setTierName(levelDtoOptional.get().getTierName());
            });
            // ??????????????????
            actActivity.setRule(JSON.toJSONString(dto));
        }
        // ???????????????????????????????????????
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId(), dto.getScope());
        if (isNull(ruleScopeDto)) {
            return Constants.EVNumber.one;
        }
        Integer actLevelId = null;  // ????????????????????????null
        if (dto.getScope() == Constants.EVNumber.one) {
            actLevelId = account.getActLevelId();   // ???????????????????????????????????????
        }
        // ??????????????????????????????????????????????????????
        Boolean isDepositSent = oprDepositSentService.checkoutDepositSent(ruleScopeDto, actActivity.getId(), account.getId(), actLevelId);
        // ?????????????????????
        if (Boolean.FALSE.equals(isDepositSent)) {
            return Constants.EVNumber.three;
        }

        // ????????????????????????????????????(????????????????????????ruleId 888888??????)
        Integer count = auditMapper.findAuditAccountWithoutWater(account.getId());  // ???????????????????????????????????????????????????????????????
        if (count.intValue() > 0) {
            return Constants.EVNumber.four;
        }
        return Constants.EVNumber.one;
    }


    public RedPacketRainRespDto redPacketRainInfo() {
        // ?????????????????????????????????????????????
        OprActActivity actActivity = getOprActActivity(redPacketRainCode);
        if (isNull(actActivity)) {
            throw new R200Exception("??????????????????");
        }

        // ?????????????????????
        RedPacketRainDto dto = jsonUtil.fromJson(actActivity.getRule(), RedPacketRainDto.class);
        // ??????
        if (Objects.isNull(dto)) {
            throw new R200Exception("?????????????????????");
        }
        // ???????????????
        RedPacketRainRespDto ret = new RedPacketRainRespDto();
        ret.setStartTime(dto.getStartTime());   // ????????????????????????
        ret.setEndTime(dto.getEndTime());       // ????????????????????????
        ret.setValidDates(dto.getValidDates()); // ???????????? ??????1-7 ,???1,5,7  ??????????????????

        return ret;
    }

    public RedPacketRainRespDto redPacketClick(Integer accountId, String ip) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        //?????????????????????????????????????????????????????????
        if (actActivityService.isBlackList(accountId, TOpActtmpl.allActivityCode)) {
            throw new R200Exception("?????????????????????????????????");
        }
        //??????????????????????????????????????????????????????????????????
        if (actActivityService.valAgentBackList(mbrAccount, TOpActtmpl.allActivityCode)) {
            throw new R200Exception("?????????????????????????????????");
        }
        RedPacketRainRespDto ret = new RedPacketRainRespDto();

        // ????????????????????????
        OprActActivity actActivity = getOprActActivity(redPacketRainCode);
        if (isNull(actActivity)) {
            throw new R200Exception("??????????????????");
        }

        // ?????????????????????
        RedPacketRainDto dto = jsonUtil.fromJson(actActivity.getRule(), RedPacketRainDto.class);
        if (Objects.isNull(dto)) {
            throw new R200Exception("?????????????????????");
        }

        // ??????
        // ????????????
        Boolean dayCheck = checkoutRedPacketDay(dto);
        if (!dayCheck) {
            throw new R200Exception("??????????????????!");
        }
        // ????????????
        Boolean timecheck = checkoutRedPacketTime(dto);
        if (!timecheck) {
            throw new R200Exception("??????????????????!!");
        }

        // ??????
        List<RedPacketRainRuleDto> redPacketRainRuleDtos = dto.getRedPacketRainRuleDtos();
        if (Collections3.isEmpty(redPacketRainRuleDtos)) {
            throw new R200Exception("?????????????????????");
        }

        redPacketRainRuleDtos.sort((r1, r2) -> r2.getMinAmount().compareTo(r1.getMinAmount()));//?????????????????????

        // ??????????????????
        BigDecimal depositAmount = fundMapper.sumFundDepositByAccountId(accountId,
                DateUtil.getTodayStart(FORMAT_10_DATE), DateUtil.getTodayEnd(FORMAT_10_DATE));

        // ????????????????????????????????????
        List<RedPacketRainRuleDto> depositRules = redPacketRainRuleDtos.stream().filter(
                rule -> depositAmount.compareTo(rule.getMinAmount()) >= 0
        ).collect(Collectors.toList());

        if (Collections3.isEmpty(depositRules)) {
            // ???????????????
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // ??????????????????????????????
        String endDay = DateUtil.getCurrentDate(FORMAT_10_DATE);
        String startDay = DateUtil.getPastDate(Constants.EVNumber.two, FORMAT_10_DATE);
        Integer loginDays = logMapper.loginDays(accountId, startDay, endDay);

        // ??????????????????
        int total = 0;
        // ????????????3?????????????????????????????????
        if (Integer.valueOf(Constants.EVNumber.three).compareTo(loginDays) == 0) {     // ??????3?????????
            total = depositRules.stream().mapToInt(RedPacketRainRuleDto::getNumber).sum();
            // ??????????????????
            redPacketRainRuleDtos.stream().forEach(
                    rule -> {
                        if (depositAmount.compareTo(rule.getMinAmount()) >= 0) {
                            rule.setIsValid(true);
                        } else {
                            rule.setIsValid(false);
                        }
                    }
            );
        } else {  // ?????????3???
            total = depositRules.stream().filter(
                    rule -> !rule.getIsAward()
            ).mapToInt(RedPacketRainRuleDto::getNumber).sum();

            // ??????????????????
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

        // ????????????????????????????????????
        OprActBonus bonus = new OprActBonus();
        bonus.setAccountId(accountId);
        bonus.setActivityId(actActivity.getId());
        Integer redPacketNum = operateActivityMapper.getTodyRedPacketRainCount(bonus);

        // ?????????????????????
        if (Integer.valueOf(total).compareTo(redPacketNum) <= 0) {    // ???????????????
            ret.setRedPacketRainRuleDtos(redPacketRainRuleDtos);
            ret.setTotal(total);
            ret.setRedPacketNum(redPacketNum);
            return ret;
        }

        // ????????????????????????
        List<BigDecimal> randomAmount = dto.getRandomAmount();
        if (Collections3.isEmpty(randomAmount)) {
            throw new R200Exception("?????????????????????");
        }
        Random random = new Random();
        BigDecimal bonusAmount = randomAmount.get(random.nextInt(randomAmount.size()));
        dto.setBonusAmount(bonusAmount);
        ret.setBonusAmount(bonusAmount);

        // ????????????
        redPacketRainBonus(dto, actActivity, mbrAccount, ip,
                OrderConstants.ACTIVITY_HBY, Constants.EVNumber.zero, mbrAccount.getLoginName());


        ret.setRedPacketRainRuleDtos(redPacketRainRuleDtos);
        ret.setTotal(total);
        ret.setRedPacketNum(redPacketNum);
        return ret;

    }

    // ????????????
    public Boolean checkoutRedPacketDay(RedPacketRainDto dto) {
        List<Integer> validDates = dto.getValidDates();
        // ?????????????????????????????????
        int forWeek = dayForWeek(getCurrentDate(FORMAT_10_DATE));
        for (Integer date : validDates) {
            if (date.equals(forWeek)) {
                return true;
            }
        }
        return false;
    }

    // ????????????
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

    // ???????????????????????????
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
