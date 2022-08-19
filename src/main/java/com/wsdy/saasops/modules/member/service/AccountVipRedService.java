package com.wsdy.saasops.modules.member.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_10_DATE;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static com.wsdy.saasops.common.utils.DateUtil.getPastDate;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.vipRedenvelopeCode;
import static java.util.Objects.isNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.operate.dto.MemDayGiftDto;
import com.wsdy.saasops.modules.operate.dto.MemDayRuleScopeDto;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.operate.mapper.SdyActivityMapper;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.wsdy.saasops.modules.operate.service.OprAppDownloadActivityService;
import com.wsdy.saasops.modules.operate.service.OprMemDayActivityService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class AccountVipRedService {

    @Autowired
    private SdyActivityMapper sdyActivityMapper;
    @Autowired
    private OperateActivityMapper activityMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private OprMemDayActivityService oprMemDayActivityService;
    @Autowired
    private OprActActivityService actActivityService;
    @Autowired
    private OprAppDownloadActivityService oprAppDownloadActivityService;

    public void castVipRedActivity(String siteCode) {
        log.info("vipRedActivityTaskAsync--【" + siteCode + "】自动发放红包==开始");
        batchVipRed(siteCode);
        log.info("vipRedActivityTaskAsync--【" + siteCode + "】本次自动发放红包==结束");
    }

    // 按批次发放
    public void batchVipRed(String siteCode) {
        List<OprActActivity> activities = getOprActActivitys(null);
        Integer lastId = 0; // 每次的最大ID
        while (true) {
            List<MbrAccount> accounts = sdyActivityMapper.findAccountLevelList(lastId);
            // 查不到数据后结束循环
            if (accounts == null || accounts.isEmpty() || accounts.size() <= 0) {
                log.info("vipRedActivityTaskAsync--【" + siteCode + "】自动发放红包while循环获取数据为空==结束");
                break;
            }
            log.info("vipRedActivityTaskAsync--【" + siteCode + "】自动发放红包while循环==开始==数据量=={}开始ID{}", accounts.size(), lastId);
            lastId = accounts.get(accounts.size()-1).getId();

            for (OprActActivity as : activities) {
                MemDayGiftDto giftDto = jsonUtil.fromJson(as.getRule(), MemDayGiftDto.class);
                if (isNull(giftDto)) {
                    continue;
                }
                Boolean memDayCheckout = oprMemDayActivityService.checkoutMemDay(giftDto);
                if (!memDayCheckout) {
                    continue;
                }
                accounts.stream().forEach(at -> vipRedActivityTaskAsync(at, siteCode, as, giftDto));
            }
        }
    }

    public static void main(String[] args) {
        List<Integer> test = Arrays.asList(1,2,3);
        System.out.println(test.get(test.size()-1));
    }

    @Async("vipRedActivityTaskAsyncExecutor")
    @Transactional
    public void vipRedActivityTaskAsync(MbrAccount account, String siteCode, OprActActivity activity, MemDayGiftDto giftDto) {
        log.info("【" + siteCode + "】自动发放红包==account==" + account.getLoginName() + "==castMbrValidbet==activity==" + activity.getId() + "==开始");
        String key = RedisConstants.ACCOUNT_VIPRED + activity.getId() + siteCode + account.getLoginName();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, account.getLoginName(), 200, TimeUnit.SECONDS);
        if (!isExpired) {
			log.info(siteCode + "vipRedActivityTaskAsync--发送红包失败,会员:" + account.getLoginName() + "==未获取到锁，跳过发放");
			return;
		}
        try {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
	        //检查被发放的账号是不是在黑名单
	        if(actActivityService.isBlackList(account.getId(),vipRedenvelopeCode)||actActivityService.isBlackList(account.getId(), TOpActtmpl.allActivityCode)){
	            log.info(siteCode + "vipRedActivityTaskAsync--发送红包失败,会员:" + account.getLoginName()+"是VIP红包黑名单会员");
	            return;
	        }

	        //判断是否存在vip活动代理黑名单
	        if (actActivityService.valAgentBackList(account,vipRedenvelopeCode)){
	            log.info(siteCode + "vipRedActivityTaskAsync--发送红包失败,会员:" + account.getLoginName()+"上级代理存在VIP活动黑名单");
	            return;
	        }
	        //判断是否存在所有活动黑名单
	        if (actActivityService.valAgentBackList(account,TOpActtmpl.allActivityCode)){
	            log.info(siteCode + "vipRedActivityTaskAsync--发送红包失败,会员:" + account.getLoginName()+"上级代理存在所有活动黑名单");
	            return;
	        }

            int countWater = activityMapper.findValidbetCount(activity.getId(), getCurrentDate(FORMAT_10_DATE), Constants.SYSTEM_USER, account.getId());
            if (countWater > 0) {
                log.info(siteCode + "vipRedActivityTaskAsync--发送红包失败,会员:" + account.getLoginName() + "==该会员今日已经自动发放红包");
                return;
            }
            applyVipRedActivity(giftDto, activity, account);
	        log.info("vipRedActivityTaskAsync--【" + siteCode + "】自动发放红包结束==account==" + account.getLoginName());
        } catch (Exception e) {
        	log.error("siteCode:" + siteCode + ",vipRedActivityTaskAsync--自动发放红包失败,会员:" + account.getLoginName(), e);
        } finally {
			redisService.del(key);
		}
    }

    public void applyVipRedActivity(MemDayGiftDto giftDto, OprActActivity actActivity, MbrAccount account) {
        // 申请条件校验
        String isAccountMsg = oprActActivityCastService.checkoutAccountMsg(account, Constants.EVNumber.zero, giftDto.getIsName(),
                giftDto.getIsBank(), giftDto.getIsMobile(), false, false);
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            log.info("vipRedActivityTaskAsync--发送红包失败,会员:" + account.getLoginName() + "==未满足绑定信息效验，跳过发放");
            return;
        }
        // 是否领取校验
        Boolean isBonus = oprMemDayActivityService.checkoutMemDayGiftBonus(actActivity, account);
        if (!isBonus) {
            log.info("vipRedActivityTaskAsync--发送红包失败,会员:" + account.getLoginName() + "==未满足是否已领取效验，跳过发放");
            return;
        }
        // 层级条件校验
        MemDayRuleScopeDto ruleScopeDto = oprMemDayActivityService.getRuleScopeDtos(giftDto.getRuleScopeDtos(), account.getActLevelId(), giftDto.getScope());
        if (isNull(ruleScopeDto)) {
            log.info("vipRedActivityTaskAsync--发送红包失败,会员:" + account.getLoginName() + "==未满足层级效验，跳过发放");
            return;
        }
        // 存款限制
        Boolean depositCheckout = oprAppDownloadActivityService.checkoutRuleDeposit(account, ruleScopeDto);
        if (!depositCheckout) {
            log.info("vipRedActivityTaskAsync--发送红包失败,会员:" + account.getLoginName() + "==未满足存款效验，跳过发放");
            return;
        }
        // 投注限制
        Boolean validBetCheckout = oprMemDayActivityService.checkoutRuleValidBet(account, ruleScopeDto);
        if (!validBetCheckout) {
            log.info("vipRedActivityTaskAsync--发送红包失败,会员:" +  account.getLoginName() + "==未满足投注效验，跳过发放");
            return;
        }
        // 生成红利数据
        // 赠送金额和流水倍数校验
        if (Objects.isNull(ruleScopeDto.getDonateAmount()) || Objects.isNull(ruleScopeDto.getMultipleWater())) {
            log.info("vipRedActivityTaskAsync--发送红包失败,会员:" + account.getLoginName() + "==未满足赠送金额与流水倍数效验，跳过发放");
            return;
        }
        oprMemDayActivityService.accountMemDayGift(ruleScopeDto,
                actActivity, account, null, OrderConstants.ACTIVITY_MYHB,
                Constants.EVNumber.one, Constants.SYSTEM_USER);
    }

    public List<OprActActivity> getOprActActivitys(Integer isSelfHelp) {
        OprActActivity activity = new OprActActivity();
        activity.setUseStart(getPastDate(Constants.EVNumber.one, FORMAT_10_DATE));
        activity.setTmplCode(vipRedenvelopeCode);
        activity.setIsSelfHelp(isSelfHelp);
        activity.setIsSelfHelpShow(isSelfHelp);
        return activityMapper.findWaterActivity(activity);
    }
}
