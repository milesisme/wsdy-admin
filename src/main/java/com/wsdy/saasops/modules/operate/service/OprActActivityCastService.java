package com.wsdy.saasops.modules.operate.service;

import com.wsdy.saasops.api.modules.apisys.entity.TGmApiprefix;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.member.dao.MbrBankcardMapper;
import com.wsdy.saasops.modules.member.dao.MbrWalletMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.dao.*;
import com.wsdy.saasops.modules.operate.dto.*;
import com.wsdy.saasops.modules.operate.entity.*;
import com.wsdy.saasops.modules.operate.mapper.GameMapper;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicOnlinepay;
import com.wsdy.saasops.modules.system.pay.mapper.PayMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

import static com.wsdy.saasops.common.utils.DateUtil.*;
import static com.wsdy.saasops.modules.operate.entity.TOpActtmpl.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class OprActActivityCastService {

    @Autowired
    private MbrBankcardMapper bankcardMapper;
    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private OprActRuleMapper actRuleMapper;
    @Autowired
    private TGmCatMapper gmCatMapper;
    @Autowired
    private TGmDepotMapper gmDepotMapper;
    @Autowired
    private MbrWalletMapper walletMapper;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private AuditAccountService accountAuditService;
    @Autowired
    private TGmApiService tGmApiService;
    @Autowired
    private OprApplyfirstDepositService applyfirstDepositService;
    @Autowired
    private PayMapper payMapper;

    private void applyActivityMsg(String siteCode, OprActBonus actBonus, String acvitityName) {
        BizEvent bizEvent = new BizEvent(this, siteCode, actBonus.getAccountId(),
                BizEventType.PROMOTE_VERIFY_SUCCESS);
        bizEvent.setAcvitityMoney(actBonus.getBonusAmount());
        bizEvent.setAcvitityName(acvitityName);
        applicationEventPublisher.publishEvent(bizEvent);
    }

    public Integer getPaymentTypeByDeposit(FundDeposit deposit) {
        // ?????????????????? 0??????, 1, 2, 3, 4, 5, 7, 8, 100USDT, 101???????????????
        Integer paymentType = 0;
        if (deposit.getCompanyPayId() != null && deposit.getCompanyPayId() != 0) {
            // ???????????????????????????????????????
            paymentType = 101;
        }
        if (deposit.getOnlinePayId() != null && deposit.getOnlinePayId() != 0) {
            // ???????????????ID????????????????????????
            SetBacicOnlinepay onlinepay = payMapper.findOnlinePayAndPay(deposit.getOnlinePayId());
            paymentType = onlinepay.getPayId();
        }
        if (deposit.getCrId() != null && deposit.getCrId() != 0) {
            // ???USDT ID????????????USDT??????
            paymentType = 100;
        }
        return paymentType;
    }

    /**
     * ?????????????????????
     * @param ruleDtos
     * @param deposit
     * @return
     */
    public ActivityRuleDto getJDepositActivityRuleDto(List<ActivityRuleDto> ruleDtos, FundDeposit deposit) {
        BigDecimal amount = deposit.getDepositAmount();
        Integer paymentType = getPaymentTypeByDeposit(deposit);
        if (amount != null) {
            ruleDtos.sort((r1, r2) -> r2.getAmountMax().compareTo(r1.getAmountMax()));
            for (ActivityRuleDto rs : ruleDtos) {
                Boolean isActivity = false;
                if (rs.getPaymentType() != null && (rs.getPaymentType() == Constants.EVNumber.zero || rs.getPaymentType() == paymentType)) {
                    isActivity = compareAmount(amount, rs.getAmountMin(), rs.getAmountMax());
                }
                if (Boolean.TRUE.equals(isActivity)) {
                    return rs;
                }
            }
        }
        return null;
    }

    public ActivityRuleDto getActivityRuleDto(List<ActivityRuleDto> ruleDtos, BigDecimal amount) {
    	if (amount != null) {
	        ruleDtos.sort((r1, r2) -> r2.getAmountMax().compareTo(r1.getAmountMax()));
	        for (ActivityRuleDto rs : ruleDtos) {
	            Boolean isActivity = compareAmount(amount, rs.getAmountMin(), rs.getAmountMax());
	            if (Boolean.TRUE.equals(isActivity)) {
	                return rs;
	            }
	        }
        }
        return null;
    }

    public void setDonateAmountMax(OprActActivity activity, Integer actLevelId) {
        RuleScopeDto ruleScopeDto = null;
        if (TOpActtmpl.preferentialCode.equals(activity.getTmplCode())) {
            JPreferentialDto dto = jsonUtil.fromJson(activity.getRule(), JPreferentialDto.class);
            ruleScopeDto = getRuleScopeDtos(dto.getRuleScopeDtos(), actLevelId, dto.getScope());
        }
        if (TOpActtmpl.depositSentCode.equals(activity.getTmplCode()) || TOpActtmpl.vipPrivilegesCode.equals(activity.getTmplCode())) {
            JDepositSentDto dto = jsonUtil.fromJson(activity.getRule(), JDepositSentDto.class);
            ruleScopeDto = getRuleScopeDtos(dto.getRuleScopeDtos(), actLevelId, dto.getScope());
        }
        if (Objects.nonNull(ruleScopeDto)) {
            List<ActivityRuleDto> ruleDtos = ruleScopeDto.getActivityRuleDtos();
            if (Collections3.isNotEmpty(ruleDtos)) {
                setDonateAmountMaxEx(activity,ruleScopeDto);
            }
        }
    }

    public void setDonateAmountMaxEx(OprActActivity activity, RuleScopeDto ruleScopeDto) {
        List<ActivityRuleDto> ruleDtos = ruleScopeDto.getActivityRuleDtos();
        if (Collections3.isNotEmpty(ruleDtos)) {
            if (Collections3.isNotEmpty(ruleDtos)) {
                ruleDtos.sort((r1, r2) -> r2.getAmountMax().compareTo(r1.getAmountMax()));
            }
            if (Collections3.isNotEmpty(ruleDtos)) {
                activity.setDonateType(ruleDtos.get(0).getDonateType());
                activity.setAmountMax(ruleDtos.get(0).getDonateAmount());
                // ????????????????????????
                if (ruleDtos.get(0).getDonateType() == Constants.EVNumber.zero) {//???????????????
                	// set ??????????????????
                    activity.setDonateAmount(ruleDtos.get(0).getDonateAmount());
                    // ??????????????????
                    BigDecimal donateAmountMax = nonNull(ruleDtos.get(0).getDonateAmountMax())
                            ? ruleDtos.get(0).getDonateAmountMax() : BigDecimal.ZERO;
                    int compare = donateAmountMax.compareTo(BigDecimal.ZERO);
                    activity.setAmountMax(compare == 1 ? ruleDtos.get(0).getDonateAmountMax() : null);
                }
                activity.setMultipleWater(ruleDtos.get(0).getMultipleWater());
            }
            activity.setAmountMin(ruleDtos.get(ruleDtos.size() - 1).getAmountMin());
        }
    }

    public Boolean compareAmount(BigDecimal amount, BigDecimal amountMin, BigDecimal amountMax) {
        if (amount.compareTo(amountMax) == 0 || amount.compareTo(amountMax) == 1) {
            return Boolean.TRUE;
        }
        if (amount.compareTo(amountMin) == 0 || amount.compareTo(amountMin) == 1) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public void setBonusAmount(BigDecimal amount, OprActBonus bonus, List<ActivityRuleDto> ruleDtos, Integer formulaMode1, FundDeposit deposit) {
    	// ???????????? 1??? ?????????
        Integer formulaMode = isNull(formulaMode1) ? Constants.EVNumber.one : formulaMode1;
        if (Collections3.isNotEmpty(ruleDtos)) {
            ruleDtos.sort((r1, r2) -> r2.getAmountMax().compareTo(r1.getAmountMax()));
        }
        // ????????????
        TreeSet<Integer> auditAmountSet = new TreeSet<Integer>();
        // ??????????????????
        TreeSet<BigDecimal> amountMaxSet = new TreeSet<BigDecimal>();
        // ?????????????????????
        BigDecimal donateAmount = BigDecimal.ZERO;
        // ?????????????????????
        Integer paymentType = null;
        if (deposit != null) {
            paymentType = getPaymentTypeByDeposit(deposit);
        }
       
        for (int j = 0; j < ruleDtos.size(); j++) {
            ActivityRuleDto ruleDto = ruleDtos.get(j);
            if (ruleDto.getPaymentType() != null && paymentType != null) {
                // ?????????????????????????????????
                if (ruleDto.getPaymentType() != paymentType && ruleDto.getPaymentType() != Constants.EVNumber.zero) {
                    continue;
                }
            }
            int multipleWater = nonNull(ruleDto.getMultipleWater()) ? ruleDto.getMultipleWater().intValue() : 0;
            // ?????????
            if (formulaMode == Constants.EVNumber.one) {
                if (amount != null && compareAmount(amount, ruleDto.getAmountMin(), ruleDto.getAmountMax())) {
                	// ????????????
                    bonus.setDiscountAudit(new BigDecimal(multipleWater));
                    // ????????????
                    donateAmount = getBonusAmount(ruleDto, amount);
                    // ???????????????????????????????????????????????????
                    BigDecimal auditAmount = getAuditAmount(donateAmount, amount, multipleWater, ruleDto.getAmountMax());
                    bonus.setAuditAmount(auditAmount);
                    bonus.setBonusAmount(getBonusAmount(ruleDto, amount));
                    break;
                }
            }
            // ?????????????????????????????????
            else {
            	if (amount != null && compareAmount(amount, ruleDto.getAmountMin(), ruleDto.getAmountMax())) {
            		// ???????????????????????????
            		auditAmountSet.add(multipleWater);
            		// ????????????
            		donateAmount = donateAmount.add(getBonusAmount(ruleDto, amount));
            		amountMaxSet.add(ruleDto.getAmountMax());
            	}
            }
        }
        // ???????????????????????????????????????=???????????? * ?????????????????? -1???+?????????????????????????????? * ???????????????
        if (formulaMode != Constants.EVNumber.one) {
        	ActivityRuleDto ruleDto = new ActivityRuleDto();
        	// ??????????????????????????????
        	ruleDto.setDonateAmount(donateAmount);
        	// ??????????????????
        	Integer multipleWater = auditAmountSet.first();
    		// ????????????
    		bonus.setDiscountAudit(new BigDecimal(multipleWater));
    		// ????????????
    		bonus.setAuditAmount(getAuditAmount(donateAmount, amount, multipleWater, amountMaxSet.last()));
    		// ???????????????
        	bonus.setBonusAmount(donateAmount);
        }
    }

    
	/**
	 * 	??????????????????????????????
	 * @param ruleDto
	 * @param amount  ?????????????????????
	 * @return
	 */
	private BigDecimal getBonusAmount(ActivityRuleDto ruleDto, BigDecimal amount) {
		BigDecimal result = new BigDecimal("0.0");
		// ??????????????????
		if (ruleDto.getDonateType() == Constants.EVNumber.zero) {
			// ????????????
			BigDecimal donateAmount = ruleDto.getDonateAmount().divide(new BigDecimal(Constants.ONE_HUNDRED));
			// ?????? * ???????????? = ????????????
			result = CommonUtil.adjustScale(donateAmount.multiply(amount));
			// ?????? ???????????? ???????????????????????? ?????????????????????????????????
			if (nonNull(ruleDto.getDonateAmountMax()) && ruleDto.getDonateAmountMax().compareTo(BigDecimal.ZERO) == 1
					&& result.compareTo(ruleDto.getDonateAmountMax()) == 1) {
				result = ruleDto.getDonateAmountMax();
			}
		}
		// ????????????????????????
		else {
			result = ruleDto.getDonateAmount();
		}
		return result;
	}
	
    /**
     * 	?????????????????????
     * @param donateAmount   ????????????
     * @param amount		  ????????????
     * @param multipleWater	  ????????????
     * @return
     */
    public BigDecimal getAuditAmount(BigDecimal donateAmount, BigDecimal amount, Integer multipleWater, BigDecimal amountMax) {
    	if (amount.compareTo(amountMax) > 0) {
    		amount = amountMax;
    	}
		// ????????? ????????????  ??????*???n-1???+??????*n???   n?????????????????????
		BigDecimal discountAudit = donateAmount.multiply(new BigDecimal(multipleWater));
		BigDecimal depositAudit = amount.multiply(new BigDecimal(multipleWater - 1));
		return discountAudit.add(depositAudit);
    }

    public void auditDepositSentBonus(OprActBonus bonus, String financialCode, String activityName, Boolean isActivityMsg) {
        accountAuditService.insertAccountAudit(bonus.getAccountId(), bonus.getDepositedAmount(),
                bonus.getDepositId(), bonus.getDiscountAudit(), bonus.getAuditAmount(),
                bonus.getBonusAmount(), bonus.getRuleId(), Constants.EVNumber.six);
        successBonus(bonus, financialCode, activityName, isActivityMsg);
    }


    public void auditOprActBonus(OprActBonus bonus, String financialCode, String activityName, Boolean isActivityMsg) {
        // ??????????????????????????????????????????
        if ((nonNull(bonus.getDiscountAudit()) && bonus.getDiscountAudit().compareTo(BigDecimal.ZERO)==1)
                || (nonNull(bonus.getAuditAmount()) && bonus.getAuditAmount().compareTo(BigDecimal.ZERO) == 1)) {
            Integer isSign = Constants.EVNumber.two;
            if (nonNull(bonus.getAuditAmount())){   // ????????????????????????????????????????????????????????????????????????
                isSign = Constants.EVNumber.six;
            }
            if (nonNull(bonus.getMemo())&&"MSF".equals(bonus.getMemo())){ //????????????
                isSign = Constants.EVNumber.eight;
            }
            accountAuditService.insertAccountAudit(bonus.getAccountId(), bonus.getDepositedAmount(),
                    bonus.getDepositId(), bonus.getDiscountAudit(), bonus.getAuditAmount(),
                    bonus.getBonusAmount(), bonus.getRuleId(), isSign);
        }
        successBonus(bonus, financialCode, activityName, isActivityMsg);
    }
    
    public void auditOprActBonusOrSubtract(OprActBonus bonus, String financialCode, String activityName, Boolean isActivityMsg, Boolean isAdd) {
    	// ??????????????????????????????????????????
    	if ((nonNull(bonus.getDiscountAudit()) && bonus.getDiscountAudit().compareTo(BigDecimal.ZERO)==1)
    			|| (nonNull(bonus.getAuditAmount()) && bonus.getAuditAmount().compareTo(BigDecimal.ZERO) == 1)) {
    		Integer isSign = Constants.EVNumber.two;
    		if (nonNull(bonus.getAuditAmount())){   // ????????????????????????????????????????????????????????????????????????
    			isSign = Constants.EVNumber.six;
    		}
    		accountAuditService.insertAccountAudit(bonus.getAccountId(), bonus.getDepositedAmount(),
    				bonus.getDepositId(), bonus.getDiscountAudit(), bonus.getAuditAmount(),
    				bonus.getBonusAmount(), bonus.getRuleId(), isSign);
    	}
    	// ??????????????????????????????????????????
    	if (isAdd) {
    		successBonusOrSubtract(bonus, financialCode, activityName, isActivityMsg, isAdd);
    	}
    }

    public void auditLotteryActBonus(OprActBonus bonus) {
        bonus.setStatus(Constants.EVNumber.one);
        actBonusMapper.updateByPrimaryKeySelective(bonus);
    }

    private void successBonusOrSubtract(OprActBonus bonus, String financialCode, String activityName, Boolean isActivityMsg, Boolean isSign) {
    	MbrBillDetail mbrBillDetail = mbrWalletService.castWalletAndBillDetail(bonus.getLoginName(),
    			bonus.getAccountId(), financialCode, bonus.getBonusAmount(),
    			bonus.getOrderNo().toString(), isSign, null, null);
    	
	    if (mbrBillDetail == null) {
           throw new R200Exception("????????????????????????????????????");
        }
    	bonus.setStatus(Constants.EVNumber.one);
    	bonus.setBillDetailId(mbrBillDetail.getId());
    	actBonusMapper.updateByPrimaryKeySelective(bonus);
    	
    	if (Boolean.TRUE.equals(isActivityMsg)) {
    		applyActivityMsg(CommonUtil.getSiteCode(), bonus, activityName);
    	}
    }
    
    private void successBonus(OprActBonus bonus, String financialCode, String activityName, Boolean isActivityMsg) {
        MbrBillDetail mbrBillDetail = mbrWalletService.castWalletAndBillDetail(bonus.getLoginName(),
                bonus.getAccountId(), financialCode, bonus.getBonusAmount(),
                bonus.getOrderNo().toString(), Boolean.TRUE, null, null);

        bonus.setStatus(Constants.EVNumber.one);
        bonus.setBillDetailId(mbrBillDetail.getId());
        actBonusMapper.updateByPrimaryKeySelective(bonus);

        if (Boolean.TRUE.equals(isActivityMsg)) {
            applyActivityMsg(CommonUtil.getSiteCode(), bonus, activityName);
        }
    }

    // ??????bonus????????????
    public OprActBonus setOprActBonus(Integer accountId, String loginName, Integer activityId,
                                      BigDecimal depositAmount, Integer depositId, Integer ruleId) {
        OprActBonus bonus = new OprActBonus();
        bonus.setAccountId(accountId);      // ??????id
        bonus.setLoginName(loginName);      // ?????????
        bonus.setActivityId(activityId);    // ??????id
        bonus.setRuleId(ruleId);            // ??????id

        bonus.setDepositId(depositId);              // ???????????????id ,??????null
        bonus.setDepositedAmount(depositAmount);    // ????????????, ??????null
        bonus.setStatus(Constants.EVNumber.two);    // ?????????2 ?????????
        bonus.setIsShow(Constants.EVNumber.two);    // 1 ????????????????????? 2?????????????????? 3???????????????

        bonus.setOrderNo(String.valueOf(new SnowFlake().nextId()));             // ???????????????
        bonus.setOrderPrefix(OrderConstants.ACTIVITY_AC);       // ??????????????? AC ????????????
        bonus.setFinancialCode(OrderConstants.ACTIVITY_RESTS);  // ??????code  ?????? ??????RS

        bonus.setTransferAmount(BigDecimal.ZERO);               // ????????????
        bonus.setPrizetype(Constants.EVNumber.zero);            // ???????????? ?????? ?????? ??????0??????  1??????

        bonus.setApplicationTime(getCurrentDate(FORMAT_18_DATE_TIME));  // ????????????
        return bonus;
    }

    public String checkoutAccountMsg(MbrAccount account, Integer scope, Boolean isName,
                                     Boolean isBank, Boolean isMobile, Boolean isMail, Boolean isApp) {
        if (scope == Constants.EVNumber.one) {
            return null;
        }
        if (Boolean.TRUE.equals(isName) && StringUtil.isEmpty(account.getRealName())) {
            return "????????????????????????";
        }
        if (Boolean.TRUE.equals(isMobile) && Constants.EVNumber.zero == account.getIsVerifyMoblie()) {
            return "????????????????????????";
        }
        if (Boolean.TRUE.equals(isMail) && Constants.EVNumber.zero == account.getIsVerifyEmail()) {
            return "??????????????????";
        }
        if (Boolean.TRUE.equals(isApp) && Constants.EVNumber.four != account.getRegisterSource()) {
            return "??????APP??????????????????";
        }
        if (Boolean.TRUE.equals(isBank)) {
            MbrBankcard bankcard = new MbrBankcard();
            bankcard.setAccountId(account.getId());
            bankcard.setAvailable(Constants.Available.enable);
            bankcard.setIsDel(Constants.Available.disable);
            int count = bankcardMapper.selectCount(bankcard);
            if (count == 0) {
                return "?????????????????????";
            }
        }
        return null;
    }

    public PageUtils accountBonusList(Integer accountId, Integer status, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<BonusListDto> bonusListDtos = operateActivityMapper.findAccountBouns(accountId, status, null);
        if (Collections3.isNotEmpty(bonusListDtos)) {
            setBonusListDto(bonusListDtos);
        }
        return BeanUtil.toPagedResult(bonusListDtos);
    }

    public BonusListDto accountBonusOne(Integer accountId, Integer id) {
        List<BonusListDto> bonusListDtos = operateActivityMapper.findAccountBouns(accountId, null, id);
        if (Collections3.isNotEmpty(bonusListDtos)) {
            setBonusListDto(bonusListDtos);
            BonusListDto dto = bonusListDtos.get(0);
            MbrWallet wallet = new MbrWallet();
            wallet.setAccountId(accountId);
            wallet = walletMapper.selectOne(wallet);
            dto.setWalletBalance(wallet.getBalance());
            return dto;
        }
        return null;
    }

    public List<BonusListDto> availableAccountBonusList(Integer accountId) {
        List<BonusListDto> bonusListDtos = operateActivityMapper.findAccountBouns(accountId,
                Constants.EVNumber.three, null);
        if (Collections3.isNotEmpty(bonusListDtos)) {
            setBonusListDto(bonusListDtos);
        }
        return bonusListDtos;
    }


    private void setBonusListDto(List<BonusListDto> bonusListDtos) {
        bonusListDtos.stream().forEach(bs -> {
            OprActRule rule = actRuleMapper.selectByPrimaryKey(bs.getRuleId());
            if (isNull(rule)) {
                return;
            }
            if (preferentialCode.equals(bs.getTmplCode())) {
                JPreferentialDto dto = jsonUtil.fromJson(rule.getRule(), JPreferentialDto.class);
                if (nonNull(dto)) {
                    setMinAmountAndAudit(bs, dto.getRuleScopeDtos(), dto.getAuditCats(), dto.getScope());
                }
            }
            if (depositSentCode.equals(bs.getTmplCode())) {
                JDepositSentDto dto = jsonUtil.fromJson(rule.getRule(), JDepositSentDto.class);
                if (nonNull(dto)) {
                    setMinAmountAndAudit(bs, dto.getRuleScopeDtos(), dto.getAuditCats(), dto.getScope());
                }
            }
            if (registerCode.equals(bs.getTmplCode())) {
                JRegisterDto dto = jsonUtil.fromJson(rule.getRule(), JRegisterDto.class);
                if (nonNull(dto)) {
                    bs.setCatDtoList(setBonusCatDtos(dto.getAuditCats()));
                }
                bs.setDiscountAudit(dto.getRuleDto().getMultipleWater());
                bs.setMinAmount(BigDecimal.ZERO);
            }
        });
    }

    private void setMinAmountAndAudit(BonusListDto bs, List<RuleScopeDto> ruleScopeDtos, List<AuditCat> auditCats, Integer scope) {
        RuleScopeDto ruleScopeDto = getRuleScopeDtos(ruleScopeDtos, bs.getActLevelId(), scope);
        bs.setCatDtoList(setBonusCatDtos(auditCats));
        ActivityRuleDto ruleDto = castMinAmount(ruleScopeDto.getActivityRuleDtos());
        if (nonNull(ruleDto)) {
            bs.setMinAmount(ruleDto.getAmountMin());
            bs.setDiscountAudit(ruleDto.getMultipleWater());
        }
    }

    private ActivityRuleDto castMinAmount(List<ActivityRuleDto> activityRuleDtos) {
        if (Collections3.isNotEmpty(activityRuleDtos)) {
            Collections.sort(activityRuleDtos, Comparator.comparing(ActivityRuleDto::getAmountMin));
            return activityRuleDtos.get(0);
        }
        return null;
    }

    private List<BonusCatDto> setBonusCatDtos(List<AuditCat> auditCats) {
        List<BonusCatDto> catDtoList = Lists.newArrayList();
        auditCats.stream().forEach(as -> {
            if (Collections3.isNotEmpty(as.getDepots())) {
                BonusCatDto bonusCatDto = new BonusCatDto();
                TGmCat tGmCat = gmCatMapper.selectByPrimaryKey(as.getCatId());
                bonusCatDto.setCatId(as.getCatId());
                bonusCatDto.setCatName(tGmCat.getCatName());
                if (Collections3.isNotEmpty(as.getDepots())) {
                    List<BonusDepotDto> bonusDepotDtos = Lists.newArrayList();
                    as.getDepots().stream().forEach(ds -> {
                        TGmDepot depot = gmDepotMapper.selectByPrimaryKey(ds.getDepotId());
                        TGmApiprefix tGmApiprefix = gameMapper.findAvailable(ds.getDepotId(), CommonUtil.getSiteCode());
                        BonusDepotDto depotDto = new BonusDepotDto();
                        depotDto.setDepotId(ds.getDepotId());
                        depotDto.setDepotName(depot.getDepotName());
                        depotDto.setAvailableWh(tGmApiprefix.getAvailable());
                        bonusDepotDtos.add(depotDto);
                    });
                    bonusCatDto.setDepotDtos(bonusDepotDtos);
                }
                catDtoList.add(bonusCatDto);
            }
        });
        return catDtoList;
    }


    public RuleScopeDto getRuleScopeDtos(List<RuleScopeDto> ruleScopeDtos, Integer actLevelId, Integer scope) {
        if (Collections3.isEmpty(ruleScopeDtos)) {
            return null;
        }
        if (scope == Constants.EVNumber.zero) {
            return ruleScopeDtos.get(0);
        }
        return ruleScopeDtos.stream()
                .filter(rs -> rs.getActLevelId() == actLevelId)
                .findFirst().orElse(null);
    }

    public OprActActivity getRebateAct() {
        OprActActivity actActivity = new OprActActivity();
        actActivity.setTmplCode(mbrRebateCode);
        actActivity.setUseStart(getPastDate(Constants.EVNumber.zero, FORMAT_10_DATE));
        actActivity.setIsdel(Constants.EVNumber.zero);
        List<OprActActivity> oprActActivities = operateActivityMapper.findWaterActivity(actActivity);
        if (CollectionUtils.isNotEmpty(oprActActivities)) {
            return oprActActivities.get(0);
        }
        return null;
    }


    public OprActActivity getHupengRebateAct() {
        OprActActivity actActivity = new OprActActivity();
        actActivity.setTmplCode(mbrRebateHuPengCode);
        actActivity.setUseStart(getPastDate(Constants.EVNumber.zero, FORMAT_10_DATE));
        actActivity.setIsdel(Constants.EVNumber.zero);
        List<OprActActivity> oprActActivities = operateActivityMapper.findWaterActivity(actActivity);
        if (CollectionUtils.isNotEmpty(oprActActivities)) {
            return oprActActivities.get(0);
        }
        return null;
    }


    public OprActActivity getActActivity(String tmplCode, Integer accountId) {
        OprActActivity actActivity = new OprActActivity();
        actActivity.setTmplCode(tmplCode);
        actActivity.setUseStart(getPastDate(Constants.EVNumber.zero, FORMAT_10_DATE));
        actActivity.setIsdel(Constants.EVNumber.zero);
        List<OprActActivity> oprActActivities = operateActivityMapper.findWaterActivity(actActivity);
        if (CollectionUtils.isNotEmpty(oprActActivities)) {
            OprActActivity at =  oprActActivities.get(0);

            if (!StringUtils.isEmpty(at.getMbLogoUrl())) {
                at.setMbLogoUrl(tGmApiService.queryGiniuyunUrl() + at.getMbLogoUrl());
            }
            if (!StringUtils.isEmpty(at.getPcLogoUrl())) {
                at.setPcLogoUrl(tGmApiService.queryGiniuyunUrl() + at.getPcLogoUrl());
            }
            if (nonNull(accountId)) {  // ????????????
                if (nonNull(at.getRuleId())) {
                    // ?????????????????????????????????????????????????????? ??????1/4/null(??????)   4???????????????????????? 1????????????
                    Integer isPass =  applyfirstDepositService.checkoutApplyFirstDeposit(at, accountId);
                    at.setButtonShow(nonNull(isPass) ? isPass.byteValue() : at.getButtonShow());
                }
                if (at.getUseState() == Constants.EVNumber.two) {   // ????????????
                    at.setButtonShow((byte) Constants.EVNumber.zero);
                }
            } else {    // ???????????????
                if (at.getUseState() == Constants.EVNumber.two) {   // ????????????
                    at.setButtonShow((byte) Constants.EVNumber.zero);
                }
            }
            return at;
        }
        return null;
    }
}

