package com.wsdy.saasops.modules.member.service;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.member.dao.MbrBankcardHistoryMapper;
import com.wsdy.saasops.modules.member.dao.MbrBankcardMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import com.wsdy.saasops.modules.member.entity.MbrBankcardHistory;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.modules.sys.service.SysWarningService;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Service
public class MbrBankcardService extends BaseService<MbrBankcardMapper, MbrBankcard> {
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    MbrAccountService mbrAccountService;
    @Autowired
    private MbrGroupService mbrGroupService;
    @Autowired
	private MbrAccountLogService accountLogService;
	@Autowired
	private MbrAccountLogService mbrAccountLogService;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
    private MbrBankcardMapper mbrBankcardMapper;
	@Autowired
	private MbrBankcardHistoryMapper mbrBankcardHistoryMapper;
	@Autowired
	private SysSettingService sysSettingService;
	@Autowired
	private SysWarningService sysWarningService;


    public PageUtils queryListPage(MbrBankcard mbrBankcard, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<MbrBankcard> list = queryListCond(mbrBankcard);
		List<MbrBankcard> bankOrAlipayList;
		if (Objects.nonNull(mbrBankcard.getBankType()) && mbrBankcard.getBankType() == Constants.EVNumber.two) {
			// ???????????????
			bankOrAlipayList = list.stream().filter(e -> e.getBankName().equals("?????????")).collect(Collectors.toList());
			bankOrAlipayList.forEach(p -> p.setBindTimePhoneNum(StringUtil.phone(p.getBindTimePhoneNum())));
		} else if (Objects.nonNull(mbrBankcard.getBankType()) && mbrBankcard.getBankType() == Constants.EVNumber.three) {
			// ??????????????????
			bankOrAlipayList = list.stream().filter(e -> e.getAddress().equals("????????????")).collect(Collectors.toList());
		} else {
			// ???????????????
			bankOrAlipayList = list.stream().filter(e -> !e.getBankName().equals("?????????")).collect(Collectors.toList());
		}
        return BeanUtil.toPagedResult(bankOrAlipayList);
    }

	public Boolean checkAccountBank(MbrBankcard mbrBankcard) {
		MbrBankcard oldBankcard = mbrMapper.findAccountFirstBankcard(mbrBankcard.getAccountId());
		if(null!=oldBankcard){
			if (oldBankcard.getRealName().equals(mbrBankcard.getRealName()) && oldBankcard.getCardNo().equals(mbrBankcard.getCardNo())){
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		} else {
			return Boolean.TRUE;
		}
	}

    public MbrBankcard findMemberCardOne(Integer id) {
        return mbrMapper.findBankCardOne(id);
    }

    public void deleteBatch(Integer[] idArr, String userName, int operatorType, String ip) {
		for (Integer id:idArr) {
			AccWithdraw accWithdraw = new AccWithdraw();
			accWithdraw.setBankCardId(id);
			List<AccWithdraw> accWithdrawList = mbrMapper.queryAccWithdrawList(accWithdraw);
			MbrBankcard mbrBankcard = mbrMapper.selectBankInfoById(id);
			MbrBankcardHistory mbrBankcardHistory = mbrBankcardHistoryMapper.selectByPrimaryKey(id);
			//???????????????????????????

			if(Collections3.isEmpty(accWithdrawList)){
				if(Objects.nonNull(mbrBankcard)){
					mbrBankcardMapper.deleteByPrimaryKey(mbrBankcard.getId());
				}
			}else{
				mbrBankcard.setIsDel(Available.enable);

				mbrBankcardMapper.updateByPrimaryKeySelective(mbrBankcard);
			}
			//??????????????????????????????????????????????????????????????????
			if (Objects.nonNull(mbrBankcard)){
				mbrBankcardHistory = getMbrBankcardHistoryVO(mbrBankcardHistory,mbrBankcard);
				mbrBankcardHistory.setIsUse(Available.enable);
				mbrBankcardHistory.setUpdater(userName);
				mbrBankcardHistory.setIsDel(Available.enable);
				mbrBankcardHistoryMapper.updateByPrimaryKeySelective(mbrBankcardHistory);
			}



			mbrAccountLogService.deleteAccountBank(mbrBankcard, userName, operatorType, ip);
		}
    }

    public int countBankNo(Integer accountId) {
        MbrBankcard mbrBankcard = new MbrBankcard();
        mbrBankcard.setAccountId(accountId);
        mbrBankcard.setIsDel(Available.disable);
        mbrBankcard.setAvailable(Available.enable);
        return super.selectCount(mbrBankcard);
    }

	public R unbindCondBankCard(Integer accountId, Integer bankCardId) {
		AccWithdraw accWithdraw = new AccWithdraw();
		accWithdraw.setBankCardId(bankCardId);
		List<AccWithdraw> accWithdrawList = mbrMapper.queryAccWithdrawList(accWithdraw);

		MbrAccount mbrAccount = mbrAccountService.getAccountInfo(accountId);
		MbrBankcard mbrBankcard = mbrBankcardMapper.selectByPrimaryKey(bankCardId);
		MbrBankcardHistory mbrBankcardHistory = mbrBankcardHistoryMapper.selectByPrimaryKey(bankCardId);
		//???????????????????????????
		if (Objects.nonNull(mbrBankcard)) {
			if (Collections3.isEmpty(accWithdrawList)) {
				int result = mbrBankcardMapper.deleteByPrimaryKey(mbrBankcard.getId());

				if (Objects.nonNull(mbrBankcardHistory)){
					mbrBankcardHistory = getMbrBankcardHistoryVO(mbrBankcardHistory,mbrBankcard);
					mbrBankcardHistory.setIsUse(Available.enable);
					mbrBankcardHistory.setUpdater("????????????");
					mbrBankcardHistoryMapper.updateByPrimaryKeySelective(mbrBankcardHistory);
				}

				applicationEventPublisher.publishEvent(new BizEvent(this,
						CommonUtil.getSiteCode(), accountId, mbrBankcard.getCardNo(), BizEventType.ACCOUNT_UNBIND_BANKCARD));
				return R.ok().put("status", result);
			}
		}
		return R.ok().put("status", 0);
	}

	public List<MbrBankcard> listCondBankCard(Integer accountId) {
		MbrBankcard mbrBankcard = new MbrBankcard();
		mbrBankcard.setAccountId(accountId);
		mbrBankcard.setIsDel(Available.disable);
		mbrBankcard.setAvailable(Available.enable);
		List<MbrBankcard> bankcards = mbrMapper.userBankCard(mbrBankcard);
		bankcards = bankcards.stream().filter(p -> (!"?????????".equals(p.getBankName()) && !"????????????".equals(p.getAddress()))).collect(Collectors.toList());
		bankcards.forEach(e -> {
			e.setCardNo(StringUtil.bankNoEx(e.getCardNo()));
		});
		return bankcards;
	}

	public Integer bankDifferentName(Integer accountId) {
    	SysSetting enable = sysSettingService.getSysSetting(SystemConstants.BANK_DIFFERENT_NAME_ENABLE);
    	if (Integer.parseInt(enable.getSysvalue()) == Constants.EVNumber.zero) {
    		return Constants.EVNumber.zero;
		}

		MbrAccount account = mbrAccountService.getAccountInfo(accountId);
		MbrGroup group = mbrGroupService.queryObject(account.getGroupId());
		if (group.getBankDifferentName() != null && group.getBankDifferentName() == Constants.EVNumber.one) {
			return Constants.EVNumber.one;
		}
		return Constants.EVNumber.zero;
	}

	public List<MbrBankcard> listCondAlipayAccount(Integer accountId) {
		MbrBankcard mbrBankcard = new MbrBankcard();
		mbrBankcard.setAccountId(accountId);
		mbrBankcard.setIsDel(Available.disable);
		mbrBankcard.setAvailable(Available.enable);
		mbrBankcard.setBankName("?????????");
		List<MbrBankcard> bankcards = mbrMapper.userBankCard(mbrBankcard);
		bankcards.forEach(e -> {
			e.setCardNo(StringUtil.bankNoEx(e.getCardNo()));
		});
		return bankcards;
	}

	public List<MbrBankcard> listCondOtherPayAccount(Integer accountId) {
		MbrBankcard mbrBankcard = new MbrBankcard();
		mbrBankcard.setAccountId(accountId);
		mbrBankcard.setIsDel(Available.disable);
		mbrBankcard.setAvailable(Available.enable);
		mbrBankcard.setAddress("????????????");
		List<MbrBankcard> bankcards = mbrMapper.userBankCard(mbrBankcard);
		bankcards.forEach(e -> {
			e.setCardNo(StringUtil.bankNoEx(e.getCardNo()));
		});
		return bankcards;
	}

	public R saveBankCard(MbrBankcard mbrBankcard, Integer operatorType, String userName, String ip)
	{
		mbrBankcard.setId(null);
		MbrAccount mbrAccount = mbrAccountService.getAccountInfo(mbrBankcard.getAccountId());

		// ????????????????????????
		SysSetting bankDifferentName = sysSettingService.getSysSetting(SystemConstants.BANK_DIFFERENT_NAME_ENABLE);
		if (Integer.valueOf(bankDifferentName.getSysvalue()) == Constants.EVNumber.one &&
				!"?????????".equals(mbrBankcard.getBankName())) {
			SysSetting bankDifferentNumber = sysSettingService.getSysSetting(SystemConstants.BANK_DIFFERENT_NAME_NUMBER);
			if (Objects.nonNull(bankDifferentName.getSysvalue())) {
				List<String> existNumber = mbrMapper.selectBankDifferentNumber(mbrAccount.getId());
				if (existNumber.size() >= Integer.valueOf(bankDifferentNumber.getSysvalue()) &&
						!existNumber.contains(mbrBankcard.getRealName())) {
					return R.error("?????????????????????????????????!");
				}
			}
		} else {
			mbrBankcard.setRealName(mbrAccount.getRealName());
		}

		if (!"????????????".equals(mbrBankcard.getAddress())) {
			Assert.isBlank(mbrBankcard.getRealName(), "????????????????????????!");
		}
/*		if (type.equals(Constants.sourceType.web))
			Assert.isBlank(mbrAccount.getMobile(), "????????????????????????,?????????????????????!");*/
		mbrBankcard.setBindTimePhoneNum(mbrAccount.getMobile());
		mbrBankcard.setIsDel(Available.disable);

		mbrBankcard.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
		MbrBankcard card = null;
		MbrBankcardHistory mbrBankcardHistory = new MbrBankcardHistory();
		if (StringUtils.isEmpty(mbrBankcard.getAvailable())) {
			mbrBankcard.setAvailable(Available.enable);
		}

		MbrBankcard tempCard = mbrMapper.selectBankInfoByCard(mbrBankcard.getCardNo());
		//card.setIsDel(Available.disable);
		if (Objects.isNull(tempCard)) {
			card = new MbrBankcard();
			card.setAccountId(mbrBankcard.getAccountId());
			card.setIsDel(Available.disable);
			if ("?????????".equals(mbrBankcard.getBankName())) {
				// ??????????????????????????????????????????5???
				card.setBankName(mbrBankcard.getBankName());
			}
			//Integer alreadyBind = super.selectCount(card);
			List<MbrBankcard> count = super.queryListCond(card);
			//mbrBankcardMapper
			if (!"?????????".equals(mbrBankcard.getBankName())) {
				// ????????????????????????????????????
				count = count.stream().filter(p -> (!p.getBankName().equals("?????????"))).collect(Collectors.toList());
			}
			if (count.size() < 5) {
				card = new MbrBankcard();
				card.setBankName(mbrBankcard.getBankName());
				if ("?????????".equals(mbrBankcard.getBankName())) {
					// ?????????????????????????????????????????????????????????????????????
					card.setCardNo(mbrBankcard.getCardNo());
				}
				card.setAccountId(mbrBankcard.getAccountId());
				card.setIsDel(Available.disable);
				if (super.selectCount(card) == 0) {
					mbrBankcardHistory = getMbrBankcardHistoryVO(mbrBankcardHistory,mbrBankcard);
					super.save(mbrBankcard);
					mbrBankcardHistory.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
					mbrBankcardHistoryMapper.insert(mbrBankcardHistory); //??????????????????
					if(operatorType==2){
						accountLogService.addAccountBank(mbrBankcard, mbrAccount, userName, operatorType, ip);
					}
					if(operatorType==1){
						applicationEventPublisher.publishEvent(new BizEvent(this,
								CommonUtil.getSiteCode(), mbrBankcard.getAccountId(), mbrBankcard.getCardNo(), BizEventType.ACCOUNT_BIND_BANKCARD));
					}

					String name = "?????????".equals(mbrBankcard.getBankName()) ? "?????????": "?????????";
					sysWarningService.bindWarning(mbrAccount,name+":" + mbrBankcard.getCardNo());
					return R.ok();
				} else {
					return R.error("?????????????????????1????????????!");
				}
			} else {
				return R.error("??????????????????????????????5???"+ ("?????????".equals(mbrBankcard.getBankName())?"?????????":"?????????") +"!");
			}
		} else {
			if(!(tempCard.getAccountId().equals(mbrBankcard.getAccountId()) && 1==tempCard.getIsDel())){
				if ("?????????".equals(mbrBankcard.getBankName())) {
					return R.error("???????????????????????????????????????????????????!");
				}
				return R.error("????????????????????????????????????????????????!");
			}
			card = new MbrBankcard();
			card.setBankName(tempCard.getBankName());
			card.setAccountId(tempCard.getAccountId());
			card.setIsDel(Available.disable);
			if ("?????????".equals(mbrBankcard.getBankName())) {
				// ?????????????????????????????????????????????????????????????????????
				card.setCardNo(mbrBankcard.getCardNo());
			}
			if (super.selectCount(card) > 0) {
				return R.error("?????????????????????1????????????!");
			}
			tempCard.setIsDel(Available.disable);
			// ????????????????????????
			tempCard.setBankCardId(mbrBankcard.getBankCardId());
			tempCard.setBankName(mbrBankcard.getBankName());
			tempCard.setProvince(mbrBankcard.getProvince());
			tempCard.setAddress(mbrBankcard.getAddress());
			tempCard.setCity(mbrBankcard.getCity());
			tempCard.setRealName(mbrBankcard.getRealName());
			tempCard.setAvailable(mbrBankcard.getAvailable());
			tempCard.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
			super.update(tempCard);
			return R.ok();
		}
	}

	public R updateBankCard(MbrBankcard mbrBankcard, String userName, String ip)
	{
		mbrBankcard.setIsDel(null);
		MbrAccount mbrAccount = mbrAccountService.getAccountInfo(mbrBankcard.getAccountId());
		mbrBankcard.setRealName(mbrAccount.getRealName());
		Assert.isBlank(mbrBankcard.getRealName(), "????????????????????????!");
		//Assert.isBlank(mbrAccount.getMobile(),"????????????????????????,?????????????????????!");
		if (StringUtils.isEmpty(mbrBankcard.getAvailable())) {
            mbrBankcard.setAvailable(Available.enable);
        }
		MbrBankcard card = new MbrBankcard();
		card.setCardNo(mbrBankcard.getCardNo());
		card.setIsDel(Available.disable);
		card.setId(mbrBankcard.getId());
		if (mbrMapper.countSameBankNum(card)==0) {
				card = new MbrBankcard();
				card.setBankName(mbrBankcard.getBankName());
				card.setIsDel(Available.disable);
				card.setAccountId(mbrBankcard.getAccountId());
				card.setId(mbrBankcard.getId());
				if (mbrMapper.countSameBankNum(card) == 0) {
					super.update(mbrBankcard);
					accountLogService.updateAccountBank(mbrBankcard, userName, Constants.EVNumber.two, ip);
					return R.ok();
				} else {
					return R.error("?????????????????????1????????????!");
				}
		} else {
			return R.error("???????????????????????????!");
		}
	}
	
	public void updateBankCardNameByAccId(int accountId, String realName) {
		mbrMapper.updateBankCardNameByAccId(accountId, realName);
	}

	public PageUtils queryBankHistoryListPage(MbrBankcardHistory mbrBankcardHistory, Integer pageNo, Integer pageSize, String orderBy) {
		PageHelper.startPage(pageNo, pageSize);
		if (!StringUtils.isEmpty(orderBy)) {
			PageHelper.orderBy(orderBy);
		}
		List<MbrBankcardHistory> list = mbrMapper.queryBankHistoryListPage(mbrBankcardHistory.getAccountId());
		return BeanUtil.toPagedResult(list);
	}

	public MbrBankcardHistory getMbrBankcardHistoryVO(MbrBankcardHistory mbrBankcardHistory,MbrBankcard mbrBankcard){

		mbrBankcardHistory.setAccountId(mbrBankcard.getAccountId());
		mbrBankcardHistory.setBankCardId(mbrBankcard.getBankCardId());
		mbrBankcardHistory.setCardNo(mbrBankcard.getCardNo());
		mbrBankcardHistory.setProvince(mbrBankcard.getProvince());
		mbrBankcardHistory.setCity(mbrBankcard.getCity());
		mbrBankcardHistory.setAddress(mbrBankcard.getAddress());
		mbrBankcardHistory.setRealName(mbrBankcard.getRealName());
		mbrBankcardHistory.setIsDel(Available.disable);
		mbrBankcardHistory.setBankName(mbrBankcard.getBankName());
		mbrBankcardHistory.setIsUse(Available.disable);
		mbrBankcardHistory.setAvailable(mbrBankcard.getAvailable());
    	return mbrBankcardHistory;
	}
}
