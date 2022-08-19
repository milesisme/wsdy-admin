
package com.wsdy.saasops.modules.lottery.serivce;

import com.google.common.collect.Lists;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.modules.user.service.SdyActivityService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.base.mapper.BaseMapper;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.log.dao.LogMbrregisterMapper;
import com.wsdy.saasops.modules.log.entity.LogMbrLogin;
import com.wsdy.saasops.modules.log.entity.LogMbrRegister;
import com.wsdy.saasops.modules.log.mapper.LogMapper;
import com.wsdy.saasops.modules.lottery.dao.OprActLotteryMapper;
import com.wsdy.saasops.modules.lottery.dto.*;
import com.wsdy.saasops.modules.lottery.entity.OprActLottery;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.AccountAutoCastService;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.SdyActivityMapper;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.common.constants.SystemConstants.LOTTERY_CONFIG;
import static com.wsdy.saasops.common.utils.DateUtil.*;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.lotteryCode;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.vipRedenvelopeCode;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Service
public class LotteryActivityService {

    @Autowired
    private SdyActivityService sdyActivityService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private LogMbrregisterMapper logMbrregisterMapper;
    @Autowired
    private AccountAutoCastService autoCastService;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private SdyActivityMapper sdyActivityMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private OprActLotteryMapper lotteryMapper;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private OprActBonusMapper oprActBonusMapper;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private LogMapper logMapper;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private OprActActivityService actActivityService;

    public List<LotteryAreaDto> lotteryInfo(Integer accountId, String mainDomain, String siteCode) {
        OprActActivity actActivity = sdyActivityService.getOprActActivity(lotteryCode);
        if (isNull(actActivity)) {
            return null;
        }
        LotteryActivityDto activityDto = jsonUtil.fromJson(actActivity.getRule(), LotteryActivityDto.class);
        // 抽奖区
        List<LotteryAreaDto> lotteryAreaDtos = activityDto.getLotteryAreaDtos();
        // 未登录返回
        if (isNull(accountId)) {
            for (LotteryAreaDto lotteryAreaDto : lotteryAreaDtos) {
                lotteryAreaDto.setDomains(null);
                lotteryAreaDto.setRegisterDomains(null);
                lotteryAreaDto.setActivityId(actActivity.getId());
            }
            return lotteryAreaDtos;
        }

        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(siteCode);

        for (LotteryAreaDto lotteryAreaDto : lotteryAreaDtos) {
            // 返回数据处理
            setLotteryAreaDto(lotteryAreaDto, account, sitePrefix,actActivity);
            lotteryAreaDto.setDomains(null);
            lotteryAreaDto.setRegisterDomains(null);
            lotteryAreaDto.setActivityId(actActivity.getId());
        }
        return lotteryAreaDtos;
    }

    public LotteryResultDto accountLottery(Integer accountId, String mainDomain, Integer prizeArea, String siteCode, String ip) {
        LotteryResultDto resultDto = new LotteryResultDto();

        OprActActivity actActivity = sdyActivityService.getOprActActivity(lotteryCode);
        if (isNull(actActivity)) {
            throw new R200Exception("活动暂未开启");
        }
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        LotteryActivityDto activityDto = jsonUtil.fromJson(actActivity.getRule(), LotteryActivityDto.class);
        List<LotteryAreaDto> lotteryAreaDtos = activityDto.getLotteryAreaDtos();
        if (lotteryAreaDtos.size() == 0) {
            throw new R200Exception("奖区不存在");
        }
        Optional<LotteryAreaDto> levelDtoOptional = lotteryAreaDtos.stream().filter(e ->
                e.getPrizeArea().equals(prizeArea)).findAny();
        if (!levelDtoOptional.isPresent()) {
            throw new R200Exception("请求奖区错误");
        }
        List<String> sitePrefix = baseMapper.getApiPrefixBySiteCode(siteCode);
        LotteryAreaDto areaDto = levelDtoOptional.get();
        setLotteryAreaDto(areaDto, account,sitePrefix,actActivity);

        //校验会员账号是否被添加进所有活动黑名单
        if (actActivityService.isBlackList(account.getId(), TOpActtmpl.allActivityCode)){
            throw new R200Exception("您不符合抽奖资格");
        }
        //校验会员对应的代理是否被添加进所有活动黑名单
        if (actActivityService.valAgentBackList(account, TOpActtmpl.allActivityCode)){
            throw new R200Exception("您不符合抽奖资格");
        }

        if (areaDto.getRemainingTimes() == Constants.EVNumber.zero) {
            throw new R200Exception("您不符合抽奖资格");
        }
        OprActLottery oprActLottery = getOprActLottery(areaDto, siteCode, actActivity.getId(), account, areaDto.getPrizeArea());
        if (isNull(oprActLottery)) {
            throw new R200Exception("活动配置错误");
        }
        if (oprActLottery.getPrizetype() != Constants.EVNumber.one) {
            OprActBonus bonus = oprActActivityCastService.setOprActBonus(account.getId(),
                    account.getLoginName(), actActivity.getId(),
                    null, null, actActivity.getRuleId());
            bonus.setCreateUser(account.getLoginName());
            bonus.setIp(ip);
            bonus.setLotteryId(oprActLottery.getId());
            bonus.setDevSource(account.getLoginSource());
            bonus.setSource(Constants.EVNumber.zero);
            if (oprActLottery.getPrizetype() == Constants.EVNumber.two) {
                bonus.setDiscountAudit(new BigDecimal(oprActLottery.getDiscountAudit()));
                bonus.setBonusAmount(oprActLottery.getDonateamount());
                bonus.setPrizename(oprActLottery.getPrizename());
                bonus.setAuditAmount(auditAccountService.getAddAuditAmount(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
            } else {
                bonus.setPrizetype(Constants.EVNumber.one);
                bonus.setPrizename(oprActLottery.getPrizename());
            }
            oprActBonusMapper.insert(bonus);
            if (actActivity.getIsAudit() == Constants.EVNumber.zero) {
                bonus.setAuditUser(Constants.SYSTEM_USER);
                bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                if (oprActLottery.getPrizetype() == Constants.EVNumber.two) {
                    oprActActivityCastService.auditOprActBonus(bonus,
                            OrderConstants.ACTIVITY_CJL, actActivity.getActivityName(), Boolean.TRUE);
                } else {
                    oprActActivityCastService.auditLotteryActBonus(bonus);
                }
            }
        }
        resultDto.setPrizeType(oprActLottery.getPrizetype());
        resultDto.setPrizeName(oprActLottery.getPrizename());
        resultDto.setDonateAmount(oprActLottery.getDonateamount());
        return resultDto;
    }

    public OprActLottery getOprActLottery(LotteryAreaDto areaDto, String siteCode, Integer activityId, MbrAccount account, Integer prizeArea) {
        while (true) {
            String key = RedisConstants.ACCOUNT_ADDLOTTERY + siteCode + activityId;
            Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(isExpired)) {
                try {
                    List<OprActLottery> oprActLotteryList = sdyActivityMapper.findLotteryList(prizeArea);
                    if (oprActLotteryList.size() > 0) {
                        OprActLottery oprActLottery = oprActLotteryList.get(0);
                        oprActLottery.setAccountId(account.getId());
                        oprActLottery.setLoginName(account.getLoginName());
                        oprActLottery.setUpdatetime(getCurrentDate(FORMAT_18_DATE_TIME));
                        lotteryMapper.updateByPrimaryKeySelective(oprActLottery);
                        return oprActLottery;
                    }

                    List<LotteryPrizeAreaDto> prizeAreaDtoList = areaDto.getPrizeAreaDtos();
                    if (prizeAreaDtoList.size() == 0) {
                        return null;
                    }
                    SysSetting sysSetting = sysSettingService.getSysSetting(LOTTERY_CONFIG);
                    if (isNull(sysSetting)) {
                        return null;
                    }
                    addOprActLottery(prizeAreaDtoList, sysSetting, activityId, prizeArea);
                    List<OprActLottery> lotteries = sdyActivityMapper.findLotteryList(prizeArea);
                    OprActLottery oprActLottery = lotteries.get(0);
                    oprActLottery.setAccountId(account.getId());
                    oprActLottery.setLoginName(account.getLoginName());
                    oprActLottery.setUpdatetime(getCurrentDate(FORMAT_18_DATE_TIME));
                    lotteryMapper.updateByPrimaryKeySelective(oprActLottery);
                    return oprActLottery;
                } finally {
                    redisService.del(key);
                }
            }
        }
    }

    @Transactional
    public void addOprActLottery(List<LotteryPrizeAreaDto> prizeAreaDtoList, SysSetting sysSetting, Integer activityId, Integer prizeArea) {
        int maxBatchnumber = sdyActivityMapper.findLotteryMax(prizeArea);

        String[] str = sysSetting.getSysvalue().split(",");
        List<Integer> integers = randomNum(1, Integer.parseInt(str[1]));
        Integer st = Integer.parseInt(str[0]);

        Collections.sort(prizeAreaDtoList, Comparator.comparing(LotteryPrizeAreaDto::getProbability).reversed());
        List<OprActLottery> lotteryList = Lists.newArrayList();
        for (int i = 0; i < prizeAreaDtoList.size(); i++) {
            LotteryPrizeAreaDto prizeAreaDto = prizeAreaDtoList.get(i);
            Integer pro = prizeAreaDto.getProbability();
            if (st > 0) {
                pro = pro * st;
            }
            if (pro == 0) {
                continue;
            }
            List<Integer> lotteryRandoms = Lists.newArrayList();
            for (int j = 0; j < pro; j++) {
                if (integers.size() == j) {
                    break;
                }
                lotteryRandoms.add(integers.get(j));
            }
            for (int j = 0; j < lotteryRandoms.size(); j++) {
                integers.remove(lotteryRandoms.get(j));
            }
            for (Integer ra : lotteryRandoms) {
                OprActLottery lottery = insertOprActLottery(activityId, ra, prizeAreaDto, maxBatchnumber, prizeArea);
                lotteryList.add(lottery);
                if (lotteryList.size() == 500) {
                    lotteryMapper.insertList(lotteryList);
                    lotteryList = Lists.newArrayList();
                }
            }
        }
        LotteryPrizeAreaDto prizeAreaDto = new LotteryPrizeAreaDto();
        prizeAreaDto.setPrizeType(Constants.EVNumber.one);
        prizeAreaDto.setPrizeName("谢谢参与");
        for (Integer its : integers) {
            OprActLottery lottery = insertOprActLottery(activityId, its, prizeAreaDto, maxBatchnumber, prizeArea);
            lotteryList.add(lottery);
            if (lotteryList.size() == 500) {
                lotteryMapper.insertList(lotteryList);
                lotteryList = Lists.newArrayList();
            }
        }
        if (lotteryList.size() > 0) {
            lotteryMapper.insertList(lotteryList);
        }
    }

    private OprActLottery insertOprActLottery(Integer activityId, Integer ra, LotteryPrizeAreaDto prizeAreaDto,
                                              int maxBatchnumber, Integer prizeArea) {
        OprActLottery oprActLottery = new OprActLottery();
        oprActLottery.setActivityid(activityId);
        oprActLottery.setRandom(ra);
        oprActLottery.setPrizetype(prizeAreaDto.getPrizeType());
        oprActLottery.setPrizename(prizeAreaDto.getPrizeName());
        oprActLottery.setDonateamount(prizeAreaDto.getDonateAmount());
        oprActLottery.setCreatetime(getCurrentDate(FORMAT_18_DATE_TIME));
        oprActLottery.setBatchnumber(maxBatchnumber + 1);
        if (nonNull(prizeAreaDto.getMultipleWater())) {
            oprActLottery.setDiscountAudit(prizeAreaDto.getMultipleWater().intValue());
        }
        oprActLottery.setPrizeArea(prizeArea);
        return oprActLottery;
    }

    private void setLotteryAreaDto(LotteryAreaDto lotteryAreaDto, MbrAccount account,List<String> sitePrefix, OprActActivity actActivity) {
        // 默认次数0
        lotteryAreaDto.setRemainingTimes(Constants.EVNumber.zero);

        // 1.校验
        // 申请信息校验
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(account,
                Constants.EVNumber.zero, lotteryAreaDto.getIsName(),
                lotteryAreaDto.getIsBank(), lotteryAreaDto.getIsMobile(), lotteryAreaDto.getIsMail(), false);
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            return;
        }
        // 会员等级校验
        if (Boolean.FALSE.equals(isActLevelId(lotteryAreaDto.getActLevelIds(), account.getActLevelId()))) {
            return;
        }
        // 注册来源和时间校验
        LogMbrRegister register = new LogMbrRegister();
        register.setAccountId(account.getId());
        LogMbrRegister logMbrRegister = logMbrregisterMapper.selectOne(register);
        if (Boolean.FALSE.equals(isDomains(lotteryAreaDto.getDomains(), logMbrRegister))) {
            return;
        }
        if (Boolean.FALSE.equals(isrRegisterCondition(lotteryAreaDto, account))) {
            return;
        }

        // 2. 次数计算 按活动期间和当日/当周 两种规则
        if(Objects.isNull(lotteryAreaDto.getCycle())){
            return;
        }
        if (Constants.EVNumber.three == lotteryAreaDto.getCycle()) {    // 活动期间
            // 计算：每日首次登录 即活动期间登录天数
            Integer num = getFirstLoginDay(lotteryAreaDto,account, actActivity);
            // 计算：是否活动期间APP注册登录
            num = num + getFirstLogin(lotteryAreaDto,account, actActivity);
            // 计算：是否活动期间首次绑定银行卡
            num = num + getFirstBindBank(lotteryAreaDto,account, actActivity);

            // 已领取次数和剩余次数
            int count = sdyActivityMapper.countLotteryByAccountId(account.getId(), lotteryAreaDto.getPrizeArea(),
                    actActivity.getUseStart(), actActivity.getUseEnd());
            if (count == num || count > num) {
                num = Constants.EVNumber.zero;
            }
            if (count < num) {
                num = num - count;
            }
            lotteryAreaDto.setRemainingTimes(num);
        }else{  // 当日/当周
            // 计算：从域名注册赠送次数
            Integer num = getRegisterNum(lotteryAreaDto, account, lotteryAreaDto.getCycle(), logMbrRegister);
            // 计算：累计充值、首次单笔充值、有效投注条件及赠送次数
            Map<String, String> timeMap = getStartTimeAndEndTime(lotteryAreaDto.getCycle());
            num = num + getDepositNum(lotteryAreaDto, account, timeMap, sitePrefix);
            // 已领取次数和剩余次数
            int count = sdyActivityMapper.countLotteryByAccountId(account.getId(), lotteryAreaDto.getPrizeArea(),
                    timeMap.get("startTime"), timeMap.get("endTime"));
            if (count == num || count > num) {
                num = Constants.EVNumber.zero;
            }
            if (count < num) {
                num = num - count;
            }
            lotteryAreaDto.setRemainingTimes(num);
        }
    }

    private Integer getDepositNum(LotteryAreaDto dto, MbrAccount account, Map<String, String> timeMap, List<String> sitePrefix) {
        List<LotteryDepositDto> depositDtoList = dto.getDepositDtos();
        Integer num = Constants.EVNumber.zero;
        if (Collections3.isNotEmpty(depositDtoList)) {
            BigDecimal betBigDecimal = autoCastService.getValidVet(sitePrefix, account.getLoginName(),
                    timeMap.get("startTime"), timeMap.get("endTime"));
            BigDecimal depositBigDecimal = fundMapper.sumFundDepositByAccountId(account.getId(),
                    timeMap.get("startTime"), timeMap.get("endTime"));
            for (LotteryDepositDto depositDto : depositDtoList) {
                if (nonNull(depositDto.getIsSelected()) && depositDto.getIsSelected() == Constants.EVNumber.one) {
                    if (depositDto.getSign() == Constants.EVNumber.zero
                            && depositDto.getAmountConditions().compareTo(depositBigDecimal) != 1) {
                        num = num + depositDto.getNum();
                    }
                    if (depositDto.getSign() == Constants.EVNumber.one) {
                        FundDeposit deposit = sdyActivityMapper.findLotterFundDeposit(account.getId(), timeMap.get("startTime"), timeMap.get("endTime"));
                        if (nonNull(deposit) && deposit.getActualArrival().compareTo(depositDto.getAmountConditions()) != -1) {
                            num = num + depositDto.getNum();
                        }
                    }
                    if (depositDto.getSign() == Constants.EVNumber.two
                            && depositDto.getAmountConditions().compareTo(betBigDecimal) != 1) {
                        num = num + depositDto.getNum();
                    }
                }
            }
        }
        return num;
    }

    private Integer getRegisterNum(LotteryAreaDto dto, MbrAccount account, Integer cycle, LogMbrRegister logMbrRegister) {
        if (Boolean.TRUE.equals(dto.getDomainsCondition())) {
            // 注册时间
            String registerTime = account.getRegisterTime();
            Date start = DateUtil.parse(registerTime, FORMAT_18_DATE_TIME);
            // 注册时间是否为当日
            Boolean istoday = isToday(start.getTime());
            // 注册时间是否为当周
            Boolean isThisWeek = isThisWeek(start.getTime());
            if (Constants.EVNumber.one == cycle && Boolean.FALSE.equals(isThisWeek)) {
                return Constants.EVNumber.zero;
            }
            if (Constants.EVNumber.zero == cycle && Boolean.FALSE.equals(istoday)) {
                return Constants.EVNumber.zero;
            }
            if (nonNull(logMbrRegister) && StringUtils.isNotEmpty(logMbrRegister.getRegisterUrl())) {
                String[] str = dto.getRegisterDomains().split(",");
                for (String domain : str) {
                    if (logMbrRegister.getRegisterUrl().equals(domain)
                            || logMbrRegister.getRegisterUrl().contains(domain)
                            || domain.contains(logMbrRegister.getRegisterUrl())) {
                        return dto.getNum();
                    }
                }
            }
        }
        return Constants.EVNumber.zero;
    }

    private Boolean isrRegisterCondition(LotteryAreaDto dto, MbrAccount account) {
        String registerTime = format(parse(account.getRegisterTime(), FORMAT_18_DATE_TIME), FORMAT_18_DATE_TIME);
        if (dto.getRegisterCondition() == Constants.EVNumber.zero) {
            return Boolean.TRUE;
        }
        if (dto.getRegisterCondition() == Constants.EVNumber.one) {
            int num = daysBetween(registerTime, getCurrentDate(FORMAT_18_DATE_TIME))+1;
            if (num > dto.getRegisterNum()) {
                return Boolean.FALSE;
            }
        }
        if (dto.getRegisterCondition() == Constants.EVNumber.two) {
            int num = DateUtil.timeCompare(registerTime, dto.getRegisterDate(), DateUtil.FORMAT_18_DATE_TIME);
            if (num < 0) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    private Boolean isActLevelId(List<Integer> actLevelIds, Integer actLevelId) {
        if (Collections3.isNotEmpty(actLevelIds)) {
            for (Integer id : actLevelIds) {
                if (id.equals(actLevelId)) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private Boolean isDomains(String domains, LogMbrRegister logMbrRegister) {
        if (StringUtils.isNotEmpty(domains)) {
            String[] str = domains.split(",");
            if (nonNull(logMbrRegister) && StringUtils.isNotEmpty(logMbrRegister.getRegisterUrl())) {
                String mainDomain = logMbrRegister.getRegisterUrl();
                for (String domain : str) {
                    if (mainDomain.equals(domain)
                            || mainDomain.contains(domain)
                            || domain.contains(mainDomain)) {
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private Integer getFirstLoginDay(LotteryAreaDto dto, MbrAccount account, OprActActivity actActivity) {
        if (Boolean.TRUE.equals(dto.getIsSelectedFirstLoginDay())) {
            // 统计会员活动期间的登录天数
            // 活动结束的时间为实际后一天的 00:00:00,此处处理为前一天
            String useEnd = DateUtil.getPostDayTime(actActivity.getUseEnd(),  - 1, DateUtil.FORMAT_10_DATE);
            Integer loginDays = logMapper.loginDays(account.getId(),actActivity.getUseStart(),useEnd);
            return dto.getNumFirstLoginDay().intValue() * loginDays.intValue();
        }
        return Constants.EVNumber.zero;
    }
    private Integer getFirstLogin(LotteryAreaDto dto, MbrAccount account, OprActActivity actActivity) {
        if (Boolean.TRUE.equals(dto.getIsSelectedFirstLogin())) {
            // 是否APP注册
            LogMbrRegister register = new LogMbrRegister();
            register.setAccountId(account.getId());
            LogMbrRegister logMbrRegister = logMbrregisterMapper.selectOne(register);
            if(logMbrRegister.getRegisterSource() != Constants.EVNumber.four){
                return Constants.EVNumber.zero;
            }
            // 查询首次登录时间
            LogMbrLogin logMbrLogin = logMapper.getFirstLogin(account.getId());
            if(Objects.isNull(logMbrLogin)){
                return Constants.EVNumber.zero;
            }
            // 活动开始时间 > 首次登录时间
            if(actActivity.getUseStart().compareTo(logMbrLogin.getLoginTime()) > 0){
                return Constants.EVNumber.zero;
            }

            return dto.getNumFirstLogin();
        }
        return Constants.EVNumber.zero;
    }
    private Integer getFirstBindBank(LotteryAreaDto dto, MbrAccount account, OprActActivity actActivity) {
        if (Boolean.TRUE.equals(dto.getIsSelectedFirstBindBank())) {
            // 查询首次绑定的银行卡
            MbrBankcard mbrBankcard = mbrMapper.findAccountFirstBankcard(account.getId());
            if(Objects.isNull(mbrBankcard)){
                return Constants.EVNumber.zero;
            }
            // 活动开始时间 > 首次绑定时间
            if(actActivity.getUseStart().compareTo(mbrBankcard.getCreateTime()) > 0){
                return Constants.EVNumber.zero;
            }

            return dto.getNumFirstBindBank();
        }
        return Constants.EVNumber.zero;
    }

    public static void main(String[] args) {
        List<Integer> lst = randomNum(1, 10);
        System.out.println("Array: " + lst.toString());
        System.out.println("Size: " + lst.size());
        // lst.sort(Comparator.naturalOrder());

        List<LotteryPrizeAreaDto> dtoList = Lists.newArrayList();

        LotteryPrizeAreaDto lotteryPrizeAreaDto = new LotteryPrizeAreaDto();
        lotteryPrizeAreaDto.setProbability(5);
        dtoList.add(lotteryPrizeAreaDto);

        LotteryPrizeAreaDto lotteryPrizeAreaDto1 = new LotteryPrizeAreaDto();
        lotteryPrizeAreaDto1.setProbability(3);
        dtoList.add(lotteryPrizeAreaDto1);

        LotteryPrizeAreaDto lotteryPrizeAreaDto2 = new LotteryPrizeAreaDto();
        lotteryPrizeAreaDto2.setProbability(1);
        dtoList.add(lotteryPrizeAreaDto2);

        for (int i = 0; i < dtoList.size(); i++) {
            Integer pro = dtoList.get(i).getProbability();
            List<Integer> lotteryList = Lists.newArrayList();

            for (int j = 0; j < pro; j++) {
                if (lst.size() == j) {
                    break;
                }
                lotteryList.add(lst.get(j));
            }
            for (int j = 0; j < lotteryList.size(); j++) {
                lst.remove(lotteryList.get(j));
            }
          /*  System.out.println("lst=="+lst);
            System.out.println("lst=size"+lst.size());*/
            System.out.println("lotteryList==" + lotteryList);
            System.out.println("lst==" + lst);
            // System.out.println("lotteryList=size"+lotteryList.size());
        }
    }


    /**
     * 首先生成一个不重复的数集（例如：0~9999），然后每次生成随机索引，
     * 并且与当前索引进行交换(swap)，最后返回目标List对象（也可用其他类型实现）;
     *
     * @param begin 区间开始
     * @param end   区间结束
     * @return 返回目标List
     */
    public static List<Integer> randomNum(int begin, int end) {
        Random rd = new Random();
        List<Integer> lst = new ArrayList<>();//存放有序数字集合
        for (int i = begin; i <= end; i++) {
            lst.add(i);
        }
        int count = 0;
        for (Integer element : lst) {
            int index = rd.nextInt(lst.size() - count) + count;//随机索引
            lst.set(count, lst.get(index));
            lst.set(index, element);
            count++;
        }
        return lst;
    }

    private Map<String, String> getStartTimeAndEndTime(Integer cycle) {
        Map<String, String> timeMap = new HashMap<>();
        String startTime = null;
        String endTime = null;
        if (Constants.EVNumber.one == cycle) {
            startTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME, 0, 0);//本周第一天
            endTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME, -1, 0);//下周第一天
        } else {
            startTime = DateUtil.getPastDate(0, DateUtil.FORMAT_10_DATE) + " 00:00:00";
            endTime = DateUtil.getPastDate(-1, DateUtil.FORMAT_10_DATE) + " 00:00:00";
        }
        timeMap.put("startTime", startTime);
        timeMap.put("endTime", endTime);
        return timeMap;
    }

}
