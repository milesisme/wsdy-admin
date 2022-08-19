package com.wsdy.saasops.modules.operate.service;

import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ActivityConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dto.*;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.TGmCat;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.GameMapper;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class OprRescueActivityService {

    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private OprMixActivityService oprMixActivityService;
    @Autowired
    private TGmGameService tGmGameService;
    @Autowired
    private OprActActivityService oprActActivityService;
    @Autowired
    private MbrAccountMapper accountMapper;

    public Integer checkoutRescueStatus(OprActActivity actActivity, int accountId){
        //MbrAccount account = accountMapper.selectByPrimaryKey(accountId);

        Type jsonType = new TypeToken<ActRuleBaseDto<ActRescueRuleDto>>() {}.getType();
        ActRuleBaseDto<ActRescueRuleDto> baseDto = jsonUtil.fromJson(actActivity.getRule(), jsonType);
        if(isNull(baseDto) || Collections3.isEmpty(baseDto.getRuleDtos())){
            return Constants.EVNumber.four;
        }
        List<ActRescueRuleDto> ruleDtos = baseDto.getRuleDtos();

        //设置活动最大赠送金额和流水倍数
        setRescueAmountMax(actActivity,ruleDtos);
        return Constants.EVNumber.one;
    }

    public void setRescueAmountMax(OprActActivity actActivity,List<ActRescueRuleDto> rescueRuleDtos){
        if (Collections3.isNotEmpty(rescueRuleDtos)) {
            Collections.sort(rescueRuleDtos,Comparator.comparing(ActRescueRuleDto::getPayoutMin).reversed());
            //领取最高金额
            ActRescueRuleDto ruleDtoMax = rescueRuleDtos.get(0);
            actActivity.setDonateType(Constants.EVNumber.zero);
            actActivity.setAmountMax(ruleDtoMax.getDonateAmountMax());
            actActivity.setDonateAmount(ruleDtoMax.getDonateAmount());
            actActivity.setMultipleWater(ruleDtoMax.getMultipleWater());
            //满足活动最低条件
            ActRescueRuleDto ruleDtoMin = rescueRuleDtos.get(rescueRuleDtos.size()-1);
            actActivity.setAmountMin(ruleDtoMin.getDepositMin());
            actActivity.setValidBet(ruleDtoMin.getPayoutMin());
        }
    }

    /**
     * 救援金
     * @param actActivity
     * @param accountId
     * @param ip
     */
    public void applyRescue(OprActActivity actActivity, int accountId, String ip){
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        // 获取规则
        Type jsonType = new TypeToken<ActRuleBaseDto<ActRescueRuleDto>>() {}.getType();
        ActRuleBaseDto<ActRescueRuleDto> baseDto = jsonUtil.fromJson(actActivity.getRule(), jsonType);
        if(isNull(baseDto) || Collections3.isEmpty(baseDto.getRuleDtos())){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // 校验姓名等信息
        String isAccountMsg = oprActActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, baseDto.getIsName(),
                baseDto.getIsBank(), baseDto.getIsMobile(), false, baseDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) { // 不符合基本条件 不显示 4
            throw new R200Exception(isAccountMsg);
        }

        // 获取今日的时间，判断今日是否有领取该优惠
        Map<String,String> curTimeMap = getStartTimeAndEndTime(Constants.EVNumber.zero,Constants.EVNumber.zero);
        OprActBonus bonus = getLastBonus(actActivity.getId(),mbrAccount.getId(),curTimeMap);
        if(nonNull(bonus)){
            throw new R200Exception(ActivityConstants.CLAIMED);
        }

        // 获取昨日的时间，统计昨日负派彩
        Map<String,String> timeMap = getStartTimeAndEndTime(Constants.EVNumber.zero,Constants.EVNumber.one);

        // 获取满足条件的规则
        ActRescueRuleDto rescueRuleDto = checkoutRescuePayout(baseDto,mbrAccount,timeMap,false);
        if(Objects.isNull(rescueRuleDto)){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // 处理优惠bonus
        accountRescue(rescueRuleDto,actActivity,mbrAccount,ip, OrderConstants.ACTIVITY_RESCUE,null);
    }

    /**
     *  救援金-获取负盈利和存款满足条件的规则
     * @param dto               救援金dto
     * @param mbrAccount        会员
     * @param timeMap           计算时间map
     * @param isMixActivity     是否是混合活动-救援金子规则计算 true 混合规则 false 单一救援金
     * @return
     */
    public ActRescueRuleDto checkoutRescuePayout(ActRuleBaseDto<ActRescueRuleDto> dto,MbrAccount mbrAccount, Map<String,String> timeMap, boolean isMixActivity){
        // 校验规则：校验派彩范围
        List<AuditCat> auditCats = dto.getAuditCats();
        if(Collections3.isEmpty(auditCats)){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // 获取catId和catCode的map,转化catId 到gameCategory
        Map<Integer, String> catMap = tGmGameService.getCatCodeMap();

        // 总派彩
        BigDecimal payout = BigDecimal.ZERO;

        // 获取统计范围内的派彩
        for(AuditCat auditCat:auditCats) {
            if(Collections3.isNotEmpty(auditCat.getDepots())){
                // 获得平台id list
                List<Integer> ids = auditCat.getDepots().stream().map(AuditDepot::getDepotId).collect(Collectors.toList());
                if(Collections3.isNotEmpty(ids)) {
                    // 按类别计算勾选平台的派彩
                    String gameCategory = catMap.get(auditCat.getCatId());
                    if(isMixActivity){
                        // 平台ids转化为平台codes
                        List<String> depostCodes = gameMapper.getDepotCodesByIds(ids);
                        // 获取派彩
                        BigDecimal catPayout = oprMixActivityService.getPayoutTotal(timeMap,mbrAccount,gameCategory.toLowerCase(),depostCodes);
                        payout = payout.add(catPayout);
                    }else{  // 救援金 查rpt
                        BigDecimal catPayout = mbrMapper.findPayoutTotal(mbrAccount.getId(), gameCategory, ids,timeMap.get("startTime"),timeMap.get("endTime"));
                        payout = payout.add(catPayout);
                    }

                }
            }
        }

        // 统计时间内的优惠红利
        BigDecimal pickedBonusAmount = operateActivityMapper.getMbrAllBonusAmount(mbrAccount.getId(),timeMap.get("startTime"),timeMap.get("endTime"));
        // 负派彩值 = - 派彩-优惠红利
        BigDecimal winLoseAmount = BigDecimal.ZERO.subtract(payout).subtract(pickedBonusAmount);
        // 判断是否为负派彩
        if(BigDecimal.ZERO.compareTo(winLoseAmount) >= 0){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // 查出统计时间内的审核通过的总存款
        BigDecimal depositAmount = fundMapper.sumFundDepositByAccountId(mbrAccount.getId(),timeMap.get("startTime"),timeMap.get("endTime"));

        // 获取符合负派彩和存款的规则
        ActRescueRuleDto rescueRuleDto = getRescueRule(dto.getRuleDtos(),winLoseAmount,depositAmount,Boolean.FALSE);
        if(Objects.isNull(rescueRuleDto)){
            return null;
        }
        rescueRuleDto.setPayout(winLoseAmount);
        rescueRuleDto.setDepositAmount(depositAmount);
        return rescueRuleDto;
    }

    // 获取符合负派彩和存款的规则
    public ActRescueRuleDto getRescueRule(List<ActRescueRuleDto> rescueRuleDtos,BigDecimal winLoseAmount,BigDecimal depositAmount,Boolean isSign){

        Collections.sort(rescueRuleDtos,Comparator.comparing(ActRescueRuleDto::getPayoutMin).reversed());
        for(ActRescueRuleDto rescueRuleDto : rescueRuleDtos){
            if(rescueRuleDto.getPayoutMin().compareTo(winLoseAmount) <= 0){
                if(Boolean.TRUE.equals(isSign) || Constants.EVNumber.zero == rescueRuleDto.getDepositAmountType()){
                    return rescueRuleDto;
                }else{
                    if(rescueRuleDto.getDepositMin().compareTo(depositAmount) <= 0){
                        return rescueRuleDto;
                    }
                }
            }
        }
        return null;
    }

    public OprActBonus getLastBonus(Integer actId,Integer accountId,Map<String,String> timeMap){
        String startTime = timeMap.get("startTime");
        String endTime = timeMap.get("endTime");
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(actId);
        bonus.setAccountId(accountId);
        bonus.setStartTime(startTime);
        bonus.setEndTime(endTime);
        bonus = operateActivityMapper.findLastBonus(bonus);
        return bonus;
    }

    /**
     * 投就送处理并生成红利
     * @param ruleDto               投就送规则dto
     * @param actActivity           活动
     * @param account               会员
     * @param ip                    会员申请ip
     * @param financialCode         活动财务code
     * @param subRuleTmplCode       混合活动-投就送子规则code
     */
    public void accountRescue(ActRescueRuleDto ruleDto,OprActActivity actActivity, MbrAccount account, String ip,String financialCode, String subRuleTmplCode){
        OprActBonus bonus = oprActActivityCastService.setOprActBonus(account.getId(), account.getLoginName(), actActivity.getId(),
                null, null, actActivity.getRuleId());
        bonus.setScope(null);
        bonus.setIp(ip);
        bonus.setDevSource(account.getLoginSource());
        int multipleWater = nonNull(ruleDto.getMultipleWater()) ? ruleDto.getMultipleWater().intValue() : 0;
        BigDecimal rateBonusAmount = ruleDto.getPayout().multiply(ruleDto.getDonateAmount().divide(new BigDecimal(Constants.ONE_HUNDRED)));
        BigDecimal bonusAmount = rateBonusAmount.compareTo(ruleDto.getDonateAmountMax()) <= 0 ? rateBonusAmount : ruleDto.getDonateAmountMax();
        bonus.setDiscountAudit(new BigDecimal(multipleWater));
        bonus.setBonusAmount(bonusAmount);
        bonus.setDepositedAmount(ruleDto.getDepositAmount());
        bonus.setPayout(ruleDto.getPayout());
        bonus.setAuditAmount(auditAccountService.getAddAuditAmount(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        bonus.setSource(Constants.EVNumber.zero);
        bonus.setCreateUser(account.getLoginName());
        bonus.setSubRuleTmplCode(subRuleTmplCode);      // 混合活动子规则code 存就送AQ0000003 投就送AQ0000012 救援金AQ0000004 其他AQ0000015
        // 插入bonus
        actBonusMapper.insert(bonus);

        // 判断是否需要审核，不审核直接发放红利
        if (actActivity.getIsAudit() == Constants.EVNumber.zero) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
            bonus.setDepositedAmount(null);
            oprActActivityCastService.auditOprActBonus(bonus, financialCode, actActivity.getActivityName(), Boolean.TRUE);
        }

    }

    /**
     *
     * @param rule 周期
     * @param past 1上一周期/0本周期/-1下一周期
     * @return
     */
    public Map<String,String> getStartTimeAndEndTime(Integer rule,Integer past){
        Map<String,String> timeMap = new HashMap<>(2);
        String startTime = null;
        String endTime = null;
        if(isNull(rule) || Constants.EVNumber.zero == rule){
            startTime = DateUtil.getPastDate(past,DateUtil.FORMAT_10_DATE)+ " " +DateUtil.DATE_STATE;
            endTime = DateUtil.getPastDate(past-1,DateUtil.FORMAT_10_DATE)+ " " +DateUtil.DATE_STATE;
        }else if(Constants.EVNumber.one == rule){
            startTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME,past,0);//上周第一天
            endTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME,past-1,0);//本周第一天
        }else if(Constants.EVNumber.two == rule){
            startTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME,past,0);//上月第一天
            endTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME,past-1,0);//本月第一天
        }
        timeMap.put("startTime",startTime);
        timeMap.put("endTime",endTime);
        return timeMap;
    }
}
