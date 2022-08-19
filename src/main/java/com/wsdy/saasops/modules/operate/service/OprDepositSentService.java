package com.wsdy.saasops.modules.operate.service;

import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ActivityConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
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
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class OprDepositSentService {

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
    private MbrAccountMapper accountMapper;
    @Autowired
    private OprActActivityService oprActActivityService;

    /**
     * 存就送 申请页面
     *
     * @param actActivity
     * @param accountId
     */
    public void applyDepositSentBonus(OprActActivity actActivity, int accountId, String ip) {
        MbrAccount account = mbrMapper.findMbrAccount(accountId, null, null);

        // 获得符合会员层级的规则
        JDepositSentDto dto = jsonUtil.fromJson(actActivity.getRule(), JDepositSentDto.class);
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId(), dto.getScope());
        if (isNull(ruleScopeDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // 校验姓名等信息
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(account, dto.getScope(), ruleScopeDto.getIsName(),
                ruleScopeDto.getIsBank(), ruleScopeDto.getIsMobile(), ruleScopeDto.getIsMail(), ruleScopeDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }

        // 该笔bonus属于会员哪个层级领取 null表示规则为全部时领取
        Integer actLevelId = null;
        if (dto.getScope() == Constants.EVNumber.one) {     // 层级会员
            actLevelId = account.getActLevelId();
        }

        // 校验统计周期内该活动该会员该等级 是否已领取达到限制
        Boolean isDepositSent = checkoutDepositSent(ruleScopeDto, actActivity.getId(), account.getId(), actLevelId);
        if (Boolean.FALSE.equals(isDepositSent)) {
            throw new R200Exception(ActivityConstants.CLAIMED);
        }

        // 校验统计周期内，最近一笔存款是否是可用的存款
        FundDeposit fundDeposit = getDepositSentDeposits(ruleScopeDto.getDrawType(), account, actActivity);
        if (nonNull(fundDeposit.getIsActivityPass())) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // 获取存款符合的规则
        // 新规则，目前存就送已区分不同存款方式
        ActivityRuleDto ruleDto = actActivityCastService.getJDepositActivityRuleDto(ruleScopeDto.getActivityRuleDtos(), fundDeposit);
        if (Objects.isNull(ruleDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // 处理优惠
        accountDepositSent(actActivity, ruleScopeDto, OrderConstants.ACTIVITY_DEPOSITSENT,null,
                fundDeposit.getId(),fundDeposit.getDepositAmount(), account, ip, actLevelId, fundDeposit);
    }

    /**
     * 检查是否可领取存就送
     *
     * @param actActivity
     * @param accountId
     */
    public OprActActivity checkDepositSentBonus(OprActActivity actActivity, int accountId, String ip) {
        MbrAccount account = mbrMapper.findMbrAccount(accountId, null, null);

        // 获得符合会员层级的规则
        JDepositSentDto dto = jsonUtil.fromJson(actActivity.getRule(), JDepositSentDto.class);
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId(), dto.getScope());
        if (isNull(ruleScopeDto)) {
            log.info("检查会员{}是否可领活动，活动ID{}，活动规则未配置", account.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        // 校验姓名等信息
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(account, dto.getScope(), ruleScopeDto.getIsName(),
                ruleScopeDto.getIsBank(), ruleScopeDto.getIsMobile(), ruleScopeDto.getIsMail(), ruleScopeDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            log.info("检查会员{}是否可领活动，活动ID{}，会员未完善信息", account.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }

        // 该笔bonus属于会员哪个层级领取 null表示规则为全部时领取
        Integer actLevelId = null;
        if (dto.getScope() == Constants.EVNumber.one) {     // 层级会员
            actLevelId = account.getActLevelId();
        }

        // 校验统计周期内该活动该会员该等级 是否已领取达到限制
        Boolean isDepositSent = checkoutDepositSent(ruleScopeDto, actActivity.getId(), account.getId(), actLevelId);
        if (Boolean.FALSE.equals(isDepositSent)) {
            log.info("检查会员{}是否可领活动，活动ID{}，会员周期内已达到限制", account.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }

        // 校验统计周期内，最近一笔存款是否是可用的存款
        FundDeposit fundDeposit = getDepositSentDeposits(ruleScopeDto.getDrawType(), account, actActivity);
        if (nonNull(fundDeposit.getIsActivityPass())) {
            log.info("检查会员{}是否可领活动，活动ID{}，会员最近一笔存款不可用", account.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        // 获取存款符合的规则
        ActivityRuleDto ruleDto = actActivityCastService.getJDepositActivityRuleDto(ruleScopeDto.getActivityRuleDtos(), fundDeposit);
        if (Objects.isNull(ruleDto)) {
            log.info("检查会员{}是否可领活动，活动ID{}，会员不符合活动规则", account.getLoginName(), actActivity.getId());
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }

        actActivity.setCanApply(Constants.EVNumber.one);
        actActivity.setCanApplyBonus(ruleDto.getDonateAmount());
        actActivity.setActivityAlready(fundDeposit.getDepositAmount());
        return actActivity;
    }

    private FundDeposit getDepositSentDeposits(int drawType, MbrAccount account, OprActActivity actActivity) {
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setAccountId(account.getId());
        fundDeposit.setIsSign(Constants.EVNumber.four);
        if (Constants.EVNumber.zero == drawType) {
            fundDeposit.setAuditTime(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.one == drawType) {
            fundDeposit.setAuditTimeFrom(getMonday(FORMAT_10_DATE));
            fundDeposit.setAuditTimeTo(getWeek(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.two == drawType) {
            fundDeposit.setAuditTimeFrom(getPastDate(Constants.EVNumber.six, FORMAT_10_DATE));
            fundDeposit.setAuditTimeTo(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.three == drawType) {
            fundDeposit.setAuditTimeFrom(actActivity.getUseStart());
            fundDeposit.setAuditTimeTo(actActivity.getUseEnd());
        }
        if (Constants.EVNumber.four == drawType) {
            fundDeposit.setAuditTimeFrom(format(getmindate(), FORMAT_10_DATE));
            fundDeposit.setAuditTimeTo(format(getmaxdate(), FORMAT_10_DATE));
        }
        List<FundDeposit> deposits = fundMapper.findDepositActivity(fundDeposit);
        if (Collections3.isEmpty(deposits)) {
            fundDeposit.setIsActivityPass(Constants.EVNumber.two);
            return fundDeposit;
        }
        FundDeposit deposit = deposits.get(0);
        Boolean isLatestWeek = isLatestWeek(parse(deposit.getAuditTime()), new Date(), Constants.EVNumber.seven);
        if (Boolean.FALSE.equals(isLatestWeek)) {
            deposit.setIsActivityPass(Constants.EVNumber.one);
            return deposit;
        }
        OprActBonus bonus = new OprActBonus();
        bonus.setAccountId(account.getId());
        bonus.setDepositId(deposit.getId());
        int count = operateActivityMapper.findOprActBouns(bonus);
        if (count > 0) {
            deposit.setIsActivityPass(Constants.EVNumber.two);
        }
        return deposit;
    }

    /**
     * 存就送处理并生成红利
     * @param actActivity       活动对象
     * @param ruleScopeDto      会员符合的规则dto
     * @param financialCode     活动financialCode
     * @param subRuleTmplCode   混合活动子规则code 存就送AQ0000003 投就送AQ0000012 救援金AQ0000004 其他AQ0000015
     *                          单独存就送活动为null
     * @param depositId         存款表id   存就送活动有值，混合活动为null
     * @param depositAmount     存款金额
     * @param account           会员
     * @param ip                会员领取的ip
     * @param actLevelId        该笔bonus属于会员哪个层级领取 null表示规则为全部时领取
     */
    public void accountDepositSent(OprActActivity actActivity, RuleScopeDto ruleScopeDto, String financialCode, String subRuleTmplCode,
                                   Integer depositId, BigDecimal depositAmount,MbrAccount account, String ip, Integer actLevelId,
                                   FundDeposit deposit) {
        // bonus公共该数据处理
        OprActBonus bonus = actActivityCastService.setOprActBonus(account.getId(), account.getLoginName(),
                actActivity.getId(),depositAmount, depositId, actActivity.getRuleId());

        bonus.setScope(null);
        bonus.setIp(ip);
        bonus.setCatId(actActivity.getCatId());         // VIP特权存就送catId
        bonus.setDevSource(account.getLoginSource());
        bonus.setCreateUser(account.getLoginName());
        bonus.setActLevelId(actLevelId);
        bonus.setSource(Constants.EVNumber.zero);
        bonus.setSubRuleTmplCode(subRuleTmplCode);      // 混合活动子规则code 存就送AQ0000003 投就送AQ0000012 救援金AQ0000004 其他AQ0000015

        // 匹配规则处理红利和稽核数据
        actActivityCastService.setBonusAmount(depositAmount, bonus, ruleScopeDto.getActivityRuleDtos(), ruleScopeDto.getFormulaMode(), deposit);
        // 插入bonus
        actBonusMapper.insert(bonus);

        // 判断是否需要审核，不审核直接发放红利
        if (actActivity.getIsAudit() == Constants.EVNumber.zero) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            actActivityCastService.auditDepositSentBonus(bonus, financialCode, actActivity.getActivityName(), Boolean.TRUE);
        }
    }

    /**
     *  存就送/vip晋级优惠 校验统计周期内该活动该会员该等级 是否已领取达到限制
     * @param dto           规则dto
     * @param activityId    活动id
     * @param accountId     会员id
     * @param actLevelId    等级id  null表示不限制等级
     * @return true 未达到  false 已达到
     */
    public Boolean checkoutDepositSent(RuleScopeDto dto, Integer activityId, Integer accountId, Integer actLevelId) {
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(activityId);
        bonus.setAccountId(accountId);
        bonus.setActLevelId(actLevelId);
        // 根据时间周期，设置时间条件： 可领取类型 0每日 1每周 2近7日 3自定义  4每月
        if (Constants.EVNumber.zero == dto.getDrawType()) {
            bonus.setApplicationTime(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.one == dto.getDrawType()) {
            bonus.setStartTime(getMonday(FORMAT_10_DATE));
            bonus.setEndTime(getWeek(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.two == dto.getDrawType()) {
            bonus.setStartTime(getPastDate(Constants.EVNumber.six, FORMAT_10_DATE));
            bonus.setEndTime(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.four == dto.getDrawType()) {
            bonus.setStartTime(format(getmindate(), FORMAT_10_DATE));
            bonus.setEndTime(format(getmaxdate(), FORMAT_10_DATE));
        }
        // 获取统计周期内的，该活动该会员该等级的已领取次数
        int count = operateActivityMapper.findBounsCount(bonus);
        if (count >= dto.getDrawNumber()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    
    
    /**
     * 	非混合活动
     * 
     * @param actActivity
     * @param accountId
     * @return
     */
    public Integer checkoutApplyDepositSentForNotMix(OprActActivity actActivity, int accountId) {
    	MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
    	JDepositSentDto dto = jsonUtil.fromJson(actActivity.getRule(), JDepositSentDto.class);
    	RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId(), dto.getScope());
    	if (isNull(ruleScopeDto) && !TOpActtmpl.vipPrivilegesCode.equals(actActivity.getTmplCode())) {
    		return Constants.EVNumber.four;
    	}
    	actActivityCastService.setDonateAmountMax(actActivity, account.getActLevelId());
    	return Constants.EVNumber.one;
    }


    /**
     * 	混合活动
     * 
     * @param actActivity
     * @param accountId
     * @return
     */
    public Integer checkoutApplyDepositSent(OprActActivity actActivity, int accountId) {
        JDepositSentDto dto = jsonUtil.fromJson(actActivity.getRule(), JDepositSentDto.class);
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), null, dto.getScope());
        if (isNull(ruleScopeDto) && !TOpActtmpl.vipPrivilegesCode.equals(actActivity.getTmplCode())) {
            return Constants.EVNumber.four;
        }
        actActivityCastService.setDonateAmountMax(actActivity, null);
        return Constants.EVNumber.one;
    }
}

