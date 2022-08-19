package com.wsdy.saasops.modules.operate.service;

import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ActivityConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.member.dao.MbrWalletMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.mapper.AuditMapper;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dto.ActivityRuleDto;
import com.wsdy.saasops.modules.operate.dto.JDepositSentDto;
import com.wsdy.saasops.modules.operate.dto.RuleScopeDto;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class OprVipDepositSentService {

    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private MbrWalletMapper walletMapper;
    @Autowired
    private OprDepositSentService depositSentService;
    @Autowired
    private AuditMapper auditMapper;
    /**
     * 存就送 VIP申请页面
     *
     * @param actActivity
     * @param accountId
     */
    public void applyDepositSentBonus(OprActActivity actActivity, int accountId, String ip) {
        MbrAccount account = mbrMapper.findMbrAccount(accountId, null, null);
        JDepositSentDto dto = jsonUtil.fromJson(actActivity.getRule(), JDepositSentDto.class);
        // 获取会员所在等级的层级规则
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId(), dto.getScope());
        if (isNull(ruleScopeDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // 校验真实姓名银行卡等
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(account, dto.getScope(), ruleScopeDto.getIsName(),
                ruleScopeDto.getIsBank(), ruleScopeDto.getIsMobile(), ruleScopeDto.getIsMail(), ruleScopeDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }
        Integer actLevelId = null;  // 全部会员规则：为null
        if (dto.getScope() == Constants.EVNumber.one) {
            actLevelId = account.getActLevelId();    // 层级会员规则：为会员的层级
        }
        // 校验统计周期内是否已领取达到限制
        Boolean isDepositSent = depositSentService.checkoutDepositSent(ruleScopeDto, actActivity.getId(), account.getId(), actLevelId);
        // 已达到领取限制
        if (Boolean.FALSE.equals(isDepositSent)) {
            throw new R200Exception(ActivityConstants.CLAIMED);
        }

        // 校验当前是否存在优惠稽核(反水除外，含任务ruleId 888888结尾)
        Integer count = auditMapper.findAuditAccountWithoutWater(account.getId());  // 获取会员当前除反水外，未通过的优惠稽核个数
        if(count.intValue() > 0){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // 获取会员钱包余额
        MbrWallet wallet = new MbrWallet();
        wallet.setLoginName(account.getLoginName());
        MbrWallet mbrWallet = walletMapper.selectOne(wallet);

        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setDepositAmount(mbrWallet.getBalance());
        ruleScopeDto.setFormulaMode(Constants.EVNumber.zero);   // 0 规则共享，实际下面的修改后的是规则不共享的，只取最高,而实际只能设置一条，此处废弃无意义

        // 获取当前余额对应的最高的档次
        ActivityRuleDto ruleDto = getActivityRuleDto(ruleScopeDto.getActivityRuleDtos(), fundDeposit.getDepositAmount());
        if (Objects.isNull(ruleDto)) {  // 不存在，即不满足中心钱包最低余额
            ActivityRuleDto ruleMinDto = ruleScopeDto.getActivityRuleDtos().get(0);
         /*   String msg = String.format("存款还差%s元即可领取彩金%s元，是否前去充值？",
                    ruleMinDto.getAmountMin().subtract(fundDeposit.getDepositAmount()), ruleMinDto.getDonateAmount());
            throw new R200Exception(msg);*/
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // 保存bonus
        accountDepositSent(dto, fundDeposit, actActivity, account, ip, actLevelId);
    }

    /**
     * 检查是否可申请VIP存就送
     *
     * @param actActivity
     * @param accountId
     */
    public OprActActivity checkDepositSentBonus(OprActActivity actActivity, int accountId, String ip) {
        MbrAccount account = mbrMapper.findMbrAccount(accountId, null, null);
        JDepositSentDto dto = jsonUtil.fromJson(actActivity.getRule(), JDepositSentDto.class);
        // 获取会员所在等级的层级规则
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId(), dto.getScope());
        if (isNull(ruleScopeDto)) {
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        // 校验真实姓名银行卡等
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(account, dto.getScope(), ruleScopeDto.getIsName(),
                ruleScopeDto.getIsBank(), ruleScopeDto.getIsMobile(), ruleScopeDto.getIsMail(), ruleScopeDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        Integer actLevelId = null;  // 全部会员规则：为null
        if (dto.getScope() == Constants.EVNumber.one) {
            actLevelId = account.getActLevelId();    // 层级会员规则：为会员的层级
        }
        // 校验统计周期内是否已领取达到限制
        Boolean isDepositSent = depositSentService.checkoutDepositSent(ruleScopeDto, actActivity.getId(), account.getId(), actLevelId);
        // 已达到领取限制
        if (Boolean.FALSE.equals(isDepositSent)) {
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }

        // 校验当前是否存在优惠稽核(反水除外，含任务ruleId 888888结尾)
        Integer count = auditMapper.findAuditAccountWithoutWater(account.getId());  // 获取会员当前除反水外，未通过的优惠稽核个数
        if(count.intValue() > 0){
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }

        // 获取会员钱包余额
        MbrWallet wallet = new MbrWallet();
        wallet.setLoginName(account.getLoginName());
        MbrWallet mbrWallet = walletMapper.selectOne(wallet);

        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setDepositAmount(mbrWallet.getBalance());
        ruleScopeDto.setFormulaMode(Constants.EVNumber.zero);   // 0 规则共享，实际下面的修改后的是规则不共享的，只取最高,而实际只能设置一条，此处废弃无意义

        // 获取当前余额对应的最高的档次
        ActivityRuleDto ruleDto = getActivityRuleDto(ruleScopeDto.getActivityRuleDtos(), fundDeposit.getDepositAmount());
        if (Objects.isNull(ruleDto)) {  // 不存在，即不满足中心钱包最低余额
            ActivityRuleDto ruleMinDto = ruleScopeDto.getActivityRuleDtos().get(0);
         /*   String msg = String.format("存款还差%s元即可领取彩金%s元，是否前去充值？",
                    ruleMinDto.getAmountMin().subtract(fundDeposit.getDepositAmount()), ruleMinDto.getDonateAmount());
            throw new R200Exception(msg);*/
            actActivity.setCanApply(Constants.EVNumber.zero);
            return actActivity;
        }
        actActivity.setCanApply(Constants.EVNumber.one);
        actActivity.setCanApplyBonus(ruleDto.getDonateAmount());
        actActivity.setActivityAlready(fundDeposit.getDepositAmount());
        return actActivity;
    }

    private ActivityRuleDto getActivityRuleDto(List<ActivityRuleDto> ruleDtos, BigDecimal amount) {
        ruleDtos.sort((r1, r2) -> r2.getAmountMax().compareTo(r1.getAmountMax()));
        for (ActivityRuleDto rs : ruleDtos) {
            if (amount.compareTo(rs.getAmountMin()) != -1) {
                return rs;
            }
        }
        return null;
    }

    private void accountDepositSent(JDepositSentDto dto, FundDeposit deposit, OprActActivity actActivity,
                                    MbrAccount account, String ip,Integer actLevelId) {
        // 设置bonus对象
        OprActBonus bonus = actActivityCastService.setOprActBonus(account.getId(), account.getLoginName(), actActivity.getId(),
                deposit.getDepositAmount(), deposit.getId(), actActivity.getRuleId());
        bonus.setActLevelId(actLevelId);
        bonus.setScope(null);
        bonus.setIp(ip);
        bonus.setCatId(actActivity.getCatId());
        bonus.setDevSource(account.getLoginSource());
        // 保存bonus
        addDepositSent(dto, deposit, bonus, actActivity, account);
    }

    private void addDepositSent(JDepositSentDto dto, FundDeposit deposit, OprActBonus bonus, OprActActivity actActivity, MbrAccount account) {
        // 获取会员所在等级的层级规则
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), account.getActLevelId(), dto.getScope());
        // 计算bonus的稽核等
        // 所需流水金额计算为：（优惠金额+优惠金额对应的中心钱包余额）× 流水倍数
        OprActBonus oprActBonus = setBonusAmount(deposit.getDepositAmount(), ruleScopeDto.getActivityRuleDtos());
        bonus.setBonusAmount(oprActBonus.getBonusAmount());
        bonus.setDiscountAudit(oprActBonus.getDiscountAudit());
        bonus.setAuditAmount(oprActBonus.getAuditAmount());
        bonus.setSource(Constants.EVNumber.zero);
        bonus.setCreateUser(account.getLoginName());
        actBonusMapper.insert(bonus);
        if (actActivity.getIsAudit() == Constants.EVNumber.zero) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            actActivityCastService.auditDepositSentBonus(bonus, OrderConstants.ACTIVITY_DEPOSITSENT, actActivity.getActivityName(), Boolean.TRUE);
        }
    }

    public OprActBonus setBonusAmount(BigDecimal amount, List<ActivityRuleDto> ruleDtos) {
        // 实际只能设置一条，此处废弃无意义
        if (Collections3.isNotEmpty(ruleDtos)) {
            ruleDtos.sort((r1, r2) -> r2.getAmountMax().compareTo(r1.getAmountMax()));
        }
        // 所需流水金额为：（优惠金额+优惠金额对应的中心钱包余额）× 流水倍数
        // 优惠金额对应的中心钱包余额：固定赠送则为中心钱包最低余额，比例赠送则为优惠金额/比例
        for (int j = 0; j < ruleDtos.size(); j++) {
            ActivityRuleDto ruleDto = ruleDtos.get(j);
            OprActBonus oprActBonus = new OprActBonus();
            int multipleWater = nonNull(ruleDto.getMultipleWater()) ? ruleDto.getMultipleWater().intValue() : 0;
            if (amount.compareTo(ruleDto.getAmountMin()) != -1) {
                // 按比例
                if (ruleDto.getDonateType() == Constants.EVNumber.zero) {
                    BigDecimal donateAmount = ruleDto.getDonateAmount().divide(new BigDecimal(Constants.ONE_HUNDRED));
                    BigDecimal bigDecimal = CommonUtil.adjustScale(donateAmount.multiply(amount));
                    if (nonNull(ruleDto.getDonateAmountMax())
                            && ruleDto.getDonateAmountMax().compareTo(BigDecimal.ZERO) == 1
                            && bigDecimal.compareTo(ruleDto.getDonateAmountMax()) ==1) {
                        bigDecimal = ruleDto.getDonateAmountMax();
                    }
                    oprActBonus.setBonusAmount(bigDecimal);
                    BigDecimal ruleAmount = CommonUtil.adjustScale(bigDecimal.add(bigDecimal.divide(donateAmount,2,BigDecimal.ROUND_DOWN)));
                    oprActBonus.setAuditAmount(CommonUtil.adjustScale(ruleAmount.multiply(new BigDecimal(multipleWater))));
                } else {    // 固定
                    oprActBonus.setBonusAmount(ruleDto.getDonateAmount());
                    BigDecimal ruleAmount = CommonUtil.adjustScale(ruleDto.getDonateAmount().add(ruleDto.getAmountMin()));
                    oprActBonus.setAuditAmount(CommonUtil.adjustScale(ruleAmount.multiply(new BigDecimal(multipleWater))));
                }
                oprActBonus.setDiscountAudit(new BigDecimal(multipleWater));
                return oprActBonus;
            }
        }
        return null;
    }
}

