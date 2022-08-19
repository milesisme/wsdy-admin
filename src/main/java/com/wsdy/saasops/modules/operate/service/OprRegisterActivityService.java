package com.wsdy.saasops.modules.operate.service;

import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ActivityConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dto.RegisterGiftDto;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class OprRegisterActivityService {
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private OprActActivityService oprActActivityService;
    //注册送
    public Integer checkoutRegisterGiftStatus(OprActActivity actActivity, int accountId) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
//        if(oprActActivityService.isBlackList(accountId, TOpActtmpl.registerGiftCode)||oprActActivityService.isBlackList(accountId, TOpActtmpl.allActivityCode)){
//            log.info(mbrAccount.getLoginName()+"是注册送活动黑名单会员");
//            return Constants.EVNumber.four;
//        }
//        //判断是否存在活动代理黑名单
//        if (oprActActivityService.valAgentBackList(mbrAccount,TOpActtmpl.registerGiftCode)){
//            log.info(mbrAccount.getLoginName()+"上级代理存在注册送活动黑名单");
//            return Constants.EVNumber.four;
//        }
//        //判断是否存在所有活动黑名单
//        if (oprActActivityService.valAgentBackList(mbrAccount,TOpActtmpl.allActivityCode)){
//            log.info(mbrAccount.getLoginName()+"上级代理存在所有活动黑名单");
//            return Constants.EVNumber.four;
//        }
        RegisterGiftDto giftDto = jsonUtil.fromJson(actActivity.getRule(), RegisterGiftDto.class);
        if (isNull(giftDto)) {
            return Constants.EVNumber.four;
        }
        // 是否领取校验
        Boolean isBonus = checkoutRegisterGiftBonus(actActivity, mbrAccount);
        if (!isBonus) {
            return Constants.EVNumber.four;
        }

        // 申请条件校验
        String isAccountMsg = oprActActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, giftDto.getIsName(),
                giftDto.getIsBank(), giftDto.getIsMobile(), false, false);
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            return Constants.EVNumber.four;
        }
        // 注册来源校验
        Boolean sourceCheck = checkoutRegisterSouce(mbrAccount, giftDto);
        if (!sourceCheck) {
            return Constants.EVNumber.four;
        }
        // 领取期限校验
        Boolean drawTimeCheckout = checkoutDrawTime(mbrAccount, giftDto, actActivity);
        if (!drawTimeCheckout) {
            return Constants.EVNumber.four;
        }

        // 设置活动最大赠送金额和流水倍数(前端展示数据)
        setRegisterGiftAmountMax(actActivity, giftDto);
        return Constants.EVNumber.one;
    }

    private Boolean checkoutRegisterGiftBonus(OprActActivity actActivity, MbrAccount mbrAccount) {
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(actActivity.getId());
        bonus.setAccountId(mbrAccount.getId());
        int count = operateActivityMapper.findBounsCountEx(bonus);   // 拒绝过的不能领取
        if (count > 0) { //已经申请活动
            return false;
        }
        return true;
    }

    public void setRegisterGiftAmountMax(OprActActivity actActivity, RegisterGiftDto giftDto) {
        actActivity.setDonateType(Constants.EVNumber.one);          // 固定金额类型
        actActivity.setMultipleWater(giftDto.getMultipleWater());   // 固定流水
        actActivity.setAmountMax(giftDto.getDonateAmount());        // 固定金额
        actActivity.setAmountMin(BigDecimal.ZERO);                  // 存款金额固定为0
        actActivity.setValidBet(BigDecimal.ZERO);                   // 投注金额固定为0
        // 活动条件
        actActivity.setDrawType(giftDto.getDrawType()); // 注册天数
    }

    public void applyRegisterGift(OprActActivity actActivity, int accountId, String ip) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        RegisterGiftDto giftDto = jsonUtil.fromJson(actActivity.getRule(), RegisterGiftDto.class);
        if (isNull(giftDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // 是否领取校验
        Boolean isBonus = checkoutRegisterGiftBonus(actActivity, mbrAccount);
        if (!isBonus) {
            throw new R200Exception(ActivityConstants.CLAIMED);
        }
        // 申请条件校验
        String isAccountMsg = oprActActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, giftDto.getIsName(),
                giftDto.getIsBank(), giftDto.getIsMobile(), false, false);
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }

        // 注册来源校验
        Boolean sourceCheck = checkoutRegisterSouce(mbrAccount, giftDto);
        if (!sourceCheck) {
            throw new R200Exception(ActivityConstants.REGISTER_SOURCE_NOT_FIT);
        }

        // 领取期限校验
        Boolean drawTimeCheckout = checkoutDrawTime(mbrAccount, giftDto, actActivity);
        if (!drawTimeCheckout) {
            throw new R200Exception(ActivityConstants.REGISTER_TIME_NOT_FIT);
        }

        // 生成红利数据
        accountRegisterGift(giftDto, actActivity, mbrAccount, ip);
    }

    // 注册来源校验
    public Boolean checkoutRegisterSouce(MbrAccount mbrAccount, RegisterGiftDto giftDto) {
        Boolean sourceCheck = false;
        // 注册来源( 0 PC 1管理后端(v2) 3 wap(h5) 4 APP   5 代理后台  6 帮好友注册)
        Byte registerSource = mbrAccount.getRegisterSource();
        if (isNull(registerSource)) {
            return sourceCheck;
        }
        if (Boolean.TRUE.equals(giftDto.getPcClient()) && Integer.valueOf(Constants.EVNumber.zero).equals(Integer.valueOf(registerSource))) {
            sourceCheck = true;
        }
        if (Boolean.TRUE.equals(giftDto.getAdminManage()) && Integer.valueOf(Constants.EVNumber.one).equals(Integer.valueOf(registerSource))) {
            sourceCheck = true;
        }
        if (Boolean.TRUE.equals(giftDto.getH5Client()) && Integer.valueOf(Constants.EVNumber.three).equals(Integer.valueOf(registerSource))) {
            sourceCheck = true;
        }
        if (Boolean.TRUE.equals(giftDto.getAppClient()) && Integer.valueOf(Constants.EVNumber.four).equals(Integer.valueOf(registerSource))) {
            sourceCheck = true;
        }
        if (Boolean.TRUE.equals(giftDto.getAgentManage()) && Integer.valueOf(Constants.EVNumber.five).equals(Integer.valueOf(registerSource))) {
            sourceCheck = true;
        }
        if (Boolean.TRUE.equals(giftDto.getFriendRegister()) && Integer.valueOf(Constants.EVNumber.six).equals(Integer.valueOf(registerSource))) {
            sourceCheck = true;
        }
        return sourceCheck;
    }

    // 领取期限校验
    public Boolean checkoutDrawTime(MbrAccount mbrAccount, RegisterGiftDto giftDto, OprActActivity actActivity) {
        Integer drawType = giftDto.getDrawType();
        if (drawType.compareTo(Constants.EVNumber.zero) < 0) {
            return false;
        }
        if (drawType.compareTo(Constants.EVNumber.zero) > 0) {
            String registerTime = mbrAccount.getRegisterTime();
            String startTime = actActivity.getUseStart();
            String reGisterEndTime = DateUtil.getPostDayTime(registerTime, drawType - 1, DateUtil.FORMAT_10_DATE) + " " + DateUtil.DATE_END;
            String currentTime = DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);

            // 注册时间在活动时间开始之后且领取时间在领取期限内
            if (startTime.compareTo(registerTime) > 0 || currentTime.compareTo(reGisterEndTime) > 0) {
                return false;
            }
        }
        return true;
    }

    // 注册送生成红利记录
    private void accountRegisterGift(RegisterGiftDto ruleDto, OprActActivity actActivity, MbrAccount account, String ip) {
        OprActBonus bonus = oprActActivityCastService.setOprActBonus(account.getId(), account.getLoginName(), actActivity.getId(), null, null, actActivity.getRuleId());
        bonus.setScope(null);
        bonus.setCreateUser(account.getLoginName());
        bonus.setIp(ip);
        bonus.setDevSource(account.getLoginSource());
        int multipleWater = nonNull(ruleDto.getMultipleWater()) ? ruleDto.getMultipleWater().intValue() : 0;
        bonus.setDiscountAudit(new BigDecimal(multipleWater));
        bonus.setBonusAmount(ruleDto.getDonateAmount());
        bonus.setAuditAmount(auditAccountService.getAddAuditAmount(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        bonus.setSource(Constants.EVNumber.zero);
        bonus.setCreateUser(account.getLoginName());
        actBonusMapper.insert(bonus);
        // 系统自动审核处理
        if (actActivity.getIsAudit() == Constants.EVNumber.zero) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            bonus.setDepositedAmount(null);
            // 审核红利
            oprActActivityCastService.auditOprActBonus(bonus, OrderConstants.ACTIVITY_REGISTER, actActivity.getActivityName(), Boolean.TRUE);
        }

    }
}
