package com.wsdy.saasops.modules.operate.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.formatEsDate;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.ElasticSearchConnection_Read;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.ActivityConstants;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.wsdy.saasops.modules.analysis.mapper.AnalysisMapper;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.dto.ActRescueRuleDto;
import com.wsdy.saasops.modules.operate.dto.ActRuleBaseDto;
import com.wsdy.saasops.modules.operate.dto.ActivityRuleDto;
import com.wsdy.saasops.modules.operate.dto.BettingGiftDto;
import com.wsdy.saasops.modules.operate.dto.BettingGiftRuleDto;
import com.wsdy.saasops.modules.operate.dto.JDepositSentDto;
import com.wsdy.saasops.modules.operate.dto.JOtherDto;
import com.wsdy.saasops.modules.operate.dto.RuleScopeDto;
import com.wsdy.saasops.modules.operate.dto.mixActivity.MixActivityDto;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class OprMixActivityService {
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private OperateActivityMapper operateMapper;
    @Autowired
    private OprActActivityService oprActActivityService;
    @Autowired
    private OprDepositSentService depositSentService;
    @Autowired
    private OprBettingGiftService bettingGiftService;
    @Autowired
    private OprRescueActivityService oprRescueActivityService;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private OprDepositSentService oprDepositSentService;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private AnalysisMapper analysisMapper;
    @Autowired
    private ElasticSearchConnection_Read connection;

    /**
     * 判断是否领取完所有子规则
     *
     * @param actActivity
     * @return true 子规则都已领取  false 子规则存在未领取的
     */
    public boolean checkClaimeAll(OprActActivity actActivity) {
        // 获取规则
        MixActivityDto dto = jsonUtil.fromJson(actActivity.getRule(), MixActivityDto.class);
        if (isNull(dto)) {
            return false;
        }

        // 规则校验
        Integer applyType = dto.getApplyType();                                     // 获取活动顺序  0 按顺序 1条件满足领取
        String startTime = dto.getStartTime();                                      // 统计开始时间
        HashMap<String, Integer> activityLinkedList = dto.getActivityLinkedList();   // 活动顺序map key: 活动code 存就送AQ0000003 投就送AQ0000012 救援金AQ0000004 其他AQ0000015  value:排序，从1开始从小到大
        if (Objects.isNull(applyType) || StringUtils.isEmpty(startTime) || Collections3.isEmpty(activityLinkedList)) {
            return false;
        }

        int size = activityLinkedList.size();       // 几个活动

        // 判断是否都已领取
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(actActivity.getId());
        bonus.setAccountId(actActivity.getAccountId());
        int count = operateActivityMapper.findBounsCount(bonus);
        if (size <= count) {      // 都已领取，显示
            return true;
        }

        return false;
    }

    /**
     * 我的优惠-全部-获取混合活动的子规则数据
     *
     * @param activityId 混合活动id
     * @param accountId  会员id
     * @return 混合活动子规则数据
     */
    public List<OprActActivity> getMixActivity(Integer activityId, Integer accountId) {
        // 获取活动
        OprActActivity actActivity = operateMapper.findOprActActivity(activityId);
        // 校验活动
        String isActivity = oprActActivityService.checkoutActivity(actActivity, Boolean.FALSE);
        if (nonNull(isActivity)) {
            throw new R200Exception(isActivity);
        }
        // 获取规则
        MixActivityDto dto = jsonUtil.fromJson(actActivity.getRule(), MixActivityDto.class);
        if (isNull(dto)) {
            throw new R200Exception("规则为空！");
        }

        // 规则校验
        Integer applyType = dto.getApplyType();                                     // 获取活动顺序  0 按顺序 1条件满足领取
        String startTime = dto.getStartTime();                                      // 统计开始时间
        HashMap<String, Integer> activityLinkedList = dto.getActivityLinkedList();   // 活动顺序map key: 活动code 存就送AQ0000003 投就送AQ0000012 救援金AQ0000004 其他AQ0000015  value:排序，从1开始从小到大
        if (Objects.isNull(applyType) || StringUtils.isEmpty(startTime) || Collections3.isEmpty(activityLinkedList)) {
            throw new R200Exception("规则错误！");
        }

        // 按顺序排序Map
        Map<String, Integer> sortMap = CommonUtil.mapSortByValue(activityLinkedList, 1);

        // 处理返回数据
        List<OprActActivity> activityList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortMap.entrySet()) {
            // 存就送
            if (TOpActtmpl.depositSentCode.equals(entry.getKey())) {
                JDepositSentDto jDepositSentDto = dto.getJDepositSentDto();
                if (Objects.isNull(jDepositSentDto)) {
                    throw new R200Exception("存就送子规则错误！");
                }
                OprActActivity opr = new OprActActivity();
                opr.setId(activityId);
                opr.setActivityName("存就送");
                opr.setRule(jsonUtil.toJson(jDepositSentDto));
                opr.setTmplCode(TOpActtmpl.depositSentCode);
                Integer isPass = depositSentService.checkoutApplyDepositSent(opr, accountId);
                opr.setButtonShow(nonNull(isPass) ? isPass.byteValue() : Constants.EVNumber.four);
                opr.setUseState(actActivity.getUseState());
                activityList.add(opr);
            }
            // 投就送
            if (TOpActtmpl.bettingGiftCode.equals(entry.getKey())) {
                BettingGiftDto bettingGiftDto = dto.getBettingGiftDto();
                if (Objects.isNull(bettingGiftDto)) {
                    throw new R200Exception("投就送子规则错误！");
                }
                OprActActivity opr = new OprActActivity();
                opr.setId(activityId);
                opr.setActivityName("投就送");
                opr.setRule(jsonUtil.toJson(bettingGiftDto));
                opr.setTmplCode(TOpActtmpl.bettingGiftCode);
                Integer isPass = bettingGiftService.checkoutBettingGiftStatus(opr, accountId);
                opr.setButtonShow(nonNull(isPass) ? isPass.byteValue() : Constants.EVNumber.four);
                opr.setUseState(actActivity.getUseState());
                activityList.add(opr);
            }
            // 救援金
            if (TOpActtmpl.rescueCode.equals(entry.getKey())) {
                ActRuleBaseDto<ActRescueRuleDto> actRescueRuleDto = dto.getActRescueRuleDto();
                if (Objects.isNull(actRescueRuleDto)) {
                    throw new R200Exception("救援金子规则错误！");
                }
                OprActActivity opr = new OprActActivity();
                opr.setId(activityId);
                opr.setActivityName("救援金");
                opr.setRule(jsonUtil.toJson(actRescueRuleDto));
                opr.setTmplCode(TOpActtmpl.rescueCode);
                Integer isPass = oprRescueActivityService.checkoutRescueStatus(opr, accountId);
                opr.setButtonShow(nonNull(isPass) ? isPass.byteValue() : Constants.EVNumber.four);
                opr.setUseState(actActivity.getUseState());
                activityList.add(opr);
            }
        }
        return activityList;
    }

    /**
     * 我的优惠-已领取-获取混合活动的子规则数据
     *
     * @param activityId 混合活动id
     * @param accountId  会员id
     * @return 混合活动子规则数据
     */
    public List<OprActActivity> getMixActivityClaimeAll(Integer activityId, Integer accountId) {
        // 获取活动
        OprActActivity actActivity = operateMapper.findOprActActivity(activityId);
        // 校验活动
        String isActivity = oprActActivityService.checkoutActivity(actActivity, Boolean.FALSE);
        if (nonNull(isActivity)) {
            return null;
        }
        // 获取规则
        MixActivityDto dto = jsonUtil.fromJson(actActivity.getRule(), MixActivityDto.class);
        if (isNull(dto)) {
            return null;
        }

        // 规则校验
        Integer applyType = dto.getApplyType();                                     // 获取活动顺序  0 按顺序 1条件满足领取
        String startTime = dto.getStartTime();                                      // 统计开始时间
        HashMap<String, Integer> activityLinkedList = dto.getActivityLinkedList();   // 活动顺序map key: 活动code 存就送AQ0000003 投就送AQ0000012 救援金AQ0000004 其他AQ0000015  value:排序，从1开始从小到大
        if (Objects.isNull(applyType) || StringUtils.isEmpty(startTime) || Collections3.isEmpty(activityLinkedList)) {
            return null;
        }

        // 查询混合活动已领取的子规则红利
        OprActBonus qry = new OprActBonus();
        qry.setAccountId(accountId);
        qry.setActivityId(activityId);
        qry.setStatus(Constants.EVNumber.one);
        List<OprActBonus> bonusList = operateMapper.findAccountBonusList(qry);

        // 处理返回数据
        List<OprActActivity> activityList = new ArrayList<>();
        for (OprActBonus bonus : bonusList) {
            // 存就送
            if (TOpActtmpl.depositSentCode.equals(bonus.getSubRuleTmplCode())) {
                JDepositSentDto jDepositSentDto = dto.getJDepositSentDto();
                if (Objects.isNull(jDepositSentDto)) {
                    continue;
                }
                OprActActivity opr = new OprActActivity();
                opr.setId(activityId);
                opr.setActivityName("存就送");
                opr.setRule(jsonUtil.toJson(jDepositSentDto));
                opr.setTmplCode(TOpActtmpl.depositSentCode);
                opr.setUseState(bonus.getUseState());
                depositSentService.checkoutApplyDepositSent(opr, accountId);
                opr.setAmountMax(bonus.getBonusAmount());   // 已领取的做最高金额显示
                activityList.add(opr);
            }
            // 投就送
            if (TOpActtmpl.bettingGiftCode.equals(bonus.getSubRuleTmplCode())) {
                BettingGiftDto bettingGiftDto = dto.getBettingGiftDto();
                if (Objects.isNull(bettingGiftDto)) {
                    continue;
                }
                OprActActivity opr = new OprActActivity();
                opr.setId(activityId);
                opr.setActivityName("投就送");
                opr.setRule(jsonUtil.toJson(bettingGiftDto));
                opr.setTmplCode(TOpActtmpl.bettingGiftCode);
                opr.setUseState(bonus.getUseState());
                bettingGiftService.checkoutBettingGiftStatus(opr, accountId);
                opr.setAmountMax(bonus.getBonusAmount());   // 已领取的做最高金额显示
                activityList.add(opr);
            }
            // 救援金
            if (TOpActtmpl.rescueCode.equals(bonus.getSubRuleTmplCode())) {
                ActRuleBaseDto<ActRescueRuleDto> actRescueRuleDto = dto.getActRescueRuleDto();
                if (Objects.isNull(actRescueRuleDto)) {
                    continue;
                }
                OprActActivity opr = new OprActActivity();
                opr.setId(activityId);
                opr.setActivityName("救援金");
                opr.setRule(jsonUtil.toJson(actRescueRuleDto));
                opr.setTmplCode(TOpActtmpl.rescueCode);
                opr.setUseState(bonus.getUseState());
                oprRescueActivityService.checkoutRescueStatus(actActivity, accountId);
                opr.setAmountMax(bonus.getBonusAmount());   // 已领取的做最高金额显示
                activityList.add(opr);
            }
            // 其他
            if (TOpActtmpl.otherCode.equals(bonus.getSubRuleTmplCode())) {
                JOtherDto jOtherDto = dto.getJOtherDto();
                if (Objects.isNull(jOtherDto)) {
                    continue;
                }
                OprActActivity opr = new OprActActivity();
                opr.setId(activityId);
                opr.setActivityName("其他");
                opr.setRule(jsonUtil.toJson(jOtherDto));
                opr.setTmplCode(TOpActtmpl.otherCode);
                opr.setAmountMax(bonus.getBonusAmount());                           // 已领取的做最高金额显示
                opr.setMultipleWater(Double.valueOf(jOtherDto.getMultipleWater())); // 流水倍数
                opr.setUseState(bonus.getUseState());
                activityList.add(opr);
            }
        }

        return activityList;
    }

    /**
     * 	校验混合活动，返回前端buttownshow
     *
     * @param actActivity
     * @param accountId
     * @return 4 不显示， 1 立即领取
     */
    public Integer checkoutMixActivityStatus(OprActActivity actActivity, int accountId) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);
        MixActivityDto dto = jsonUtil.fromJson(actActivity.getRule(), MixActivityDto.class);
        if (isNull(dto)) {
            return Constants.EVNumber.four;
        }

        Integer applyType = dto.getApplyType();                                     // 获取活动顺序  0 按顺序 1条件满足领取
        String startTime = dto.getStartTime();                                      // 统计开始时间
        HashMap<String, Integer> activityLinkedList = dto.getActivityLinkedList();   // 活动顺序map key: 活动code 存就送CS 投就送TS 救援金JY 其他RS  value:排序，从1开始从小到大
        if (Objects.isNull(applyType) || StringUtils.isEmpty(startTime) || Collections3.isEmpty(activityLinkedList)) {
            return Constants.EVNumber.four;
        }
        int size = activityLinkedList.size();       // 几个活动

        // 判断是否都已领取,审核失败的也算，按照原有逻辑审核失败不可再次领取
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(actActivity.getId());
        bonus.setAccountId(accountId);
        int count = operateActivityMapper.findBounsCount(bonus);
        if (size <= count) {      // 都已领取，不显示
            return Constants.EVNumber.four;
        }
        
        // 结果标记 1： 用户端展示 4：用户端不展示
        Integer resultFlag = Constants.EVNumber.four;
        
        // 奖励金额
        TreeSet<BigDecimal> bonusMaxSet = new TreeSet<>();
        
        // 按顺序排序Map
        Map<String, Integer> sortMap = CommonUtil.mapSortByValue(activityLinkedList, 1);

        // 校验，只要有一个可以领取就返回可领取
        for (Map.Entry<String, Integer> entry : sortMap.entrySet()) {
            // 校验存就送
            if (TOpActtmpl.depositSentCode.equals(entry.getKey())) {
                JDepositSentDto jDepositSentDto = dto.getJDepositSentDto();
                if (!Objects.isNull(jDepositSentDto) && resultFlag == Constants.EVNumber.four) {
                	boolean result = checkoutApplyDepositSent(jDepositSentDto, mbrAccount);
                	if (result) {
                		resultFlag = Constants.EVNumber.one;
                	}
                }
                // 获取存就送奖励金额
                bonusMaxSet.add(getDonateAmountMaxForMixActivity(jDepositSentDto));
            }
            
            // 校验投就送
            if (TOpActtmpl.bettingGiftCode.equals(entry.getKey())) {
                BettingGiftDto bettingGiftDto = dto.getBettingGiftDto();
                if (!Objects.isNull(bettingGiftDto)) {
                	if (resultFlag == Constants.EVNumber.four) {
                		boolean result = checkoutBettingGiftStatus(bettingGiftDto);
                		if (result) {
                			resultFlag = Constants.EVNumber.one;
                		}
                	}
                	// 获取投就送奖励金额
                	List<BettingGiftRuleDto> bettingGiftRuleDtos = bettingGiftDto.getBettingGiftRuleDtos();
                    BigDecimal betBonusMax = bettingGiftRuleDtos.stream()
                    		.map(BettingGiftRuleDto :: getDonateAmount).max((x1, x2) -> x1.compareTo(x2)).get();
                    bonusMaxSet.add(betBonusMax);
                }
                
            }
            // 校验救援金
            if (TOpActtmpl.rescueCode.equals(entry.getKey())) {
                ActRuleBaseDto<ActRescueRuleDto> actRescueRuleDto = dto.getActRescueRuleDto();
                if (!Objects.isNull(actRescueRuleDto)) {
                	if (resultFlag == Constants.EVNumber.four) {
                		boolean result = checkoutRescueStatus(actRescueRuleDto);
                		if (result) {
                			resultFlag = Constants.EVNumber.one;
                		}
                	}
                	// 获取救援金奖励金额
                	List<ActRescueRuleDto> ruleDtos = actRescueRuleDto.getRuleDtos();
                	BigDecimal rescueBonusMax = ruleDtos.stream()
                			.map(ActRescueRuleDto :: getDonateAmountMax).max((x1, x2) -> x1.compareTo(x2)).get();
                	bonusMaxSet.add(rescueBonusMax);
                }
            }
            // 校验其他
            if (TOpActtmpl.otherCode.equals(entry.getKey())) {
                JOtherDto jOtherDto = dto.getJOtherDto();
                if (!Objects.isNull(jOtherDto)) {
                	if (resultFlag == Constants.EVNumber.four) {
                		if (size != Constants.EVNumber.one) {     // 仅配置了其他，不显示
                			resultFlag = Constants.EVNumber.one;          // 否则存在 其他 ，则显示
                		}
                	}
                    bonusMaxSet.add(jOtherDto.getDonateAmount());
                }
            }
        }
        actActivity.setBonusAmountMin(bonusMaxSet.first());
        actActivity.setAmountMax(bonusMaxSet.last());
        return resultFlag;
    }
    
    
    /**
     * 	获取混合活动 -- 存就送 的最大奖励金额
     * 
     * @param ruleScopeDto
     */
    public BigDecimal getDonateAmountMaxForMixActivity(JDepositSentDto jDepositSentDto) {
    	List<RuleScopeDto> ruleScopeDtos = jDepositSentDto.getRuleScopeDtos();
    	
    	RuleScopeDto ruleScopeDto = new RuleScopeDto();
		ruleScopeDto = ruleScopeDtos.get(0);

		List<ActivityRuleDto> activityRuleDtos = ruleScopeDto.getActivityRuleDtos();
        if (Collections3.isNotEmpty(activityRuleDtos)) {
            if (Collections3.isNotEmpty(activityRuleDtos)) {
            	activityRuleDtos.sort((r1, r2) -> r2.getAmountMax().compareTo(r1.getAmountMax()));
            }
            if (Collections3.isNotEmpty(activityRuleDtos)) {
                // 如果是按比例赠送 取donateAmountMax 
                if (activityRuleDtos.get(0).getDonateType() == Constants.EVNumber.zero) {//按比例赠送
                	return nonNull(activityRuleDtos.get(0).getDonateAmountMax())
                            ? activityRuleDtos.get(0).getDonateAmountMax() : BigDecimal.ZERO;
                }
                // 固定金额
              return activityRuleDtos.get(0).getDonateAmount();
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * 	校验混合活动-1.校验存就送
     *
     * @param dto
     * @param mbrAccount
     * @return true 符合 fasle 不符合
     */
    private boolean checkoutApplyDepositSent(JDepositSentDto dto, MbrAccount mbrAccount) {
        RuleScopeDto ruleScopeDto = actActivityCastService.getRuleScopeDtos(dto.getRuleScopeDtos(), mbrAccount.getActLevelId(), dto.getScope());
        if (isNull(ruleScopeDto)) {
            return false;
        }
        return true;
    }

    /**
     * 校验混合活动-2.校验投就送
     *
     * @param bettingGiftDto
     * @return true 符合 fasle 不符合
     */
    private boolean checkoutBettingGiftStatus(BettingGiftDto bettingGiftDto) {
        if (isNull(bettingGiftDto) || Collections3.isEmpty(bettingGiftDto.getBettingGiftRuleDtos())) {
            return false;
        }
        List<BettingGiftRuleDto> ruleDtos = bettingGiftDto.getBettingGiftRuleDtos();
        if (Collections3.isEmpty(ruleDtos)) {
            return false;
        }
        return true;
    }

    /**
     * 校验混合活动-3.校验救援金
     *
     * @param actRescueRuleDto
     * @return true 符合 fasle 不符合
     */
    public boolean checkoutRescueStatus(ActRuleBaseDto<ActRescueRuleDto> actRescueRuleDto) {
        if (isNull(actRescueRuleDto) || Collections3.isEmpty(actRescueRuleDto.getRuleDtos())) {
            return false;
        }
        List<ActRescueRuleDto> ruleDtos = actRescueRuleDto.getRuleDtos();
        if (Collections3.isEmpty(ruleDtos)) {
            return false;
        }
        return true;
    }

    /**
     * 会员申请领取混合活动红利
     *
     * @param actActivity     混合活动
     * @param subRuleTmplCode 领取的子规则code 存就送AQ0000003 投就送AQ0000012 救援金AQ0000004 （其他AQ0000015）
     * @param accountId       领取的会员id
     * @param ip
     */
    public void applyMixActivity(OprActActivity actActivity, String subRuleTmplCode, int accountId, String ip) {
        // 获取混合活动规则
        MixActivityDto dto = jsonUtil.fromJson(actActivity.getRule(), MixActivityDto.class);
        if (Objects.isNull(dto)) {
            throw new R200Exception("规则为空！");
        }

        // 校验规则
        Integer applyType = dto.getApplyType();                                     // 获取活动顺序  0 按顺序 1条件满足领取
        String startTime = dto.getStartTime();                                      // 统计开始时间
        HashMap<String, Integer> activityLinkedList = dto.getActivityLinkedList();   // 活动顺序map key: 活动code 存就送AQ0000003 投就送AQ0000012 救援金AQ0000004 其他AQ0000015  value:排序，从1开始从小到大
        if (Objects.isNull(applyType) || StringUtils.isEmpty(startTime) || Collections3.isEmpty(activityLinkedList)) {
            throw new R200Exception("规则错误！");
        }

        // 活动按先后顺序排序
        Map<String, Integer> sortMap = CommonUtil.mapSortByValue(activityLinkedList, 1);

        // 按顺序领取情况
        if (Integer.valueOf(Constants.EVNumber.zero).equals(applyType)) {
            for (Map.Entry<String, Integer> entry : sortMap.entrySet()) {
                // map按顺序，所以不等于此次领取的都是需要先领取的
                if (!entry.getKey().equals(subRuleTmplCode)) {
                    // 查询是否已领取子规则
                    OprActBonus bonus = new OprActBonus();
                    bonus.setAccountId(accountId);
                    bonus.setActivityId(actActivity.getId());
                    bonus.setSubRuleTmplCode(entry.getKey());
                    bonus.setStatus(Constants.EVNumber.one);
                    List<OprActBonus> bonusList = actBonusMapper.select(bonus);

                    // 未领取不能领取当前申请的
                    if (Collections3.isEmpty(bonusList)) {
                        // 存就送
                        if (TOpActtmpl.depositSentCode.equals(entry.getKey())) {
                            throw new R200Exception("请先成功领取存就送！");
                        }
                        // 投就送
                        if (TOpActtmpl.bettingGiftCode.equals(entry.getKey())) {
                            throw new R200Exception("请先成功领取投就送！");
                        }
                        // 救援金
                        if (TOpActtmpl.rescueCode.equals(entry.getKey())) {
                            throw new R200Exception("请先成功领取救援金！");
                        }
                        // 其他
                        if (TOpActtmpl.otherCode.equals(entry.getKey())) {
                            throw new R200Exception("顺序领取，有未领取的优惠，请与客服联系！");
                        }
                    }

                    if (bonusList.size() > Constants.EVNumber.one) {
                        throw new R200Exception(entry.getKey() + "存在多个领取结果！");
                    }

                    // 获取审核通过时间：顺序，所以上一个就是领取的开始计算时间
                    OprActBonus bonusPre = bonusList.get(0);
                    // 把上一个通过的审核时间设置为计算起始时间
                    startTime = bonusPre.getAuditTime();
                }

                // 前序已领取的，则领取此次申请的子规则，并结束循环
                if (entry.getKey().equals(subRuleTmplCode)) {
                    // 存就送
                    if (TOpActtmpl.depositSentCode.equals(entry.getKey())) {
                        JDepositSentDto jDepositSentDto = dto.getJDepositSentDto();
                        if (Objects.isNull(jDepositSentDto)) {
                            throw new R200Exception("存就送子规则错误！");
                        }

                        applyDepositSentBonus(jDepositSentDto, actActivity, subRuleTmplCode, startTime, accountId, ip);
                    }
                    // 投就送
                    if (TOpActtmpl.bettingGiftCode.equals(entry.getKey())) {
                        BettingGiftDto bettingGiftDto = dto.getBettingGiftDto();
                        if (Objects.isNull(bettingGiftDto) || Collections3.isEmpty(bettingGiftDto.getBettingGiftRuleDtos())) {
                            throw new R200Exception("投就送子规则错误！");
                        }

                        applyBettingGiftBonus(bettingGiftDto, actActivity, subRuleTmplCode, startTime, accountId, ip);
                    }
                    // 救援金
                    if (TOpActtmpl.rescueCode.equals(entry.getKey())) {
                        ActRuleBaseDto<ActRescueRuleDto> actRescueRuleDto = dto.getActRescueRuleDto();
                        if (Objects.isNull(actRescueRuleDto)) {
                            throw new R200Exception("救援金子规则错误！");
                        }
                        applyRescue(actRescueRuleDto, actActivity, subRuleTmplCode, startTime, accountId, ip);
                    }
                    // 结束for
                    break;
                }
            }
        }

        // 符合规则即可领取情况
        if (Integer.valueOf(Constants.EVNumber.one).equals(applyType)) {
            // 存就送
            if (TOpActtmpl.depositSentCode.equals(subRuleTmplCode)) {
                JDepositSentDto jDepositSentDto = dto.getJDepositSentDto();
                if (Objects.isNull(jDepositSentDto)) {
                    throw new R200Exception("存就送子规则错误！");
                }

                applyDepositSentBonus(jDepositSentDto, actActivity, subRuleTmplCode, startTime, accountId, ip);
            }
            // 投就送
            if (TOpActtmpl.bettingGiftCode.equals(subRuleTmplCode)) {
                BettingGiftDto bettingGiftDto = dto.getBettingGiftDto();
                if (Objects.isNull(bettingGiftDto) || Collections3.isEmpty(bettingGiftDto.getBettingGiftRuleDtos())) {
                    throw new R200Exception("投就送子规则错误！");
                }

                applyBettingGiftBonus(bettingGiftDto, actActivity, subRuleTmplCode, startTime, accountId, ip);
            }
            // 救援金
            if (TOpActtmpl.rescueCode.equals(subRuleTmplCode)) {
                ActRuleBaseDto<ActRescueRuleDto> actRescueRuleDto = dto.getActRescueRuleDto();
                if (Objects.isNull(actRescueRuleDto)) {
                    throw new R200Exception("救援金子规则错误！");
                }
                applyRescue(actRescueRuleDto, actActivity, subRuleTmplCode, startTime, accountId, ip);
            }
        }
    }

    /**
     * 校验是否领过子规则红利，被拒绝的也算是，不让再次领，复用之前的约定
     *
     * @param activityId      混合活动id
     * @param subRuleTmplCode 子规则code
     * @param accountId       会员id
     * @return 返回是否领取 true 未领取 false 已领取
     */
    public Boolean checkoutIsApplyBonus(Integer activityId, String subRuleTmplCode, Integer accountId) {
        OprActBonus bonus = new OprActBonus();
        bonus.setAccountId(accountId);
        bonus.setActivityId(activityId);
        bonus.setSubRuleTmplCode(subRuleTmplCode);
        int count = operateActivityMapper.findBounsCount(bonus);
        if (count > 0) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 混合活动子规则申请-存就送
     *
     * @param dto             存就送规则dto
     * @param actActivity     混合活动
     * @param subRuleTmplCode 子规则code
     * @param startTime       计算开始时间
     * @param accountId       会员id
     * @param ip              会员申请ip
     */
    public void applyDepositSentBonus(JDepositSentDto dto, OprActActivity actActivity, String subRuleTmplCode,
                                      String startTime, int accountId, String ip) {
        MbrAccount account = mbrMapper.findMbrAccount(accountId, null, null);

        // 获得符合会员层级的规则
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

        // 校验该会员是否已经领取过混合活动的存就送 false 已领取
        Boolean isApply = checkoutIsApplyBonus(actActivity.getId(), subRuleTmplCode, account.getId());
        if (Boolean.FALSE.equals(isApply)) {
            throw new R200Exception(ActivityConstants.CLAIMED);
        }

        // 获得计算时间内的审核通过的存款总额
        BigDecimal depositAmount = fundMapper.sumFundDepositVipRed(account.getId(), startTime, getCurrentDate(FORMAT_18_DATE_TIME));
        if (depositAmount.compareTo(BigDecimal.ZERO) == 0) {    // 未存款
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // 拿实际存款总额去比对规则，获取符合的规则
        ActivityRuleDto ruleDto = actActivityCastService.getActivityRuleDto(ruleScopeDto.getActivityRuleDtos(), depositAmount);
        if (Objects.isNull(ruleDto)) {  // 存款金额不满足活动
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // 该笔bonus属于会员哪个层级领取 null表示规则为全部时领取
        Integer actLevelId = null;
        if (dto.getScope() == Constants.EVNumber.one) {  // 当为层级会员  会员范围 0全部会员 1层级会员
            actLevelId = account.getActLevelId();
        }

        // 处理优惠bonus
        oprDepositSentService.accountDepositSent(actActivity, ruleScopeDto, OrderConstants.ACTIVITY_HH, subRuleTmplCode,
                null, depositAmount, account, ip, actLevelId, null);
    }

    /**
     * 混合活动子规则申请-投就送
     *
     * @param dto             存就送规则dto
     * @param actActivity     混合活动
     * @param subRuleTmplCode 子规则code
     * @param startTime       计算开始时间
     * @param accountId       会员id
     * @param ip              会员申请ip
     */
    public void applyBettingGiftBonus(BettingGiftDto dto, OprActActivity actActivity, String subRuleTmplCode,
                                      String startTime, int accountId, String ip) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);

        // 校验规则-混合活动的投就送没有统计周期的区分
        List<BettingGiftRuleDto> ruleDtos = dto.getBettingGiftRuleDtos();
        if (Collections3.isEmpty(ruleDtos)) {
            throw new R200Exception("投就送子规则错误");
        }

        // 校验姓名等信息
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, dto.getIsName(),
                dto.getIsBank(), dto.getIsMobile(), false, dto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }

        // 校验该会员是否已经领取过混合活动的投就送
        Boolean isApply = checkoutIsApplyBonus(actActivity.getId(), subRuleTmplCode, mbrAccount.getId());
        if (Boolean.FALSE.equals(isApply)) {
            throw new R200Exception(ActivityConstants.CLAIMED);
        }

        // 获取统计时间
        Map<String, String> timeMap = new HashMap<>();
        timeMap.put("startTime", startTime);
        timeMap.put("endTime", getCurrentDate(FORMAT_18_DATE_TIME));

        // 获取投注和存款满足条件的规则
        BettingGiftRuleDto ruleDto = bettingGiftService.checkoutBettingGiftValidBet(dto, mbrAccount, timeMap, true);
        if (isNull(ruleDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }
        // 处理优惠bonus
        bettingGiftService.accountBettingGift(ruleDto, actActivity, mbrAccount, ip, OrderConstants.ACTIVITY_HH, subRuleTmplCode);
    }

    /**
     * 混合活动子规则申请-救援金
     *
     * @param dto
     * @param actActivity
     * @param subRuleTmplCode
     * @param startTime
     * @param accountId
     * @param ip
     */
    public void applyRescue(ActRuleBaseDto<ActRescueRuleDto> dto, OprActActivity actActivity, String subRuleTmplCode,
                            String startTime, int accountId, String ip) {
        MbrAccount mbrAccount = mbrMapper.findMbrAccount(accountId, null, null);

        // 校验姓名等信息
        String isAccountMsg = actActivityCastService.checkoutAccountMsg(mbrAccount, Constants.EVNumber.zero, dto.getIsName(),
                dto.getIsBank(), dto.getIsMobile(), false, dto.getIsApp());
        if (StringUtils.isNotEmpty(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }

        // 校验该会员是否已经领取过混合活动的救援金
        Boolean isApply = checkoutIsApplyBonus(actActivity.getId(), subRuleTmplCode, mbrAccount.getId());
        if (Boolean.FALSE.equals(isApply)) {
            throw new R200Exception(ActivityConstants.CLAIMED);
        }

        // 获取统计时间
        Map<String, String> timeMap = new HashMap<>();
        timeMap.put("startTime", startTime);
        timeMap.put("endTime", getCurrentDate(FORMAT_18_DATE_TIME));

        // 获取满足条件的规则
        ActRescueRuleDto rescueRuleDto = oprRescueActivityService.checkoutRescuePayout(dto, mbrAccount, timeMap, true);
        if (Objects.isNull(rescueRuleDto)) {
            throw new R200Exception(ActivityConstants.INCOMPATIBLE);
        }

        // 处理优惠bonus
        oprRescueActivityService.accountRescue(rescueRuleDto, actActivity, mbrAccount, ip, OrderConstants.ACTIVITY_HH, subRuleTmplCode);
    }

    /**
     * 根据类别和平台获取会员有效投注
     *
     * @param timeMap      统计时间map
     * @param mbrAccount   会员
     * @param gameCategory 游戏类别
     * @param platForms    平台codes
     * @return
     */
    public BigDecimal getValidBetTotal(Map<String, String> timeMap, MbrAccount mbrAccount, String gameCategory, List<String> platForms) {
        // 处理查询时间
        String startTime = formatEsDate(timeMap.get("startTime"));
        String endTime = formatEsDate(timeMap.get("endTime"));

        // 查询条件
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        builder.must(QueryBuilders.rangeQuery("payoutTime").gte(startTime).lt(endTime));    // 派彩时间
        builder.must(QueryBuilders.termsQuery("sitePrefix", analysisService.toLowerCase(analysisMapper.getApiPrefixBySiteCode(CommonUtil.getSiteCode())))); // 站点前缀
        builder.must(QueryBuilders.termsQuery("userName", mbrAccount.getLoginName()));      // 会员名
        builder.must(QueryBuilders.termsQuery("gameCategory", gameCategory));               // 游戏类别
        // 游戏平台改为小写，term语法只支持小写
        List<String> lowPlatForms = new ArrayList<>();
        platForms.stream().forEach(as -> {
            lowPlatForms.add(as.toLowerCase());
        });
        builder.must(QueryBuilders.termsQuery("platform", lowPlatForms));                      // 游戏平台
        // 平台查询条件
//        BoolQueryBuilder pfbuilder = QueryBuilders.boolQuery();
//        platForms.forEach(pf -> {
//            pfbuilder.should(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("platform", pf.toLowerCase())));
//        });
//        builder.must(pfbuilder);

        // 聚合参数
        SumAggregationBuilder agg = AggregationBuilders.sum("validBet").field("validBet");

        // SearchRequestBuilder
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        searchRequestBuilder.setQuery(builder);     // 查询条件
        searchRequestBuilder.addAggregation(agg);   // 聚合参数
        String str = searchRequestBuilder.toString();
        log.info("MixActivity==getValidBetTotal==loginame==" + mbrAccount.getLoginName() + "==es==" + str);

        // 请求
        try {
            Response response = connection.restClient_Read.performRequest("GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"), new NStringEntity(str, ContentType.APPLICATION_JSON));

            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            Map validBet = (Map) ((Map) map.get("aggregations")).get("validBet");
            if (Objects.nonNull(validBet)) {
                BigDecimal totalValidBet = new BigDecimal(validBet.get("value").toString()).setScale(2, BigDecimal.ROUND_DOWN);
                log.info("MixActivity==getPayoutTotal====loginame==" + mbrAccount.getLoginName() + "==totalValidBet==" + totalValidBet);
                return totalValidBet;
            }
        } catch (Exception e) {
            log.info("MixActivity==getValidBetTotal==loginame==" + mbrAccount.getLoginName() + "==error==" + e);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 根据类别和平台获取会员派彩
     *
     * @param timeMap      统计时间map
     * @param mbrAccount   会员
     * @param gameCategory 游戏类别
     * @param platForms    平台codes
     * @return
     */
    public BigDecimal getPayoutTotal(Map<String, String> timeMap, MbrAccount mbrAccount, String gameCategory, List<String> platForms) {
        // 处理查询时间
        String startTime = formatEsDate(timeMap.get("startTime"));
        String endTime = formatEsDate(timeMap.get("endTime"));

        // 查询条件
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        builder.must(QueryBuilders.rangeQuery("betTime").gte(startTime).lt(endTime));    // 此处使用投注时间
        builder.must(QueryBuilders.termsQuery("sitePrefix", analysisService.toLowerCase(analysisMapper.getApiPrefixBySiteCode(CommonUtil.getSiteCode())))); // 站点前缀
        builder.must(QueryBuilders.termsQuery("userName", mbrAccount.getLoginName()));      // 会员名
        builder.must(QueryBuilders.termsQuery("gameCategory", gameCategory));               // 游戏类别
        // 游戏平台改为小写，term语法只支持小写
        List<String> lowPlatForms = new ArrayList<>();
        platForms.stream().forEach(as -> {
            lowPlatForms.add(as.toLowerCase());
        });
        builder.must(QueryBuilders.termsQuery("platform", lowPlatForms));                      // 游戏平台
        builder.must(QueryBuilders.queryStringQuery("已").defaultField("status"));                 // 计算状态 已结算

        // 聚合参数
        SumAggregationBuilder agg = AggregationBuilders.sum("payout").field("payout");

        // SearchRequestBuilder
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        searchRequestBuilder.setQuery(builder);     // 查询条件
        searchRequestBuilder.addAggregation(agg);   // 聚合参数
        String str = searchRequestBuilder.toString();
        log.info("MixActivity==getPayoutTotal==loginame==" + mbrAccount.getLoginName() + "==es==" + str);

        // 请求
        try {
            Response response = connection.restClient_Read.performRequest("GET", "/" + ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"), new NStringEntity(str, ContentType.APPLICATION_JSON));

            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            Map validBet = (Map) ((Map) map.get("aggregations")).get("payout");
            if (Objects.nonNull(validBet)) {
                BigDecimal totalPayout = new BigDecimal(validBet.get("value").toString()).setScale(2, BigDecimal.ROUND_DOWN);
                log.info("MixActivity==getPayoutTotal====loginame==" + mbrAccount.getLoginName() + "==totalPayout==" + totalPayout);
                return totalPayout;
            }
        } catch (Exception e) {
            log.error("MixActivity==getPayoutTotal====loginame==" + mbrAccount.getLoginName() + "==error==" + e);
        }
        return BigDecimal.ZERO;
    }
}
