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

        //?????????????????????????????????????????????
        setRescueAmountMax(actActivity,ruleDtos);
        return Constants.EVNumber.one;
    }

    public void setRescueAmountMax(OprActActivity actActivity,List<ActRescueRuleDto> rescueRuleDtos){
        if (Collections3.isNotEmpty(rescueRuleDtos)) {
            Collections.sort(rescueRuleDtos,Comparator.comparing(ActRescueRuleDto::getPayoutMin).reversed());
            //??????????????????
            ActRescueRuleDto ruleDtoMax = rescueRuleDtos.get(0);
            actActivity.setDonateType(Constants.EVNumber.zero);
            actActivity.setAmountMax(ruleDtoMax.getDonateAmountMax());
            actActivity.setDonateAmount(ruleDtoMax.getDonateAmount());
            actActivity.setMultipleWater(ruleDtoMax.getMultipleWater());
            //????????????????????????
            ActRescueRuleDto ruleDtoMin = rescueRuleDtos.get(rescueRuleDtos.size()-1);
            actActivity.setAmountMin(ruleDtoMin.getDepositMin());
            actActivity.setValidBet(ruleDtoMin.getPayoutMin());
        }
    }

    /**
     * ?????????
     * @param actActivity
     * @param accountId
     * @param ip
     */
    public void applyRescue(OprActActivity actActivity, int accountId, String ip){
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        // ????????????
        Type jsonType = new TypeToken<ActRuleBaseDto<ActRescueRuleDto>>() {}.getType();
        ActRuleBaseDto<ActRescueRuleDto> baseDto = jsonUtil.fromJson(actActivity.getRule(), jsonType);
        if(isNull(baseDto) || Collections3.isEmpty(baseDto.getRuleDtos())){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // ?????????????????????
        String isAccountMsg = oprActActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, baseDto.getIsName(),
                baseDto.getIsBank(), baseDto.getIsMobile(), false, baseDto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) { // ????????????????????? ????????? 4
            throw new R200Exception(isAccountMsg);
        }

        // ????????????????????????????????????????????????????????????
        Map<String,String> curTimeMap = getStartTimeAndEndTime(Constants.EVNumber.zero,Constants.EVNumber.zero);
        OprActBonus bonus = getLastBonus(actActivity.getId(),mbrAccount.getId(),curTimeMap);
        if(nonNull(bonus)){
            throw new R200Exception(ActivityConstants.CLAIMED);
        }

        // ?????????????????????????????????????????????
        Map<String,String> timeMap = getStartTimeAndEndTime(Constants.EVNumber.zero,Constants.EVNumber.one);

        // ???????????????????????????
        ActRescueRuleDto rescueRuleDto = checkoutRescuePayout(baseDto,mbrAccount,timeMap,false);
        if(Objects.isNull(rescueRuleDto)){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // ????????????bonus
        accountRescue(rescueRuleDto,actActivity,mbrAccount,ip, OrderConstants.ACTIVITY_RESCUE,null);
    }

    /**
     *  ?????????-?????????????????????????????????????????????
     * @param dto               ?????????dto
     * @param mbrAccount        ??????
     * @param timeMap           ????????????map
     * @param isMixActivity     ?????????????????????-???????????????????????? true ???????????? false ???????????????
     * @return
     */
    public ActRescueRuleDto checkoutRescuePayout(ActRuleBaseDto<ActRescueRuleDto> dto,MbrAccount mbrAccount, Map<String,String> timeMap, boolean isMixActivity){
        // ?????????????????????????????????
        List<AuditCat> auditCats = dto.getAuditCats();
        if(Collections3.isEmpty(auditCats)){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // ??????catId???catCode???map,??????catId ???gameCategory
        Map<Integer, String> catMap = tGmGameService.getCatCodeMap();

        // ?????????
        BigDecimal payout = BigDecimal.ZERO;

        // ??????????????????????????????
        for(AuditCat auditCat:auditCats) {
            if(Collections3.isNotEmpty(auditCat.getDepots())){
                // ????????????id list
                List<Integer> ids = auditCat.getDepots().stream().map(AuditDepot::getDepotId).collect(Collectors.toList());
                if(Collections3.isNotEmpty(ids)) {
                    // ????????????????????????????????????
                    String gameCategory = catMap.get(auditCat.getCatId());
                    if(isMixActivity){
                        // ??????ids???????????????codes
                        List<String> depostCodes = gameMapper.getDepotCodesByIds(ids);
                        // ????????????
                        BigDecimal catPayout = oprMixActivityService.getPayoutTotal(timeMap,mbrAccount,gameCategory.toLowerCase(),depostCodes);
                        payout = payout.add(catPayout);
                    }else{  // ????????? ???rpt
                        BigDecimal catPayout = mbrMapper.findPayoutTotal(mbrAccount.getId(), gameCategory, ids,timeMap.get("startTime"),timeMap.get("endTime"));
                        payout = payout.add(catPayout);
                    }

                }
            }
        }

        // ??????????????????????????????
        BigDecimal pickedBonusAmount = operateActivityMapper.getMbrAllBonusAmount(mbrAccount.getId(),timeMap.get("startTime"),timeMap.get("endTime"));
        // ???????????? = - ??????-????????????
        BigDecimal winLoseAmount = BigDecimal.ZERO.subtract(payout).subtract(pickedBonusAmount);
        // ????????????????????????
        if(BigDecimal.ZERO.compareTo(winLoseAmount) >= 0){
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // ????????????????????????????????????????????????
        BigDecimal depositAmount = fundMapper.sumFundDepositByAccountId(mbrAccount.getId(),timeMap.get("startTime"),timeMap.get("endTime"));

        // ???????????????????????????????????????
        ActRescueRuleDto rescueRuleDto = getRescueRule(dto.getRuleDtos(),winLoseAmount,depositAmount,Boolean.FALSE);
        if(Objects.isNull(rescueRuleDto)){
            return null;
        }
        rescueRuleDto.setPayout(winLoseAmount);
        rescueRuleDto.setDepositAmount(depositAmount);
        return rescueRuleDto;
    }

    // ???????????????????????????????????????
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
     * ??????????????????????????????
     * @param ruleDto               ???????????????dto
     * @param actActivity           ??????
     * @param account               ??????
     * @param ip                    ????????????ip
     * @param financialCode         ????????????code
     * @param subRuleTmplCode       ????????????-??????????????????code
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
        bonus.setSubRuleTmplCode(subRuleTmplCode);      // ?????????????????????code ?????????AQ0000003 ?????????AQ0000012 ?????????AQ0000004 ??????AQ0000015
        // ??????bonus
        actBonusMapper.insert(bonus);

        // ??????????????????????????????????????????????????????
        if (actActivity.getIsAudit() == Constants.EVNumber.zero) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
            bonus.setDepositedAmount(null);
            oprActActivityCastService.auditOprActBonus(bonus, financialCode, actActivity.getActivityName(), Boolean.TRUE);
        }

    }

    /**
     *
     * @param rule ??????
     * @param past 1????????????/0?????????/-1????????????
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
            startTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME,past,0);//???????????????
            endTime = DateUtil.getMonday(DateUtil.FORMAT_18_DATE_TIME,past-1,0);//???????????????
        }else if(Constants.EVNumber.two == rule){
            startTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME,past,0);//???????????????
            endTime = DateUtil.getFirstOfMonth(DateUtil.FORMAT_18_DATE_TIME,past-1,0);//???????????????
        }
        timeMap.put("startTime",startTime);
        timeMap.put("endTime",endTime);
        return timeMap;
    }
}
