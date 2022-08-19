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

    @Value("${task.bounsStatistical.excel.path:excelTemplate/task/任务中心统计.xls}")
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
        // 操作日志
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
            throw new R200Exception("数据格式不对");
        }
    }


    public void updateAvailable(Integer id, Integer available, String username) {
        TaskConfig taskConfig = configMapper.selectByPrimaryKey(id);
        if (StringUtils.isEmpty(taskConfig.getRule())) {
            throw new R200Exception("需要先配置活动，才能开启任务");
        }
        taskConfig.setAvailable(available);
        taskConfig.setModifyUser(username);
        taskConfig.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        taskConfig.setOpeningtime(getCurrentDate(FORMAT_18_DATE_TIME));
        configMapper.updateByPrimaryKeySelective(taskConfig);
        // 操作日志
        mbrAccountLogService.taskUpdateAvailableLog(taskConfig);

    }

    public PageUtils taskBlackList(Integer id, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<OprActBlacklist> list = taskMapper.taskBlackList(id);
        return BeanUtil.toPagedResult(list);
    }

    public void deletTaskBlackList(Integer id) {
        // 删除前查出记录，记入日志
        OprActBlacklist black = blacklistMapper.selectByPrimaryKey(id);
        blacklistMapper.deleteByPrimaryKey(id);
        // 操作日志
        mbrAccountLogService.deletTaskBlackListLog(black);
    }

    public void addTaskBlackList(Integer configId, String loginName) {
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setLoginName(loginName);
        MbrAccount account = accountMapper.selectOne(mbrAccount);
        if (isNull(account)) {
            throw new R200Exception("用户不存在");
        }
        TaskConfig taskConfig = configMapper.selectByPrimaryKey(configId);
        if (actActivityService.isBlackList(account.getId(), taskConfig.getFinancialCode())) {
            throw new R200Exception("用户已在黑名单，不能重复添加");
        }
        OprActBlacklist blacklist = new OprActBlacklist();
        blacklist.setAccountId(account.getId());
        blacklist.setLoginName(account.getLoginName());
        blacklist.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        blacklist.setTmplCode(taskConfig.getFinancialCode());
        blacklistMapper.insert(blacklist);

        // 操作日志
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
        // 获取会员任务中心总的今日收益
        centerDto.setDayAmount(taskMapper.findBonusAmount(accountId, getCurrentDate(FORMAT_10_DATE)));
        // 获取会员任务中心总的累计收益
        centerDto.setSumAmount(taskMapper.findBonusAmount(accountId, null));
        // 获取任务配置
        List<TaskConfig> configs = taskMapper.findTaskConfigList(financialCode);
        if (Collections3.isEmpty(configs)) {
            return centerDto;
        }
        // 处理任务配置参数及校验校验结果，返回信息等；
        setConfigs(configs, accountId);
        centerDto.setTaskConfigs(configs);
        return centerDto;
    }

    private void setConfigs(List<TaskConfig> configs, Integer accountId) {
        configs.stream().forEach(cs -> {
            // 设置是否黑名单
            int count = taskMapper.findBlackListByAccountId(cs.getFinancialCode(), accountId);
            cs.setIsBlacklist(count > 0 ? Constants.EVNumber.one : Constants.EVNumber.zero);
            // 签到
            if (QD_TASK_ACTIVITY.equals(cs.getFinancialCode())) {
                TaskSignin taskSignin = taskMapper.findAccountSignin(accountId, null);
                if (nonNull(taskSignin)) {
                    cs.setQdNumber(taskSignin.getNumber());
                    cs.setQdTime(taskSignin.getTime());
                }
            }
            // 限时
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
            // 升级
            if (SJ_TASK_ACTIVITY.equals(cs.getFinancialCode())) {
                UpgradeAwardsDto upgradeAwards = new UpgradeAwardsDto();
                upgradeAwards.setAccountLevel(taskMapper.findTaskAccountLevel(accountId));
                upgradeAwards.setDrawAccountLevels(taskMapper.findDrawAccountLevel(accountId));
                cs.setUpgradeAwards(upgradeAwards);
            }
            // 完善资料
            if (ZL_TASK_ACTIVITY.equals(cs.getFinancialCode())) {
                // 获取规则校验结果及返回数据
                cs.setAccountInfoDto(setAccountInfoDto(accountId, cs));
            }
            // 定时
            if (DS_TASK_ACTIVITY.equals(cs.getFinancialCode())) {
                cs.setTaskTimeDto(setTaskTimeDto(accountId, cs));
            }
            // 活跃奖励
            if (HR_TASK_ACTIVITY.equals(cs.getFinancialCode())) {
                cs.setRewardDto(setTaskActiveRewardDto(accountId, cs));
            }
        });
    }

    private TaskActiveRewardDto setTaskActiveRewardDto(Integer accountId, TaskConfig config) {
        TaskActiveRewardDto rewardDto = JSON.parseObject(config.getRule(), TaskActiveRewardDto.class);
        // 获取最近一次的签到信息
        TaskBonus taskBonus = taskMapper.findTaskBonusLimtOne(accountId, config.getId());
        if (nonNull(taskBonus)) {
            rewardDto.setNum(taskBonus.getNum());                   // 最近一次的签到是第几天
            rewardDto.setDay(taskBonus.getTime().substring(0, 10)); // 最近一次的签到的时间
        }
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        BigDecimal dayDepositAmount = taskMapper.findDepositamountTsk(accountId, getCurrentDate(FORMAT_10_DATE));       // 会员当日存款
        BigDecimal dayValidbet = taskMapper.findValidbetTsk(account.getLoginName(), getCurrentDate(FORMAT_10_DATE));    // 会员当日投注
        rewardDto.setDayDepositAmount(dayDepositAmount);
        rewardDto.setDayValidbet(dayValidbet);

        // 获取资料信息校验结果
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

        // 是否已领取
        TaskBonus bonus = new TaskBonus();
        bonus.setAccountId(accountId);
        bonus.setConfigId(config.getId());
        int countBonus = taskBonusMapper.selectCount(bonus);
        accountInfoDto.setIsBonus(countBonus > 0 ? Boolean.TRUE : Boolean.FALSE);

        // 是否绑定银行卡
        MbrBankcard bankcard = new MbrBankcard();
        bankcard.setAccountId(account.getId());
        bankcard.setAvailable(Constants.Available.enable);
        bankcard.setIsDel(Constants.Available.disable);
        int count = bankcardMapper.selectCount(bankcard);
        accountInfoDto.setIsBank(count > 0 ? Boolean.TRUE : Boolean.FALSE);

        // 存款是否满足
        TaskAccountDto ruleDto = JSON.parseObject(config.getRule(), TaskAccountDto.class);  // 规则dto
        // 累积充值金额
        BigDecimal depositBigDecimal = fundMapper.sumFundDepositByAccountId(account.getId(),null,null);
        if (isNull(ruleDto.getMinAmount()) || depositBigDecimal.compareTo(ruleDto.getMinAmount()) >= 0) {
            accountInfoDto.setIsMinAmount(Boolean.TRUE);    // 未配置或累积存款大于存款需求，则满足存款条件
        }else{
            accountInfoDto.setIsMinAmount(Boolean.FALSE);
        }

        // 用于前端显示
        accountInfoDto.setBonusAmount(ruleDto.getBonusAmount());        // 优惠金额
        accountInfoDto.setMultipleWater(ruleDto.getMultipleWater());    // 流水倍数
        accountInfoDto.setMinAmount(ruleDto.getMinAmount());            // 存款需求

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
                    throw new R200Exception("未配置任务");
                }
                int count = taskMapper.findBlackListByAccountId(config.getFinancialCode(), accountId);
                if (count > 0) {
                    log.info(loginName + "黑名单" + config.getFinancialCode());
                    throw new R200Exception("您不可以领取该任务");
                }
                if (QD_TASK_ACTIVITY.equals(config.getFinancialCode())) {
                    taskSignin(config, accountId, loginName);
                }
                if (SJ_TASK_ACTIVITY.equals(config.getFinancialCode())) {
                    upgradeAwards(config, accountId, loginName, level);
                }
                // 完善资料
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
            errorMsg.append("未绑定银行卡！");
        }
        if (Boolean.TRUE.equals(rewardDto.getIsMobile()) && Boolean.FALSE.equals(rewardDto.getAccountInfoDto().getIsMobile())) {
            errorMsg.append("未绑定手机号！");
        }
        if (Boolean.TRUE.equals(rewardDto.getIsName()) && Boolean.FALSE.equals(rewardDto.getAccountInfoDto().getIsName())) {
            errorMsg.append("未填写真实姓名！");
        }
        if (Boolean.TRUE.equals(rewardDto.getIsMail()) && Boolean.FALSE.equals(rewardDto.getAccountInfoDto().getIsMail())) {
            errorMsg.append("未绑定邮箱!");
        }
        BigDecimal dayDepositAmount = taskMapper.findDepositamountTsk(accountId, getCurrentDate(FORMAT_10_DATE));
        BigDecimal validbet = taskMapper.findValidbetTsk(loginName, getCurrentDate(FORMAT_10_DATE));
        TaskActiveFilesDto activeFilesDto = getTaskActiveDayDto(dayDepositAmount, validbet, rewardDto);
        if (isNull(activeFilesDto)) {
        	errorMsg.append("请先完成签到");
        	List<TaskActiveFilesDto> activeDayDtos = rewardDto.getFilesDtos();
        	TaskActiveFilesDto taskActiveFilesDto = activeDayDtos.stream().filter(t -> t.getNum() == 1).findAny().get();
        	BigDecimal depositAmount = taskActiveFilesDto.getDepositAmount();
        	if (dayDepositAmount.compareTo(depositAmount) == -1) {
        		errorMsg.append("存款:" + dayDepositAmount + "/" + depositAmount);
        	}
        	if (validbet.compareTo(taskActiveFilesDto.getValidBet()) == -1) {
        		errorMsg.append("流水:" + validbet + "/" + taskActiveFilesDto.getValidBet());
        	}
        }
        String errorMsgStr = errorMsg.toString();
        if (StringUtil.isNotEmpty(errorMsgStr)) {
        	throw new R200Exception(errorMsgStr);
        }
        List<TaskActiveDayDto> dayDtoList = activeFilesDto.getDayDtoList();
        Collections.sort(dayDtoList, Comparator.comparing(TaskActiveDayDto::getDay).reversed());
        if (rewardDto.getCycle() == 0 && dayDtoList.get(0).getDay() == rewardDto.getNum()) {
        	throw new R200Exception("已经领取过,不可以再次领取!");
        }
        /*if (nonNull(rewardDto.getDepositAmount()) && dayDepositAmount.compareTo(rewardDto.getDepositAmount()) == -1) {
            throw new R200Exception("当日存款不符合领取条件");
        }
        if (nonNull(rewardDto.getValidBet()) && validbet.compareTo(rewardDto.getValidBet()) == -1) {
            throw new R200Exception("当日有效投注不符合领取条件");
        }*/
        String data1 = getCurrentDate(FORMAT_10_DATE);
        if (data1.equals(rewardDto.getDay())) {
        	throw new R200Exception("今天已经领取过奖励!");
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
     * @param dayDepositAmount 当日充值额
     * @param validbet 当日投注额
     * @param rewardDto  
     * @return
     */
    private TaskActiveFilesDto getTaskActiveDayDto(BigDecimal dayDepositAmount, BigDecimal validbet, TaskActiveRewardDto rewardDto) {
        List<TaskActiveFilesDto> activeDayDtos = rewardDto.getFilesDtos();
        Collections.sort(activeDayDtos, Comparator.comparing(TaskActiveFilesDto::getNum).reversed());
        for (TaskActiveFilesDto filesDto : activeDayDtos) {
        	// 存款，投注不小于配置的金额
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
            throw new R200Exception("不符合领取条件");
        }
        insertTaskBonus(accountId, loginName, taskTimeDto.getBonusAmount(), taskTimeDto.getMultipleWater(),
                config.getId(), DS_TASK_ACTIVITY, null);
    }

    private void accountInfoBonusAmount(TaskConfig config, Integer accountId, String loginName) {
        // 规则dto
        TaskAccountDto ruleDto = JSON.parseObject(config.getRule(), TaskAccountDto.class);
        // 获取规则校验结果
        TaskAccountInfoDto accountInfoDto = setAccountInfoDto(accountId, config);
        if (Boolean.TRUE.equals(accountInfoDto.getIsBonus())) {
            throw new R200Exception("已经领取该红利");
        }
        if (Boolean.TRUE.equals(ruleDto.getIsBank()) && Boolean.FALSE.equals(accountInfoDto.getIsBank())) {
            throw new R200Exception("不符合领取条件");
        }
        if (Boolean.TRUE.equals(ruleDto.getIsMobile()) && Boolean.FALSE.equals(accountInfoDto.getIsMobile())) {
            throw new R200Exception("不符合领取条件");
        }
        if (Boolean.TRUE.equals(ruleDto.getIsName()) && Boolean.FALSE.equals(accountInfoDto.getIsName())) {
            throw new R200Exception("不符合领取条件");
        }
        if (Boolean.FALSE.equals(accountInfoDto.getIsMinAmount())) {
            throw new R200Exception("存款未满足！");
        }
        insertTaskBonus(accountId, loginName, ruleDto.getBonusAmount(), ruleDto.getMultipleWater(),
                config.getId(), ZL_TASK_ACTIVITY, null);
    }

    private void upgradeAwards(TaskConfig config, Integer accountId, String loginName, Integer level) {
        if (isNull(level) || level < 1) {
            throw new R200Exception(level + "不可领取");
        }
        Integer accountLevel = taskMapper.findTaskAccountLevel(accountId);
        List<Integer> drawAccountLevels = taskMapper.findDrawAccountLevel(accountId);
        Optional<Integer> drawAccountLevel = drawAccountLevels.stream().filter(ts -> ts.equals(level)).findAny();
        if (drawAccountLevel.isPresent()) {
            throw new R200Exception("已经领取该奖励");
        }
        if (accountLevel < level || "0".equals(accountLevel)) {
            throw new R200Exception("该奖励不可领取");
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
            throw new R200Exception("今天已经签到了");
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
                throw new R200Exception("配置错误");
            }
        } else {
            taskSignin1.setNumber(1);
            Optional<TaskSigninListDto> signinListDto = ruleDto.getSigninDtos().stream().filter(ts -> 1 == ts.getDay()).findAny();
            if (signinListDto.isPresent()) {
                amount = signinListDto.get().getAmount();
            } else {
                throw new R200Exception("配置错误");
            }
        }
        if (amount.compareTo(BigDecimal.ZERO) != 1) {
            throw new R200Exception("配置错误");
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
        // 生成文件导出记录
        SysFileExportRecord record = sysFileExportRecordService.saveAsynFileExportRecord(userId, bounsStatisticalModule);

        // 不存在导出文件，则生成并上传导出文件
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
        // 查询用户的module下载记录
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
