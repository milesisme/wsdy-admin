package com.wsdy.saasops.modules.task.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrActivityLevelMapper;
import com.wsdy.saasops.modules.member.dao.MbrBankcardMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.dao.OprActBlacklistMapper;
import com.wsdy.saasops.modules.operate.entity.OprActBlacklist;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import com.wsdy.saasops.modules.task.dao.*;
import com.wsdy.saasops.modules.task.dto.*;
import com.wsdy.saasops.modules.task.entity.*;
import com.wsdy.saasops.modules.task.mapper.TaskMapper;
import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.constants.OrderConstants.*;
import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class TaskAccountService {

    @Autowired
    private TaskConfigMapper configMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private OprActBlacklistMapper blacklistMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private OprActActivityService actActivityService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private TaskStatisticalMapper statisticalMapper;
    @Autowired
    private TaskSigninMapper taskSigninMapper;
    @Autowired
    private TaskBonusMapper taskBonusMapper;
    @Autowired
    private AuditAccountService accountAuditService;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private MbrActivityLevelMapper activityLevelMapper;
    @Autowired
    private TaskLevelMapper taskLevelMapper;
    @Autowired
    private MbrBankcardMapper bankcardMapper;
    @Autowired
    private FundMapper fundMapper;

    @Value("${task.bounsStatistical.excel.path:excelTemplate/task/??????????????????.xls}")
    private String bounsStatisticalExportPath;

    private final String bounsStatisticalModule = "bounsStatisticalList";
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Autowired
    private JsonUtil jsonUtil;

    public List<TaskConfig> configList() {
        return taskMapper.configList(null);
    }

    public TaskConfig configInfo(Integer id) {
        List<TaskConfig> configs = taskMapper.configList(id);
        return configs.size() > 0 ? configs.get(0) : null;
    }

    public void updateTaskRule(TaskConfig taskConfig, String username) {
        TaskConfig config = configMapper.selectByPrimaryKey(taskConfig.getId());
        checkoutJson(config);
        config.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        config.setModifyUser(username);
        config.setRule(taskConfig.getRule());
        if (StringUtils.isEmpty(taskConfig.getRule())) {
            config.setAvailable(Constants.EVNumber.zero);
        }
        config.setTaskName(taskConfig.getTaskName());
        configMapper.updateByPrimaryKeySelective(config);
        // ????????????
        mbrAccountLogService.updateTaskRuleLog(config);
    }

    private void checkoutJson(TaskConfig taskConfig) {
        try {
            if (QD_TASK_ACTIVITY.equals(taskConfig.getFinancialCode())) {
                JSON.parseObject(taskConfig.getRule(), TaskSigninRuleDto.class);
            }
            if (XS_TASK_ACTIVITY.equals(taskConfig.getFinancialCode())) {
                new Gson().fromJson(taskConfig.getRule(), new TypeToken<List<TaskActivityRuleDto>>() {
                }.getType());
            }
            if (SJ_TASK_ACTIVITY.equals(taskConfig.getFinancialCode())) {
                JSON.parseObject(taskConfig.getRule(), TaskUpgradeDto.class);
            }
            if (ZL_TASK_ACTIVITY.equals(taskConfig.getFinancialCode())) {
                JSON.parseObject(taskConfig.getRule(), TaskAccountDto.class);
            }
            if (DS_TASK_ACTIVITY.equals(taskConfig.getFinancialCode())) {
                JSON.parseObject(taskConfig.getRule(), TaskTimeDto.class);
            }
            if (HR_TASK_ACTIVITY.equals(taskConfig.getFinancialCode())) {
                JSON.parseObject(taskConfig.getRule(), TaskActiveRewardDto.class);
            }
        } catch (Exception e) {
            throw new R200Exception("??????????????????");
        }
    }


    public void updateAvailable(Integer id, Integer available, String username) {
        TaskConfig taskConfig = configMapper.selectByPrimaryKey(id);
        if (StringUtils.isEmpty(taskConfig.getRule())) {
            throw new R200Exception("??????????????????????????????????????????");
        }
        taskConfig.setAvailable(available);
        taskConfig.setModifyUser(username);
        taskConfig.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        taskConfig.setOpeningtime(getCurrentDate(FORMAT_18_DATE_TIME));
        configMapper.updateByPrimaryKeySelective(taskConfig);
        // ????????????
        mbrAccountLogService.taskUpdateAvailableLog(taskConfig);

    }

    public PageUtils taskBlackList(Integer id, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<OprActBlacklist> list = taskMapper.taskBlackList(id);
        return BeanUtil.toPagedResult(list);
    }

    public void deletTaskBlackList(Integer id) {
        // ????????????????????????????????????
        OprActBlacklist black = blacklistMapper.selectByPrimaryKey(id);
        blacklistMapper.deleteByPrimaryKey(id);
        // ????????????
        mbrAccountLogService.deletTaskBlackListLog(black);
    }

    public void addTaskBlackList(Integer configId, String loginName) {
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setLoginName(loginName);
        MbrAccount account = accountMapper.selectOne(mbrAccount);
        if (isNull(account)) {
            throw new R200Exception("???????????????");
        }
        TaskConfig taskConfig = configMapper.selectByPrimaryKey(configId);
        if (actActivityService.isBlackList(account.getId(), taskConfig.getFinancialCode())) {
            throw new R200Exception("??????????????????????????????????????????");
        }
        OprActBlacklist blacklist = new OprActBlacklist();
        blacklist.setAccountId(account.getId());
        blacklist.setLoginName(account.getLoginName());
        blacklist.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        blacklist.setTmplCode(taskConfig.getFinancialCode());
        blacklistMapper.insert(blacklist);

        // ????????????
        mbrAccountLogService.addTaskBlackListLog(blacklist);
    }

    public List<TaskBonus> bounsStatistical(TaskBonus taskBonus) {
        return taskMapper.findBonusStatistical(taskBonus);
    }

    public PageUtils bounsDetail(TaskBonus taskBonus, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TaskBonus> list = taskMapper.bounsDetail(taskBonus);
        return BeanUtil.toPagedResult(list);
    }

    public TaskCenterDto taskCenter(Integer accountId, String financialCode) {
        TaskCenterDto centerDto = new TaskCenterDto();
        // ??????????????????????????????????????????
        centerDto.setDayAmount(taskMapper.findBonusAmount(accountId, getCurrentDate(FORMAT_10_DATE)));
        // ??????????????????????????????????????????
        centerDto.setSumAmount(taskMapper.findBonusAmount(accountId, null));
        // ??????????????????
        List<TaskConfig> configs = taskMapper.findTaskConfigList(financialCode);
        if (Collections3.isEmpty(configs)) {
            return centerDto;
        }
        // ??????????????????????????????????????????????????????????????????
        setConfigs(configs, accountId);
        centerDto.setTaskConfigs(configs);
        return centerDto;
    }

    private void setConfigs(List<TaskConfig> configs, Integer accountId) {
        configs.stream().forEach(cs -> {
            // ?????????????????????
            int count = taskMapper.findBlackListByAccountId(cs.getFinancialCode(), accountId);
            cs.setIsBlacklist(count > 0 ? Constants.EVNumber.one : Constants.EVNumber.zero);
            // ??????
            if (QD_TASK_ACTIVITY.equals(cs.getFinancialCode())) {
                TaskSignin taskSignin = taskMapper.findAccountSignin(accountId, null);
                if (nonNull(taskSignin)) {
                    cs.setQdNumber(taskSignin.getNumber());
                    cs.setQdTime(taskSignin.getTime());
                }
            }
            // ??????
            if (XS_TASK_ACTIVITY.equals(cs.getFinancialCode())) {
                List<TaskActivityRuleDto> ruleDtos = new Gson().fromJson(cs.getRule(),
                        new TypeToken<List<TaskActivityRuleDto>>() {
                        }.getType());
                List<Integer> ids = ruleDtos.stream().map(TaskActivityRuleDto::getActivityId).collect(Collectors.toList());
                List<TaskActivityDto> activityDtos = taskMapper.findTaskActivity(ids);
                activityDtos.stream().forEach(as -> {
                    ruleDtos.stream().forEach(rs -> {
                        if (rs.getActivityId().equals(as.getActivityId())) {
                            as.setSort(rs.getSort());
                        }
                    });
                });
                Collections.sort(activityDtos, Comparator.comparing(TaskActivityDto::getSort));
                cs.setActivityDtos(activityDtos);
            }
            // ??????
            if (SJ_TASK_ACTIVITY.equals(cs.getFinancialCode())) {
                UpgradeAwardsDto upgradeAwards = new UpgradeAwardsDto();
                upgradeAwards.setAccountLevel(taskMapper.findTaskAccountLevel(accountId));
                upgradeAwards.setDrawAccountLevels(taskMapper.findDrawAccountLevel(accountId));
                cs.setUpgradeAwards(upgradeAwards);
            }
            // ????????????
            if (ZL_TASK_ACTIVITY.equals(cs.getFinancialCode())) {
                // ???????????????????????????????????????
                cs.setAccountInfoDto(setAccountInfoDto(accountId, cs));
            }
            // ??????
            if (DS_TASK_ACTIVITY.equals(cs.getFinancialCode())) {
                cs.setTaskTimeDto(setTaskTimeDto(accountId, cs));
            }
            // ????????????
            if (HR_TASK_ACTIVITY.equals(cs.getFinancialCode())) {
                cs.setRewardDto(setTaskActiveRewardDto(accountId, cs));
            }
        });
    }

    private TaskActiveRewardDto setTaskActiveRewardDto(Integer accountId, TaskConfig config) {
        TaskActiveRewardDto rewardDto = JSON.parseObject(config.getRule(), TaskActiveRewardDto.class);
        // ?????????????????????????????????
        TaskBonus taskBonus = taskMapper.findTaskBonusLimtOne(accountId, config.getId());
        if (nonNull(taskBonus)) {
            rewardDto.setNum(taskBonus.getNum());                   // ?????????????????????????????????
            rewardDto.setDay(taskBonus.getTime().substring(0, 10)); // ??????????????????????????????
        }
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        BigDecimal dayDepositAmount = taskMapper.findDepositamountTsk(accountId, getCurrentDate(FORMAT_10_DATE));       // ??????????????????
        BigDecimal dayValidbet = taskMapper.findValidbetTsk(account.getLoginName(), getCurrentDate(FORMAT_10_DATE));    // ??????????????????
        rewardDto.setDayDepositAmount(dayDepositAmount);
        rewardDto.setDayValidbet(dayValidbet);

        // ??????????????????????????????
        TaskActiveInfoDto activeInfoDto = new TaskActiveInfoDto();
        activeInfoDto.setIsMobile(account.getIsVerifyMoblie() == 1 ? Boolean.TRUE : Boolean.FALSE);
        activeInfoDto.setIsName(StringUtils.isNotEmpty(account.getRealName()) ? Boolean.TRUE : Boolean.FALSE);
        activeInfoDto.setIsMail(account.getIsVerifyEmail() == 1 ? Boolean.TRUE : Boolean.FALSE);
        MbrBankcard bankcard = new MbrBankcard();
        bankcard.setAccountId(account.getId());
        bankcard.setAvailable(Constants.Available.enable);
        bankcard.setIsDel(Constants.Available.disable);
        int count = bankcardMapper.selectCount(bankcard);
        activeInfoDto.setIsBank(count > 0 ? Boolean.TRUE : Boolean.FALSE);
        rewardDto.setAccountInfoDto(activeInfoDto);
        return rewardDto;
    }

    private TaskTimeDto setTaskTimeDto(Integer accountId, TaskConfig config) {
        TaskTimeDto taskTimeDto = JSON.parseObject(config.getRule(), TaskTimeDto.class);
        taskTimeDto.setReceiveTime(config.getOpeningtime());
        TaskBonus bonus = taskMapper.findTaskReceiveTime(accountId, config.getId(), config.getOpeningtime());
        if (nonNull(bonus)) {
            taskTimeDto.setReceiveTime(bonus.getTime());
        }
        return taskTimeDto;
    }

    private TaskAccountInfoDto setAccountInfoDto(Integer accountId, TaskConfig config) {
        TaskAccountInfoDto accountInfoDto = new TaskAccountInfoDto();
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        accountInfoDto.setIsMobile(account.getIsVerifyMoblie() == 1 ? Boolean.TRUE : Boolean.FALSE);
        accountInfoDto.setIsName(StringUtils.isNotEmpty(account.getRealName()) ? Boolean.TRUE : Boolean.FALSE);

        // ???????????????
        TaskBonus bonus = new TaskBonus();
        bonus.setAccountId(accountId);
        bonus.setConfigId(config.getId());
        int countBonus = taskBonusMapper.selectCount(bonus);
        accountInfoDto.setIsBonus(countBonus > 0 ? Boolean.TRUE : Boolean.FALSE);

        // ?????????????????????
        MbrBankcard bankcard = new MbrBankcard();
        bankcard.setAccountId(account.getId());
        bankcard.setAvailable(Constants.Available.enable);
        bankcard.setIsDel(Constants.Available.disable);
        int count = bankcardMapper.selectCount(bankcard);
        accountInfoDto.setIsBank(count > 0 ? Boolean.TRUE : Boolean.FALSE);

        // ??????????????????
        TaskAccountDto ruleDto = JSON.parseObject(config.getRule(), TaskAccountDto.class);  // ??????dto
        // ??????????????????
        BigDecimal depositBigDecimal = fundMapper.sumFundDepositByAccountId(account.getId(),null,null);
        if (isNull(ruleDto.getMinAmount()) || depositBigDecimal.compareTo(ruleDto.getMinAmount()) >= 0) {
            accountInfoDto.setIsMinAmount(Boolean.TRUE);    // ??????????????????????????????????????????????????????????????????
        }else{
            accountInfoDto.setIsMinAmount(Boolean.FALSE);
        }

        // ??????????????????
        accountInfoDto.setBonusAmount(ruleDto.getBonusAmount());        // ????????????
        accountInfoDto.setMultipleWater(ruleDto.getMultipleWater());    // ????????????
        accountInfoDto.setMinAmount(ruleDto.getMinAmount());            // ????????????

        return accountInfoDto;
    }

    public void taskClickRate(Integer configId, String siteCode) {
        TaskStatistical statistical = new TaskStatistical();
        statistical.setConfigId(configId);
        int count = statisticalMapper.selectCount(statistical);
        if (count == 0) {
            String key = RedisConstants.TASK_CLICK_RATE + siteCode + configId;
            Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isExpired)) {
                TaskStatistical statistical1 = new TaskStatistical();
                statistical1.setConfigId(configId);
                statistical1.setNumber(1L);
                statisticalMapper.insert(statistical1);
                redisService.del(key);
            }
        } else {
            taskMapper.updateClickRate(configId);
        }
    }

    public void getTask(Integer configId, Integer accountId, String siteCode, String loginName, Integer level) {
        String key = RedisConstants.TASK_ACCOUT + siteCode + accountId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                TaskConfig config = configMapper.selectByPrimaryKey(configId);
                if(Objects.isNull(config)){
                    throw new R200Exception("???????????????");
                }
                int count = taskMapper.findBlackListByAccountId(config.getFinancialCode(), accountId);
                if (count > 0) {
                    log.info(loginName + "?????????" + config.getFinancialCode());
                    throw new R200Exception("???????????????????????????");
                }
                if (QD_TASK_ACTIVITY.equals(config.getFinancialCode())) {
                    taskSignin(config, accountId, loginName);
                }
                if (SJ_TASK_ACTIVITY.equals(config.getFinancialCode())) {
                    upgradeAwards(config, accountId, loginName, level);
                }
                // ????????????
                if (ZL_TASK_ACTIVITY.equals(config.getFinancialCode())) {
                    accountInfoBonusAmount(config, accountId, loginName);
                }
                if (DS_TASK_ACTIVITY.equals(config.getFinancialCode())) {
                    accountTimeBonusAmount(config, accountId, loginName);
                }
                if (HR_TASK_ACTIVITY.equals(config.getFinancialCode())) {
                    accounttaskActiveReward(config, accountId, loginName);
                }
            } finally {
                redisService.del(key);
            }
        }
    }

    private void accounttaskActiveReward(TaskConfig config, Integer accountId, String loginName) {
        TaskActiveRewardDto rewardDto = setTaskActiveRewardDto(accountId, config);
        StringBuffer errorMsg = new StringBuffer();
        if (Boolean.TRUE.equals(rewardDto.getIsBank()) && Boolean.FALSE.equals(rewardDto.getAccountInfoDto().getIsBank())) {
            errorMsg.append("?????????????????????");
        }
        if (Boolean.TRUE.equals(rewardDto.getIsMobile()) && Boolean.FALSE.equals(rewardDto.getAccountInfoDto().getIsMobile())) {
            errorMsg.append("?????????????????????");
        }
        if (Boolean.TRUE.equals(rewardDto.getIsName()) && Boolean.FALSE.equals(rewardDto.getAccountInfoDto().getIsName())) {
            errorMsg.append("????????????????????????");
        }
        if (Boolean.TRUE.equals(rewardDto.getIsMail()) && Boolean.FALSE.equals(rewardDto.getAccountInfoDto().getIsMail())) {
            errorMsg.append("???????????????!");
        }
        BigDecimal dayDepositAmount = taskMapper.findDepositamountTsk(accountId, getCurrentDate(FORMAT_10_DATE));
        BigDecimal validbet = taskMapper.findValidbetTsk(loginName, getCurrentDate(FORMAT_10_DATE));
        TaskActiveFilesDto activeFilesDto = getTaskActiveDayDto(dayDepositAmount, validbet, rewardDto);
        if (isNull(activeFilesDto)) {
        	errorMsg.append("??????????????????");
        	List<TaskActiveFilesDto> activeDayDtos = rewardDto.getFilesDtos();
        	TaskActiveFilesDto taskActiveFilesDto = activeDayDtos.stream().filter(t -> t.getNum() == 1).findAny().get();
        	BigDecimal depositAmount = taskActiveFilesDto.getDepositAmount();
        	if (dayDepositAmount.compareTo(depositAmount) == -1) {
        		errorMsg.append("??????:" + dayDepositAmount + "/" + depositAmount);
        	}
        	if (validbet.compareTo(taskActiveFilesDto.getValidBet()) == -1) {
        		errorMsg.append("??????:" + validbet + "/" + taskActiveFilesDto.getValidBet());
        	}
        }
        String errorMsgStr = errorMsg.toString();
        if (StringUtil.isNotEmpty(errorMsgStr)) {
        	throw new R200Exception(errorMsgStr);
        }
        List<TaskActiveDayDto> dayDtoList = activeFilesDto.getDayDtoList();
        Collections.sort(dayDtoList, Comparator.comparing(TaskActiveDayDto::getDay).reversed());
        if (rewardDto.getCycle() == 0 && dayDtoList.get(0).getDay() == rewardDto.getNum()) {
        	throw new R200Exception("???????????????,?????????????????????!");
        }
        /*if (nonNull(rewardDto.getDepositAmount()) && dayDepositAmount.compareTo(rewardDto.getDepositAmount()) == -1) {
            throw new R200Exception("?????????????????????????????????");
        }
        if (nonNull(rewardDto.getValidBet()) && validbet.compareTo(rewardDto.getValidBet()) == -1) {
            throw new R200Exception("???????????????????????????????????????");
        }*/
        String data1 = getCurrentDate(FORMAT_10_DATE);
        if (data1.equals(rewardDto.getDay())) {
        	throw new R200Exception("???????????????????????????!");
        }
        List<TaskActiveDayDto> dayDtos = activeFilesDto.getDayDtoList();
        Collections.sort(dayDtos, Comparator.comparing(TaskActiveDayDto::getDay));
        TaskActiveDayDto activeDayDto = dayDtos.get(0);
        int num = 0;
        if (StringUtils.isNotEmpty(rewardDto.getDay())) {
            num = daysBetween(rewardDto.getDay(), data1);
        }
        if (num == 1) {
            for (TaskActiveDayDto dayDto : dayDtos) {
                if (dayDto.getDay() > rewardDto.getNum()) {
                    activeDayDto = dayDto;
                    break;
                }
            }
        } else {
            activeDayDto = dayDtos.get(0);

        }
        insertTaskBonus(accountId, loginName, activeDayDto.getBonusAmount(),
                rewardDto.getMultipleWater(), config.getId(), HR_TASK_ACTIVITY, activeDayDto.getDay());
    }

    /**
     * @param dayDepositAmount ???????????????
     * @param validbet ???????????????
     * @param rewardDto  
     * @return
     */
    private TaskActiveFilesDto getTaskActiveDayDto(BigDecimal dayDepositAmount, BigDecimal validbet, TaskActiveRewardDto rewardDto) {
        List<TaskActiveFilesDto> activeDayDtos = rewardDto.getFilesDtos();
        Collections.sort(activeDayDtos, Comparator.comparing(TaskActiveFilesDto::getNum).reversed());
        for (TaskActiveFilesDto filesDto : activeDayDtos) {
        	// ???????????????????????????????????????
            if (dayDepositAmount.compareTo(filesDto.getDepositAmount()) != -1
                    && validbet.compareTo(filesDto.getValidBet()) != -1) {
                return filesDto;
            }
        }
        return null;
    }

    private void accountTimeBonusAmount(TaskConfig config, Integer accountId, String loginName) {
        TaskTimeDto taskTimeDto = setTaskTimeDto(accountId, config);
        Long min = getDistanceTimes(getCurrentDate(FORMAT_18_DATE_TIME), taskTimeDto.getReceiveTime());
        if (min < taskTimeDto.getTime()) {
            throw new R200Exception("?????????????????????");
        }
        insertTaskBonus(accountId, loginName, taskTimeDto.getBonusAmount(), taskTimeDto.getMultipleWater(),
                config.getId(), DS_TASK_ACTIVITY, null);
    }

    private void accountInfoBonusAmount(TaskConfig config, Integer accountId, String loginName) {
        // ??????dto
        TaskAccountDto ruleDto = JSON.parseObject(config.getRule(), TaskAccountDto.class);
        // ????????????????????????
        TaskAccountInfoDto accountInfoDto = setAccountInfoDto(accountId, config);
        if (Boolean.TRUE.equals(accountInfoDto.getIsBonus())) {
            throw new R200Exception("?????????????????????");
        }
        if (Boolean.TRUE.equals(ruleDto.getIsBank()) && Boolean.FALSE.equals(accountInfoDto.getIsBank())) {
            throw new R200Exception("?????????????????????");
        }
        if (Boolean.TRUE.equals(ruleDto.getIsMobile()) && Boolean.FALSE.equals(accountInfoDto.getIsMobile())) {
            throw new R200Exception("?????????????????????");
        }
        if (Boolean.TRUE.equals(ruleDto.getIsName()) && Boolean.FALSE.equals(accountInfoDto.getIsName())) {
            throw new R200Exception("?????????????????????");
        }
        if (Boolean.FALSE.equals(accountInfoDto.getIsMinAmount())) {
            throw new R200Exception("??????????????????");
        }
        insertTaskBonus(accountId, loginName, ruleDto.getBonusAmount(), ruleDto.getMultipleWater(),
                config.getId(), ZL_TASK_ACTIVITY, null);
    }

    private void upgradeAwards(TaskConfig config, Integer accountId, String loginName, Integer level) {
        if (isNull(level) || level < 1) {
            throw new R200Exception(level + "????????????");
        }
        Integer accountLevel = taskMapper.findTaskAccountLevel(accountId);
        List<Integer> drawAccountLevels = taskMapper.findDrawAccountLevel(accountId);
        Optional<Integer> drawAccountLevel = drawAccountLevels.stream().filter(ts -> ts.equals(level)).findAny();
        if (drawAccountLevel.isPresent()) {
            throw new R200Exception("?????????????????????");
        }
        if (accountLevel < level || "0".equals(accountLevel)) {
            throw new R200Exception("?????????????????????");
        }
        TaskUpgradeDto ruleDto = JSON.parseObject(config.getRule(), TaskUpgradeDto.class);
        setAccountLevel(ruleDto);
        List<TaskUpgradeStarDto> starDtos = ruleDto.getStarDtos();
        // Collections.sort(starDtos, Comparator.comparing(TaskUpgradeStarDto::getAccountLevel).reversed());
        Optional<TaskUpgradeStarDto> signinListDto = starDtos.stream().filter(ts -> ts.getAccountLevel().equals(level)).findAny();
        if (signinListDto.isPresent()) {
            TaskLevel taskLevel = new TaskLevel();
            taskLevel.setAccountId(accountId);
            taskLevel.setLoginName(loginName);
            taskLevel.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
            taskLevel.setAccountLevelid(signinListDto.get().getAccountLevelId());
            taskLevelMapper.insert(taskLevel);
            insertTaskBonus(accountId, loginName, signinListDto.get().getAmount(),
                    ruleDto.getMultipleWater(), config.getId(), SJ_TASK_ACTIVITY, null);
        }
    }

    private void setAccountLevel(TaskUpgradeDto ruleDto) {
        ruleDto.getStarDtos().stream().forEach(rs -> {
            rs.setAccountLevel(activityLevelMapper.selectByPrimaryKey(rs.getAccountLevelId()).getAccountLevel());
        });
    }

    private void taskSignin(TaskConfig config, Integer accountId, String loginName) {
        TaskSignin signin = taskMapper.findAccountSignin(accountId, getCurrentDate(FORMAT_10_DATE));
        if (nonNull(signin)) {
            throw new R200Exception("?????????????????????");
        }

        TaskSigninRuleDto ruleDto = JSON.parseObject(config.getRule(), TaskSigninRuleDto.class);
        TaskSignin taskSignin = taskMapper.findAccountSignin(accountId, null);
        TaskSignin taskSignin1 = new TaskSignin();
        taskSignin1.setAccountId(accountId);
        taskSignin1.setLoginName(loginName);
        taskSignin1.setTime(getCurrentDate(FORMAT_10_DATE));

        String date = getPastDate(Constants.EVNumber.one, FORMAT_10_DATE);
        BigDecimal amount = BigDecimal.ZERO;
        if (nonNull(taskSignin) && date.equals(taskSignin.getTime()) && taskSignin.getNumber() < 7) {
            taskSignin1.setNumber(taskSignin.getNumber() + 1);
            Optional<TaskSigninListDto> signinListDto = ruleDto.getSigninDtos().stream().filter(ts -> taskSignin1.getNumber() == ts.getDay()).findAny();
            if (signinListDto.isPresent()) {
                amount = signinListDto.get().getAmount();
            } else {
                throw new R200Exception("????????????");
            }
        } else {
            taskSignin1.setNumber(1);
            Optional<TaskSigninListDto> signinListDto = ruleDto.getSigninDtos().stream().filter(ts -> 1 == ts.getDay()).findAny();
            if (signinListDto.isPresent()) {
                amount = signinListDto.get().getAmount();
            } else {
                throw new R200Exception("????????????");
            }
        }
        if (amount.compareTo(BigDecimal.ZERO) != 1) {
            throw new R200Exception("????????????");
        }
        taskSigninMapper.insert(taskSignin1);
        insertTaskBonus(accountId, loginName, amount, ruleDto.getMultipleWater(), config.getId(), QD_TASK_ACTIVITY, null);
    }

    private void insertTaskBonus(Integer accountId, String loginName, BigDecimal amount,
                                 Integer multipleWater, Integer configId, String financialCode,
                                 Integer num) {
        TaskBonus taskBonus = new TaskBonus();
        taskBonus.setAccountId(accountId);
        taskBonus.setLoginName(loginName);
        taskBonus.setBonusAmount(amount);
        taskBonus.setDiscountAudit(multipleWater);
        taskBonus.setConfigId(configId);
        taskBonus.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
        taskBonus.setNum(num);
        taskBonus.setOrderNo(String.valueOf(new SnowFlake().nextId()));
        taskBonusMapper.insert(taskBonus);

        if (nonNull(multipleWater) && multipleWater > 0) {
            int ruleId = Integer.parseInt(configId + "888888");
            accountAuditService.insertAccountAudit(accountId, null,
                    null, new BigDecimal(multipleWater), null,
                    amount, ruleId, Constants.EVNumber.two);
        }
        walletService.castWalletAndBillDetail(loginName,
                accountId, financialCode, amount, taskBonus.getOrderNo()
                , Boolean.TRUE, null, null);
    }

    public PageUtils taskBonus(Integer accountId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        TaskBonus taskBonus = new TaskBonus();
        taskBonus.setAccountId(accountId);
        List<TaskBonus> list = taskMapper.bounsDetail(taskBonus);
        return BeanUtil.toPagedResult(list);
    }


    public SysFileExportRecord exportBounsStatistical(TaskBonus taskBonus,Long userId) {
        // ????????????????????????
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, bounsStatisticalModule);

        // ??????????????????????????????????????????????????????
        if (null != record) {
            List<TaskBonus> taskBonuslist = taskMapper.bounsDetail(taskBonus);
            List<Map<String, Object>> list = taskBonuslist.stream().map(e -> {

                Map<String, Object> entityMap = jsonUtil.Entity2Map(e);
                return entityMap;
            }).collect(Collectors.toList());
            String siteCode = CommonUtil.getSiteCode();
            sysFileExportRecordService.exportExcel(bounsStatisticalExportPath, list, userId, bounsStatisticalModule, siteCode);
        }
        return record;
    }

    public R checkFile(String module, Long userId){
        // ???????????????module????????????
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if(null != record){
            String fileName = "";
            fileName = bounsStatisticalExportPath.substring(bounsStatisticalExportPath.lastIndexOf("/")+1,bounsStatisticalExportPath.length());
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }
}
