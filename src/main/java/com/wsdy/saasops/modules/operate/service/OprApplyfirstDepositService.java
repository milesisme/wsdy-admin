package com.wsdy.saasops.modules.operate.service;

import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ActivityConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dto.*;
import com.wsdy.saasops.modules.operate.entity.*;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class OprApplyfirstDepositService {

    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private OprActActivityService oprActActivityService;
    /**
     * 首存送
     *
     * @param actActivity
     * @param accountId
     */
    public BigDecimal applyfirstDeposit(OprActActivity actActivity, int accountId, String ip) {

        MbrAccount account = mbrMapper.findMbrAccount(accountId, null, null);
        JPreferentialDto dto = jsonUtil.fromJson(actActivity.getRule(), JPreferentialDto.class);
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId(), dto.getScope());
        if (isNull(ruleScopeDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        if (ruleScopeDto.getDepositType() == Constants.EVNumber.three) {
            Boolean isSign = isLatestWeek(parse(account.getRegisterTime()), new Date(), ruleScopeDto.getDay());
            if (Boolean.FALSE.equals(isSign)){
                throw new R200Exception(ActivityConstants.INCOMPATIBLE);
            }
        }
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(account, dto.getScope(), ruleScopeDto.getIsName(),
                ruleScopeDto.getIsBank(), ruleScopeDto.getIsMobile(), ruleScopeDto.getIsMail(), ruleScopeDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }
        Integer isBonus = checkoutFirstDepositBonus(ruleScopeDto, actActivity.getId(), account.getId());
        if (Constants.EVNumber.three == isBonus) {
            throw new R200Exception(ActivityConstants.CLAIMED);
        }
        if (Constants.EVNumber.zero == isBonus) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        FundDeposit deposit = getFundDeposit(account, ruleScopeDto, actActivity, Boolean.FALSE);
        if (nonNull(deposit.getIsActivityPass())) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        return accountDepositBonus(ruleScopeDto, deposit, actActivity, account, ip);
    }

    /**
     * 检查是否可领取首存送
     *
     * @param actActivity
     * @param accountId
     */
    public OprActActivity checkfirstDeposit(OprActActivity actActivity, int accountId, String ip) {

        MbrAccount account = mbrMapper.findMbrAccount(accountId, null, null);
        JPreferentialDto dto = jsonUtil.fromJson(actActivity.getRule(), JPreferentialDto.class);
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId(), dto.getScope());
        if (isNull(ruleScopeDto)) {
            log.info("检查会员{}是否可领活动，活动ID{}，活动条件配置为空", account.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        if (ruleScopeDto.getDepositType() == Constants.EVNumber.three) {
            Boolean isSign = isLatestWeek(parse(account.getRegisterTime()), new Date(), ruleScopeDto.getDay());
            if (Boolean.FALSE.equals(isSign)){
                log.info("检查会员{}是否可领活动，活动ID{}，活动已领取", account.getLoginName(), actActivity.getId());
                actActivity.setCanApply(Constants.EVNumber.zero);
                return actActivity;
            }
        }
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(account, dto.getScope(), ruleScopeDto.getIsName(),
                ruleScopeDto.getIsBank(), ruleScopeDto.getIsMobile(), ruleScopeDto.getIsMail(), ruleScopeDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            log.info("检查会员{}是否可领活动，活动ID{}，会员未完善资料", account.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        Integer isBonus = checkoutFirstDepositBonus(ruleScopeDto, actActivity.getId(), account.getId());
        if (Constants.EVNumber.three == isBonus) {
            log.info("检查会员{}是否可领活动，活动ID{}，已申请过该活动", account.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        if (Constants.EVNumber.zero == isBonus) {
            log.info("检查会员{}是否可领活动，活动ID{}，已申请其他活动", account.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        FundDeposit deposit = getFundDeposit(account, ruleScopeDto, actActivity, Boolean.FALSE);
        if (nonNull(deposit.getIsActivityPass())) {
            log.info("检查会员{}是否可领活动，活动ID{}，活动存款未通过", account.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }

        return checkAccountDepositBonus(ruleScopeDto, deposit, actActivity, account, ip);
    }
    private OprActActivity checkAccountDepositBonus(RuleScopeDto ruleScopeDto, FundDeposit deposit,
                                                    OprActActivity actActivity, MbrAccount account, String ip) {
        ActivityRuleDto ruleDto = actActivityCastService.getActivityRuleDto(ruleScopeDto.getActivityRuleDtos(), deposit.getDepositAmount());
        if (isNull(ruleDto)) {
            log.info("检查会员{}是否可领活动，活动ID{}，活动规则为空", account.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        int count = operateActivityMapper.findBonusWaterCount(account.getId(), waterRebatesCode, getCurrentDate(FORMAT_10_DATE));
        if (count > 0) {
            log.info("检查会员{}是否可领活动，活动ID{}，有流水未完成", account.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        BigDecimal bonus = getFirstDepositBonus(deposit, ruleDto, actActivity, account, ip);
        actActivity.setCanApply(Constants.EVNumber.one);
        actActivity.setCanApplyBonus(bonus);
        actActivity.setActivityAlready(deposit.getDepositAmount());
        return actActivity;
    }
    private BigDecimal getFirstDepositBonus(FundDeposit deposit, ActivityRuleDto ruleDto,
                                            OprActActivity actActivity, MbrAccount account, String ip) {
        OprActBonus bonus = actActivityCastService.setOprActBonus(account.getId(), account.getLoginName(),
                actActivity.getId(), deposit.getDepositAmount(), deposit.getId(), actActivity.getRuleId());
        if (ruleDto.getDonateType() == Constants.EVNumber.one) {
            bonus.setBonusAmount(ruleDto.getDonateAmount());
        } else {
            BigDecimal bigDecimal = CommonUtil.adjustScale(ruleDto.getDonateAmount().divide(
                    new BigDecimal(Constants.ONE_HUNDRED)).multiply(deposit.getDepositAmount()));
            bonus.setBonusAmount(bigDecimal.compareTo(ruleDto.getDonateAmountMax()) == 1
                    ? ruleDto.getDonateAmountMax() : bigDecimal);
        }
        return bonus.getBonusAmount();
    }

    private FundDeposit getFundDeposit(MbrAccount account, RuleScopeDto ruleScopeDto, OprActActivity actActivity, Boolean isStatus) {
        int depositType = ruleScopeDto.getDepositType();
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setAccountId(account.getId());
        fundDeposit.setAuditTimeFrom(actActivity.getUseStart());
        fundDeposit.setAuditTimeTo(actActivity.getUseEnd());
        if (Constants.EVNumber.zero == depositType || Constants.EVNumber.four == depositType) {
            FundDeposit fundDeposit1 = new FundDeposit();
            fundDeposit1.setAccountId(account.getId());

            fundDeposit1.setAuditTimeFrom(actActivity.getUseStart());
            List<FundDeposit> depositList = fundMapper.findDepositActivity(fundDeposit1);
            if (depositList.size() == 0) {
                fundDeposit.setIsActivityPass(Constants.EVNumber.two);
                return fundDeposit;
            }
            fundDeposit.setIsSign(Constants.EVNumber.one);
        }
        if (Constants.EVNumber.one == depositType) {
            fundDeposit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            fundDeposit.setIsSign(Constants.EVNumber.one);
        }
        if (Constants.EVNumber.two == depositType) {
            fundDeposit.setIsSign(Constants.EVNumber.three);
        }
        List<FundDeposit> deposits = fundMapper.findDepositActivity(fundDeposit);
        if (deposits.size() == 0) {
            fundDeposit.setIsActivityPass(Constants.EVNumber.two);
            return fundDeposit;
        }
        FundDeposit deposit = deposits.get(0);
        if (Constants.EVNumber.zero == depositType && nonNull(ruleScopeDto.getValidDate()) && ruleScopeDto.getValidDate() > 0) {//首存有效期校验
            Date validDate = DateUtil.parse(DateUtil.getPastDate(ruleScopeDto.getValidDate(), DateUtil.FORMAT_18_DATE_TIME));
            if (validDate.compareTo(DateUtil.parse(deposit.getAuditTime(), DateUtil.FORMAT_18_DATE_TIME)) == 1) {
                fundDeposit.setIsActivityPass(Constants.EVNumber.two);
                return fundDeposit;
            }
        }
        OprActBonus bonus = new OprActBonus();
        bonus.setAccountId(account.getId());
        bonus.setDepositId(deposit.getId());
        if (Boolean.TRUE.equals(isStatus)) {
            bonus.setIsStatus(Constants.EVNumber.one);
        }
        int count = operateActivityMapper.findOprActBouns(bonus);
        if (count > 0) {
            deposit.setIsActivityPass(Constants.EVNumber.two);
        }
        return deposit;
    }

    private BigDecimal accountDepositBonus(RuleScopeDto ruleScopeDto, FundDeposit deposit,
                                     OprActActivity actActivity, MbrAccount account, String ip) {
        ActivityRuleDto ruleDto = actActivityCastService.getActivityRuleDto(ruleScopeDto.getActivityRuleDtos(), deposit.getDepositAmount());
        if (isNull(ruleDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        int count = operateActivityMapper.findBonusWaterCount(account.getId(), waterRebatesCode, getCurrentDate(FORMAT_10_DATE));
        if (count > 0) {
            throw new R200Exception("今天已经发放返水，不可领取");
        }
        return addFirstDepositBonus(deposit, ruleDto, actActivity, account, ip);
    }

    private BigDecimal addFirstDepositBonus(FundDeposit deposit, ActivityRuleDto ruleDto,
                                      OprActActivity actActivity, MbrAccount account, String ip) {
        OprActBonus bonus = actActivityCastService.setOprActBonus(account.getId(), account.getLoginName(),
                actActivity.getId(), deposit.getDepositAmount(), deposit.getId(), actActivity.getRuleId());
        if (ruleDto.getDonateType() == Constants.EVNumber.one) {
            bonus.setBonusAmount(ruleDto.getDonateAmount());
        } else {
            BigDecimal bigDecimal = CommonUtil.adjustScale(ruleDto.getDonateAmount().divide(
                    new BigDecimal(Constants.ONE_HUNDRED)).multiply(deposit.getDepositAmount()));
            bonus.setBonusAmount(bigDecimal.compareTo(ruleDto.getDonateAmountMax()) == 1
                    ? ruleDto.getDonateAmountMax() : bigDecimal);
        }
        int multipleWater = nonNull(ruleDto.getMultipleWater()) ? ruleDto.getMultipleWater().intValue() : 0;
        bonus.setDiscountAudit(new BigDecimal(multipleWater));
        bonus.setIp(ip);
        bonus.setDevSource(account.getLoginSource());
        bonus.setAuditAmount(auditAccountService.getAddAuditAmountForDeposit(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        bonus.setSource(Constants.EVNumber.zero);
        bonus.setCreateUser(account.getLoginName());
        actBonusMapper.insert(bonus);
        if (actActivity.getIsAudit() == Constants.EVNumber.zero) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            actActivityCastService.auditOprActBonus(bonus, OrderConstants.ACTIVITY_PREFERENTIAL, actActivity.getActivityName(), Boolean.TRUE);
        }
        return bonus.getBonusAmount();
    }

    public Integer checkoutFirstDepositBonus(RuleScopeDto ruleScopeDto, Integer activityId, Integer accountId) {
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(activityId);
        bonus.setAccountId(accountId);
        if (Constants.EVNumber.one == ruleScopeDto.getDepositType()) {
            bonus.setApplicationTime(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.two == ruleScopeDto.getDepositType()) {
            bonus.setStartTime(getMonday(FORMAT_10_DATE));
            bonus.setEndTime(getWeek(FORMAT_10_DATE));
        }
        int applyCount = operateActivityMapper.findDepositApplyForActivity(bonus);
        if (applyCount > 0) {
            return Constants.EVNumber.zero; //存款申请其他活动
        }
        int count = operateActivityMapper.findBounsCount(bonus);
        if (count > 0) {
            return Constants.EVNumber.three; //已经申请活动
        }
        return Constants.EVNumber.one;
    }

    public Integer checkoutApplyFirstDeposit(OprActActivity actActivity, int accountId) {
        MbrAccount account = mbrMapper.findMbrAccount(accountId, null, null);
//        if(oprActActivityService.isBlackList(accountId, TOpActtmpl.preferentialCode)||oprActActivityService.isBlackList(accountId, TOpActtmpl.allActivityCode)){
//            log.info(account.getLoginName()+"是首存送活动黑名单会员");
//            return Constants.EVNumber.four;
//        }
//        //判断是否存在活动代理黑名单
//        if (oprActActivityService.valAgentBackList(account,TOpActtmpl.preferentialCode)){
//            log.info(account.getLoginName()+"上级代理存在首存送活动黑名单");
//            return Constants.EVNumber.four;
//        }
//        //判断是否存在所有活动黑名单
//        if (oprActActivityService.valAgentBackList(account,TOpActtmpl.allActivityCode)){
//            log.info(account.getLoginName()+"上级代理存在所有活动黑名单");
//            return Constants.EVNumber.four;
//        }
        JPreferentialDto dto = jsonUtil.fromJson(actActivity.getRule(), JPreferentialDto.class);
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId(), dto.getScope());
        if (isNull(ruleScopeDto)) {
            return Constants.EVNumber.four;
        }
        actActivityCastService.setDonateAmountMax(actActivity, account.getActLevelId());
        return Constants.EVNumber.one;
    }
}

