package com.wsdy.saasops.modules.operate.service;

import com.google.common.reflect.TypeToken;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.WarningConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dto.WarningLogDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dto.*;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class ActivityAddService {

    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private OprMemDayActivityService dayActivityService;
    @Autowired
    private OprRescueActivityService oprRescueActivityService;
    @Autowired
    private OprActBonusMapper oprActBonusMapper;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private OprBettingGiftService bettingGiftService;
    @Autowired
    private OprMixActivityService oprMixActivityService;
    @Autowired
    private FundMapper fundMapper;


    public void save(OprActBonus oprActBonus, String userName, String ip){
        // ????????????
        MbrAccount mbrAccount = mbrAccountService.findAccountByName(oprActBonus.getLoginName());
        if(isNull(mbrAccount)){
            throw new R200Exception("??????????????????,???????????????");
        }
        // ????????????
        OprActActivity actActivity = operateActivityMapper.findOprActActivity(oprActBonus.getActivityId());
        if (StringUtil.isEmpty(actActivity.getRuleId())){
            throw new R200Exception("????????????????????????");
        }
        if((TOpActtmpl.waterRebatesCode.equals(actActivity.getTmplCode())
                || TOpActtmpl.mbrRebateCode.equals(actActivity.getTmplCode()))){
            throw new R200Exception("???????????????????????????????????????????????????");
        }

        // ????????????????????????????????????????????????
        if(TOpActtmpl.mixActivityCode.equals(actActivity.getTmplCode())){
            Assert.isBlank(oprActBonus.getSubRuleTmplCode(), "?????????????????????code?????????null");
            Boolean isApply = oprMixActivityService.checkoutIsApplyBonus( actActivity.getId(), oprActBonus.getSubRuleTmplCode(), mbrAccount.getId());
            if (Boolean.FALSE.equals(isApply)) {
                throw new R200Exception("?????????????????????????????????????????????(??????????????????????????????)");
            }
        }

        // ??????bonus????????????
        OprActBonus bonus = actActivityCastService.setOprActBonus(mbrAccount.getId(), mbrAccount.getLoginName(),
                oprActBonus.getActivityId(), oprActBonus.getDepositedAmount(), null, actActivity.getRuleId());

        // ??????bonus????????? tip ????????????????????????financialCode??????RS??????
        bonus.setIp(ip);
        bonus.setCreateUser(userName);
        bonus.setCatId(oprActBonus.getCatId());                     // ??????id
        bonus.setSource(Constants.EVNumber.one);                    // 1????????????
        bonus.setBonusAmount(oprActBonus.getBonusAmount());         // ????????????
        bonus.setDiscountAudit(oprActBonus.getDiscountAudit());     // ????????????
        bonus.setAuditAmount(oprActBonus.getAuditAmount());         // ????????????
        bonus.setValidBet(oprActBonus.getValidBet());               // ????????????
        bonus.setApplicationMemo(oprActBonus.getMemo());            // ???????????????
        bonus.setDepositedAmount(oprActBonus.getDepositedAmount()); // ????????????
        bonus.setSubRuleTmplCode(oprActBonus.getSubRuleTmplCode()); // ????????????SubRuleTmplCode
        oprActBonusMapper.insert(bonus);

        mbrAccountLogService.saveBonus(bonus);

        String content = String.format( WarningConstants.BONUS_TMP, oprActBonus.getBonusAmount());
        // ??????
        if(oprActBonus.getBonusAmount().compareTo(WarningConstants.WARNING_BONUS) >= 0){
            mbrAccountLogService.addWarningLog(new WarningLogDto(oprActBonus.getLoginName(), userName, content, Constants.EVNumber.four) );
        }
    }


    public void getAuditMulti(OprActBonus bonus) {
        OprActActivity oprActActivity = operateActivityMapper.findOprActActivity(bonus.getActivityId());
        String ruleStr = oprActActivity.getRule();
        if (!isNull(oprActActivity.getRuleId()) && StringUtil.isNotEmpty(ruleStr)) {
            MbrAccount account = new MbrAccount();
            account.setLoginName(bonus.getLoginName());
            MbrAccount mbrAccount = accountMapper.selectOne(account);
            if (isNull(mbrAccount)) {
                throw new R200Exception("?????????????????????");
            }
            if (TOpActtmpl.preferentialCode.equals(oprActActivity.getTmplCode())) {
                JPreferentialDto dto = jsonUtil.fromJson(ruleStr, JPreferentialDto.class);
                RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), mbrAccount.getActLevelId(), dto.getScope());
                if (null == ruleScopeDto) {
                    return;
                }
                ActivityRuleDto ruleDto = actActivityCastService.getActivityRuleDto(ruleScopeDto.getActivityRuleDtos(), bonus.getDepositedAmount());
                if (null != ruleDto) {//?????????setPreferentialBonus
                    int multipleWater = nonNull(ruleDto.getMultipleWater()) ? ruleDto.getMultipleWater().intValue() : 0;
                    bonus.setDiscountAudit(new BigDecimal(multipleWater));
                    if (ruleDto.getDonateType() == Constants.EVNumber.one) {
                        bonus.setBonusAmount(ruleDto.getDonateAmount());
                    } else {
                        BigDecimal bigDecimal = CommonUtil.adjustScale(ruleDto.getDonateAmount().divide(
                                new BigDecimal(Constants.ONE_HUNDRED)).multiply(bonus.getDepositedAmount()));
                        bonus.setBonusAmount(bigDecimal.compareTo(ruleDto.getDonateAmountMax()) == 1
                                ? ruleDto.getDonateAmountMax() : bigDecimal);
                    }
                }
            }
            if (TOpActtmpl.depositSentCode.equals(oprActActivity.getTmplCode())) {
                JDepositSentDto jDepositSentDto = jsonUtil.fromJson(ruleStr, JDepositSentDto.class);
                RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(jDepositSentDto.getRuleScopeDtos(), mbrAccount.getActLevelId(), jDepositSentDto.getScope());
                if (null == ruleScopeDto) {
                    return;
                }
                actActivityCastService.setBonusAmount(bonus.getDepositedAmount(), bonus, ruleScopeDto.getActivityRuleDtos(), ruleScopeDto.getFormulaMode(), null);
            }
            if (TOpActtmpl.memDayGiftCode.equals(oprActActivity.getTmplCode())) {
                MemDayGiftDto giftDto = jsonUtil.fromJson(ruleStr, MemDayGiftDto.class);
                MemDayRuleScopeDto ruleScopeDto = dayActivityService.getRuleScopeDtos(giftDto.getRuleScopeDtos(), mbrAccount.getActLevelId(), giftDto.getScope());
                if (isNull(ruleScopeDto)) {
                    return;
                }
                bonus.setDiscountAudit(new BigDecimal(nonNull(ruleScopeDto.getMultipleWater()) ? ruleScopeDto.getMultipleWater().intValue() : 0));
                bonus.setBonusAmount(ruleScopeDto.getDonateAmount());
            }
            if (TOpActtmpl.registerGiftCode.equals(oprActActivity.getTmplCode())) {
                RegisterGiftDto giftDto = jsonUtil.fromJson(ruleStr, RegisterGiftDto.class);
                if (isNull(giftDto)) {
                    return;
                }
                int multipleWater = nonNull(giftDto.getMultipleWater()) ? giftDto.getMultipleWater().intValue() : 0;
                bonus.setDiscountAudit(new BigDecimal(multipleWater));
                bonus.setBonusAmount(giftDto.getDonateAmount());
            }
            if (TOpActtmpl.bettingGiftCode.equals(oprActActivity.getTmplCode())) {
                BettingGiftDto giftDto = jsonUtil.fromJson(ruleStr, BettingGiftDto.class);
                List<BettingGiftRuleDto> ruleDtos = giftDto.getBettingGiftRuleDtos();
                ruleDtos.sort((r1, r2) -> r2.getValidBetMin().compareTo(r1.getValidBetMin()));//?????????????????????
                //??????????????????????????????????????????
                List<BettingGiftRuleDto> validBetRuleDtos = ruleDtos.stream().filter(dto -> bonus.getValidBet().compareTo(dto.getValidBetMin()) >= 0)
                        .collect(Collectors.toList());
                BettingGiftRuleDto matchedRuleDto = null;
                for (BettingGiftRuleDto ruleDto : validBetRuleDtos) {
                    ruleDto.setDepositAmountType(0);
                    if (bettingGiftService.checkBettingGiftDeposit(ruleDto, BigDecimal.ZERO)) {
                        matchedRuleDto = ruleDto;
                        break;
                    }
                }
                if (nonNull(matchedRuleDto)) {
                    int multipleWater = nonNull(matchedRuleDto.getMultipleWater()) ? matchedRuleDto.getMultipleWater().intValue() : 0;
                    bonus.setDiscountAudit(new BigDecimal(multipleWater));
                    bonus.setBonusAmount(matchedRuleDto.getDonateAmount());
                }
            }
            if (TOpActtmpl.rescueCode.equals(oprActActivity.getTmplCode())) {
                Type jsonType = new TypeToken<ActRuleBaseDto<ActRescueRuleDto>>() {
                }.getType();
                ActRuleBaseDto<ActRescueRuleDto> baseDto = jsonUtil.fromJson(ruleStr, jsonType);
                ActRescueRuleDto ruleDto = oprRescueActivityService.getRescueRule(baseDto.getRuleDtos(), bonus.getValidBet(), BigDecimal.ZERO, Boolean.TRUE);
                if (isNull(ruleDto)) {
                    return;
                }
                int multipleWater = nonNull(ruleDto.getMultipleWater()) ? ruleDto.getMultipleWater().intValue() : 0;
                BigDecimal rateBonusAmount = bonus.getValidBet().multiply(ruleDto.getDonateAmount().divide(new BigDecimal(Constants.ONE_HUNDRED)));
                BigDecimal bonusAmount = rateBonusAmount.compareTo(ruleDto.getDonateAmountMax()) <= 0 ? rateBonusAmount : ruleDto.getDonateAmountMax();
                bonus.setDiscountAudit(new BigDecimal(multipleWater));
                bonus.setBonusAmount(bonusAmount);
            }
            if (TOpActtmpl.otherCode.equals(oprActActivity.getTmplCode())) {
                JOtherDto otherDto = jsonUtil.fromJson(ruleStr, JOtherDto.class);
                if (isNull(otherDto)) {
                    return;
                }
                int multipleWater = nonNull(otherDto.getMultipleWater()) ? otherDto.getMultipleWater().intValue() : 0;
                bonus.setDiscountAudit(new BigDecimal(multipleWater));
                bonus.setBonusAmount(otherDto.getDonateAmount());
            }
        }
    }
}

