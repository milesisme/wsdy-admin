package com.wsdy.saasops.modules.activity.service;


import com.wsdy.saasops.api.modules.user.service.SdyActivityService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ActivityConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.activity.dto.FirstChargeActivityRuleDto;
import com.wsdy.saasops.modules.activity.dto.FirstChargeDto;
import com.wsdy.saasops.modules.activity.dto.FirstChargeRuleScopeDto;
import com.wsdy.saasops.modules.activity.entity.MbrRebateFirstChargeReward;
import com.wsdy.saasops.modules.activity.mapper.MbrRebateFirstChargeRewardMapper;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAuditAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.firstChargeCode;
import static java.util.Objects.isNull;

@Service
@Slf4j
public class FirstChargeOprService {
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
    private MbrRebateFirstChargeRewardMapper mbrRebateFirstChargeRewardMapper;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private SdyActivityService sdyActivityService;
    /**
     * 首存送 返上级
     * @param accountId
     */
    public void applyFirstCharge( int accountId, String ip, String siteCode) {

        OprActActivity actActivity =  sdyActivityService.getOprActActivity(firstChargeCode);
        if(actActivity== null){
            log.info("充值返上级活动不存在【firstCharge_" + siteCode + "】");
            return;
        }
        MbrAccount account = mbrMapper.findMbrAccount(accountId, null, null);
        MbrAccount parentMbrAccount = mbrMapper.findParentMbrAccount(accountId);
        if(parentMbrAccount== null){
            return;
        }
        FirstChargeDto dto = jsonUtil.fromJson(actActivity.getRule(), FirstChargeDto.class);

        // 获取存款奖励
        FirstChargeRuleScopeDto ruleScopeDto = getRuleScopeDto(parentMbrAccount, dto);
        if (isNull(ruleScopeDto)) {
            log.info("奖励规则不存在【firstCharge_" + siteCode + "】");
           return;
        }

        // 校验姓名等信息
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(parentMbrAccount, dto.getScope(), ruleScopeDto.getIsName(),
                ruleScopeDto.getIsBank(), ruleScopeDto.getIsMobile(), ruleScopeDto.getIsMail(), ruleScopeDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            log.info("校验规则不符合【firstCharge_" + siteCode + "】 " + isAccountMsg);
            return;
        }

        // 该笔bonus属于会员哪个层级领取
        Integer actLevelId = null;
        if (dto.getScope() == Constants.EVNumber.one) {     // 层级会员
            actLevelId = parentMbrAccount.getActLevelId();
        }

        Boolean isIpOk =  checkoutIpFirstDepositSent(ip);
        if (Boolean.FALSE.equals(isIpOk)) {
            log.info("该IP已触发过发奖，无法触发第二次!【firstCharge_" + siteCode + "】 " + ip);
            return;
        }

        // 校验统计周期内该活动该会员该等级 是否已领取达到限制
        Boolean isDepositSent = checkoutFirstDepositSent(ruleScopeDto, actActivity.getId(), parentMbrAccount.getId(), account.getId(), actLevelId, account.getRegisterTime());
        if (Boolean.FALSE.equals(isDepositSent)) {
            log.info("该玩家已发放过【firstCharge_" + siteCode + "】 " + parentMbrAccount.getId());
            return;
        }

        // 查询首存条件
        FundDeposit deposit = getFirstDepositSentDeposits(ruleScopeDto.getDepositType(), account, account.getRegisterTime(), ruleScopeDto.getDay() );

        if(deposit.getIsActivityPass() == Constants.EVNumber.zero){
            log.info("首存条件不满足【firstCharge_" + siteCode + "】 " + account.getId());
            return;
        }

        FirstChargeActivityRuleDto ruleDto = getActivityRuleDto(ruleScopeDto.getActivityRuleDtos(), deposit.getDepositAmount());
        if (Objects.isNull(ruleDto)) {
            log.info("没有找到符合条件的奖励【firstCharge_" + siteCode + "】 " +  deposit.getDepositAmount());
            return;
        }
        // 奖励发放
        addFirstDepositReward(deposit, ruleDto, actActivity, parentMbrAccount, account, ip);
    }

    private FirstChargeRuleScopeDto getRuleScopeDto(MbrAccount account, FirstChargeDto dto) {

        if (Collections3.isEmpty(dto.getRuleScopeDtos())) {
            return null;
        }
        if (dto.getScope() == Constants.EVNumber.zero) {
            return dto.getRuleScopeDtos().get(0);
        }
        return dto.getRuleScopeDtos().stream()
                .filter(rs -> rs.getActLevelId() == account.getActLevelId())
                .findFirst().orElse(null);
    }


    private FundDeposit getFirstDepositSentDeposits(int depositType, MbrAccount account, String registerTime, Integer days) {
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setAccountId(account.getId());
        fundDeposit.setIsSign(Constants.EVNumber.one);

        if (Constants.EVNumber.one == depositType) {
            fundDeposit.setAuditTime(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.two == depositType) {
            fundDeposit.setAuditTimeFrom(getMonday(FORMAT_10_DATE));
            fundDeposit.setAuditTimeTo(getWeek(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.three == depositType) {
            Date date = parse(registerTime);
            Calendar calendar = Calendar.getInstance();  //得到日历
            calendar.setTime(date);//把当前时间赋给日历
            calendar.add(Calendar.DATE, days);
            Date endDate = calendar.getTime();
            fundDeposit.setAuditTimeFrom(format(date, FORMAT_10_DATE));
            fundDeposit.setAuditTimeTo(format(endDate, FORMAT_10_DATE));
        }

        List<FundDeposit> deposits = fundMapper.findDepositActivity(fundDeposit);
        if (Collections3.isEmpty(deposits)) {
            fundDeposit.setIsActivityPass(Constants.EVNumber.zero);
            return fundDeposit;
        }
        FundDeposit deposit = deposits.get(0);
        deposit.setIsActivityPass(Constants.EVNumber.one);
        return deposit;
    }

    private void addFirstDepositReward(FundDeposit deposit, FirstChargeActivityRuleDto ruleDto,
                                       OprActActivity actActivity, MbrAccount account,MbrAccount subAccount,  String ip) {

        MbrRebateFirstChargeReward mbrRebateFirstChargeReward = new MbrRebateFirstChargeReward();
        mbrRebateFirstChargeReward.setAccountId(account.getId());      // 会员id
        mbrRebateFirstChargeReward.setLoginName(account.getLoginName());      // 会员名
        mbrRebateFirstChargeReward.setSubAccountId(subAccount.getId());         //下级会员id
        mbrRebateFirstChargeReward.setActivityId(actActivity.getId());    // 活动id
        mbrRebateFirstChargeReward.setRuleId(actActivity.getRuleId());            // 规则id
        mbrRebateFirstChargeReward.setDepositId(deposit.getId());              // 存款对应的id ,可为null
        mbrRebateFirstChargeReward.setDepositedAmount(deposit.getDepositAmount());    // 存款金额, 可为null
        mbrRebateFirstChargeReward.setFinancialCode(OrderConstants.ACTIVITY_SCUP);  // 财务code  默认 其他RS
        mbrRebateFirstChargeReward.setApplicationTime(getCurrentDate(FORMAT_18_DATE_TIME));  // 申请时间
        mbrRebateFirstChargeReward.setBonusAmount(ruleDto.getDonateAmount());
        mbrRebateFirstChargeReward.setIp(ip);
        mbrRebateFirstChargeReward.setDevSource(account.getLoginSource());
        BigDecimal multipleWater = new BigDecimal(ruleDto.getMultipleWater() != null ? ruleDto.getMultipleWater().intValue() : 0);


        mbrRebateFirstChargeReward.setDiscountAudit(multipleWater); // 流水
        mbrRebateFirstChargeRewardMapper.insert(mbrRebateFirstChargeReward);

        BigDecimal amount =  ruleDto.getDonateAmount();
        // 发放奖励
        MbrBillDetail mbrBillDetail =  walletService.castWalletAndBillDetail(account.getLoginName(), account.getId(), OrderConstants.ACTIVITY_SCUP, amount, String.valueOf(new SnowFlake().nextId()), Boolean.TRUE,null,null);
        MbrAuditAccount mbrAuditAccount = null;
        if (multipleWater.intValue() > Constants.EVNumber.zero ) {
            mbrAuditAccount =  auditAccountService.insertAccountAudit(account.getId(), amount, null, multipleWater, null, null, null, Constants.EVNumber.one);
        }
        MbrRebateFirstChargeReward mbrRebateFirstChargeRewardBill = new MbrRebateFirstChargeReward();
        mbrRebateFirstChargeRewardBill.setId(mbrRebateFirstChargeReward.getId());
        mbrRebateFirstChargeRewardBill.setBillDetailId(mbrBillDetail.getId());
        if(mbrAuditAccount != null){
            mbrRebateFirstChargeRewardBill.setAuditId(mbrAuditAccount.getId());
        }
        mbrRebateFirstChargeRewardMapper.updateMbrRebateFirstChargeBilldIdAndAuditIdById(mbrRebateFirstChargeRewardBill);
    }

    private FirstChargeActivityRuleDto getActivityRuleDto(List<FirstChargeActivityRuleDto> ruleDtos, BigDecimal amount) {
        if (amount != null) {
            ruleDtos.sort((r1, r2) -> r2.getAmountMax().compareTo(r1.getAmountMax()));
            for (FirstChargeActivityRuleDto rs : ruleDtos) {
                Boolean isActivity = compareAmount(amount, rs.getAmountMin(), rs.getAmountMax());
                if (Boolean.TRUE.equals(isActivity)) {
                    return rs;
                }
            }
        }
        return null;
    }


    private Boolean compareAmount(BigDecimal amount, BigDecimal amountMin, BigDecimal amountMax) {
        if (amount.compareTo(amountMax) <= 0 && amount.compareTo(amountMin) >= 0 ) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    private Boolean checkoutIpFirstDepositSent(String  ip) {
        MbrRebateFirstChargeReward mbrRebateFirstChargeReward = new MbrRebateFirstChargeReward();
        mbrRebateFirstChargeReward.setIp(ip);
        int count = mbrRebateFirstChargeRewardMapper.findMbrRebateFirstChargeRewardCount(mbrRebateFirstChargeReward);
        if (count >= Constants.EVNumber.one) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     *  首存送/vip晋级优惠 校验统计周期内该活动该会员该等级 是否已领取达到限制
     * @param dto           规则dto
     * @param activityId    活动id
     * @param accountId     会员id
     * @param actLevelId    等级id  null表示不限制等级
     * @return true 未达到  false 已达到
     */
    private Boolean checkoutFirstDepositSent(FirstChargeRuleScopeDto dto, Integer activityId, Integer accountId, Integer subAccountId,  Integer actLevelId, String registerTime) {
        MbrRebateFirstChargeReward mbrRebateFirstChargeReward = new MbrRebateFirstChargeReward();
        mbrRebateFirstChargeReward.setActivityId(activityId);
        mbrRebateFirstChargeReward.setAccountId(accountId);
        mbrRebateFirstChargeReward.setActLevelId(actLevelId);
        mbrRebateFirstChargeReward.setSubAccountId(subAccountId);
        // 根据时间周期，设置时间条件： 可领取类型 0常规  1每天  2每周 3现时
        if (Constants.EVNumber.one == dto.getDepositType()) {
            mbrRebateFirstChargeReward.setApplicationTime(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.two == dto.getDepositType()) {
            mbrRebateFirstChargeReward.setStartTime(getMonday(FORMAT_10_DATE));
            mbrRebateFirstChargeReward.setEndTime(getWeek(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.three == dto.getDepositType()) {
            Date date = parse(registerTime);
            Calendar calendar = Calendar.getInstance();  //得到日历
            calendar.setTime(date);//把当前时间赋给日历
            calendar.add(Calendar.DATE, dto.getDay());
            Date endDate = calendar.getTime();
            mbrRebateFirstChargeReward.setStartTime(format(date, FORMAT_10_DATE));
            mbrRebateFirstChargeReward.setEndTime(format(endDate, FORMAT_10_DATE));
        }
        // 获取统计周期内的，该活动该会员该等级的已领取次数
        int count = mbrRebateFirstChargeRewardMapper.findMbrRebateFirstChargeRewardCount(mbrRebateFirstChargeReward);
        if (count >= Constants.EVNumber.one) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}
