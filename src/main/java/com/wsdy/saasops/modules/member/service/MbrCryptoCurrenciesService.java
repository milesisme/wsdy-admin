package com.wsdy.saasops.modules.member.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.fund.dao.FundMerchantScopeMapper;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.fund.entity.FundMerchantScope;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrCryptoCurrenciesMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrCryptoCurrencies;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.sys.service.SysWarningService;
import com.wsdy.saasops.modules.system.pay.dao.SetBasicSysCryptoCurrenciesMapper;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicSysCryptoCurrencies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Service
public class MbrCryptoCurrenciesService extends BaseService<MbrCryptoCurrenciesMapper, MbrCryptoCurrencies> {
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    MbrAccountService mbrAccountService;
	@Autowired
	private MbrAccountLogService mbrAccountLogService;
	@Autowired
    private MbrCryptoCurrenciesMapper mbrCryptoCurrenciesMapper;
	@Autowired
	private SetBasicSysCryptoCurrenciesMapper setBasicSysCryptoCurrenciesMapper;
	@Autowired
	private MbrAccountMapper accountMapper;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private FundMapper fundMapper;
	@Autowired
	private FundMerchantScopeMapper merchantScopeMapper;
	@Autowired
	private SysWarningService sysWarningService;


    public PageUtils queryListPage(MbrCryptoCurrencies mbrCryptoCurrencies, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<MbrCryptoCurrencies> list = queryListCond(mbrCryptoCurrencies);
        return BeanUtil.toPagedResult(list);
    }

//	public Boolean checkAccountBank(MbrCryptoCurrencies mbrCryptoCurrencies) {
//		MbrCryptoCurrencies oldBankcard = mbrMapper.findAccountFirstBankcard(mbrCryptoCurrencies.getAccountId());
//		if(null!=oldBankcard){
//			if (oldBankcard.getRealName().equals(mbrCryptoCurrencies.getRealName()) && oldBankcard.getCardNo().equals(mbrCryptoCurrencies.getCardNo())){
//				return Boolean.TRUE;
//			} else {
//				return Boolean.FALSE;
//			}
//		} else {
//			return Boolean.TRUE;
//		}
//	}
//
//    public MbrCryptoCurrencies findMemberCardOne(Integer id) {
//        return mbrMapper.findCryptoCurrenciesOne(id);
//    }
//
    public void deleteBatch(Integer[] idArr, String userName, int operatorType, String ip) {
		for (Integer id:idArr) {
			AccWithdraw accWithdraw = new AccWithdraw();
			accWithdraw.setCryptoCurrenciesId(id);
			List<AccWithdraw> accWithdrawList = mbrMapper.queryAccWithdrawListByCryptoCurrencies(accWithdraw);
			MbrCryptoCurrencies mbrCryptoCurrencies = mbrMapper.selectCryptoCurrenciesInfoById(id);
			//???????????????????????????
			if(Collections3.isEmpty(accWithdrawList)){	// ???????????????????????????????????????
				if(Objects.nonNull(mbrCryptoCurrencies)){
					mbrCryptoCurrenciesMapper.deleteByPrimaryKey(mbrCryptoCurrencies.getId());
				}
			}else{	// ????????????????????????????????????
				mbrCryptoCurrencies.setIsDel(Constants.Available.enable);
				mbrCryptoCurrenciesMapper.updateByPrimaryKeySelective(mbrCryptoCurrencies);
			}
			// ????????????
			MbrAccount mbr = accountMapper.selectByPrimaryKey(mbrCryptoCurrencies.getAccountId());
			mbrAccountLogService.deleteAccountCr(mbrCryptoCurrencies,mbr);
		}
    }
//
//    public int countBankNo(Integer accountId) {
//        MbrCryptoCurrencies mbrCryptoCurrencies = new MbrCryptoCurrencies();
//        mbrCryptoCurrencies.setAccountId(accountId);
//        mbrCryptoCurrencies.setIsDel(Available.disable);
//        mbrCryptoCurrencies.setAvailable(Available.enable);
//        return super.selectCount(mbrCryptoCurrencies);
//    }
//
//	public R unbindCondCryptoCurrencies(Integer accountId, Integer bankCardId) {
//		AccWithdraw accWithdraw = new AccWithdraw();
//		accWithdraw.setCryptoCurrenciesId(bankCardId);
//		List<AccWithdraw> accWithdrawList = mbrMapper.queryAccWithdrawList(accWithdraw);
//
//		MbrCryptoCurrencies mbrCryptoCurrencies = mbrCryptoCurrenciesMapper.selectByPrimaryKey(bankCardId);
//		//???????????????????????????
//		if (Objects.nonNull(mbrCryptoCurrencies)) {
//			if (Collections3.isEmpty(accWithdrawList)) {
//				int result = mbrCryptoCurrenciesMapper.deleteByPrimaryKey(mbrCryptoCurrencies.getId());
//				applicationEventPublisher.publishEvent(new BizEvent(this,
//						CommonUtil.getSiteCode(), accountId, mbrCryptoCurrencies.getCardNo(), BizEventType.ACCOUNT_UNBIND_BANKCARD));
//				return R.ok().put("status", result);
//			}
//		}
//		return R.ok().put("status", 0);
//	}

	public List<MbrCryptoCurrencies> listCondCryptoCurrencies(Integer accountId) {
		MbrAccount mbrAccount = mbrAccountService.getAccountInfo(accountId);

		MbrCryptoCurrencies mbrCryptoCurrencies = new MbrCryptoCurrencies();
		mbrCryptoCurrencies.setAccountId(accountId);
		mbrCryptoCurrencies.setIsDel(Constants.Available.disable);
		mbrCryptoCurrencies.setAvailable(Constants.Available.enable);
		List<MbrCryptoCurrencies> bankcards = mbrMapper.userCryptoCurrencies(mbrCryptoCurrencies);


		//???????????????????????????????????????
		FundMerchantPay merchantPay = new FundMerchantPay();
		//merchantPay.setAvailable(Constants.EVNumber.zero);
		merchantPay.setMethodType(Constants.EVNumber.one);
//		List<Integer> availables = new ArrayList<>();
//		availables.add(Constants.EVNumber.zero);
//		merchantPay.setAvailables(availables);
		List<FundMerchantPay> merchantPayList = fundMapper.findFundMerchantPayList(merchantPay);

		if (null!=merchantPayList&&merchantPayList.size()>0&&bankcards!=null&&bankcards.size()>0){
			for (int i=0;i<merchantPayList.size();i++){
				FundMerchantScope scope = new FundMerchantScope();
				scope.setMerchantId(merchantPayList.get(i).getId());
				List<FundMerchantScope> scopeList =  merchantScopeMapper.select(scope);
				List<Integer> groupList = new ArrayList<>();
				for (int a =0;a<scopeList.size();a++){
					groupList.add(scopeList.get(a).getGroupId());
				}
				for (int b =0 ;b<bankcards.size();b++){
					//???????????????????????????????????????  ????????????????????????????????????????????????
					if(merchantPayList.get(i).getAvailable()==Constants.EVNumber.zero){
						if (merchantPayList.get(i).getCurrencyProtocol().equals(bankcards.get(b).getCurrencyProtocol())){
							bankcards.get(b).setAvailable(Constants.Available.disableTwo);
						}
					}else {

						//???????????????????????????
						if (mbrAccount.getGroupId()!=null&&!groupList.contains(mbrAccount.getGroupId())){
							if (merchantPayList.get(i).getCurrencyProtocol().equals(bankcards.get(b).getCurrencyProtocol())){
								bankcards.get(b).setAvailable(Constants.Available.disableThr);
							}

						}
					}

				}
			}
		}
		// ????????????
		bankcards.forEach(e -> {
			e.setWalletAddress(StringUtil.walletAddress(e.getWalletAddress()));
		});
		return bankcards;
	}

	public R saveCryptoCurrencies(MbrCryptoCurrencies mbrCryptoCurrencies, Integer operatorType){
		// ???????????????????????????????????????
		MbrCryptoCurrencies mbr = new MbrCryptoCurrencies();
		mbr.setAccountId(mbrCryptoCurrencies.getAccountId());
		mbr.setIsDel(Constants.Available.disable);
		if(super.selectCount(mbr) >= 15){
			mbr.setIsDel(null);
			if(super.selectCount(mbr) > 30){
				return R.error("?????????????????????????????????????????????");
			}
			return R.error("??????????????????15????????????");
		}

		MbrCryptoCurrencies tempCurrencies = mbrMapper.selectCryptoCurrenciesByAddress(mbrCryptoCurrencies.getWalletAddress());

		if (Objects.isNull(tempCurrencies)) {	// ???????????????, ????????????
			MbrCryptoCurrencies currencies = new MbrCryptoCurrencies();
			currencies.setAccountId(mbrCryptoCurrencies.getAccountId());
			currencies.setIsDel(Constants.Available.disable);
			currencies.setWalletAddress(mbrCryptoCurrencies.getWalletAddress());
			currencies.setWalletName(mbrCryptoCurrencies.getWalletName());
			currencies.setCurrencyCode(mbrCryptoCurrencies.getCurrencyCode());
			currencies.setCurrencyProtocol(mbrCryptoCurrencies.getCurrencyProtocol());
			currencies.setWalletId(mbrCryptoCurrencies.getWalletId());
			currencies.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
			currencies.setAvailable(Integer.valueOf(Constants.EVNumber.one).byteValue());
			SetBasicSysCryptoCurrencies tmp = setBasicSysCryptoCurrenciesMapper.getCrByCodeAndProtocol(currencies.getCurrencyCode(),currencies.getCurrencyProtocol());
			if (Objects.isNull(tmp)) {
				tmp = setBasicSysCryptoCurrenciesMapper.getCrByCodeAndProtocolFromManage(currencies.getCurrencyCode(), currencies.getCurrencyProtocol());
			}
			currencies.setBankCardId(tmp.getBankCardId());
			super.save(currencies);
			MbrAccount mbrAccount = mbrAccountService.getAccountInfo(mbrCryptoCurrencies.getAccountId());
			// ???????????????????????????
			if(operatorType==2){
//				accountLogService.addAccountBank(mbrCryptoCurrencies, mbrAccount, userName, operatorType, ip);
			}
			// ???????????????????????????
			if(operatorType==1){
				applicationEventPublisher.publishEvent(new BizEvent(this,
						CommonUtil.getSiteCode(), mbrCryptoCurrencies.getAccountId(), mbrCryptoCurrencies.getWalletAddress(), BizEventType.ACCOUNT_BIND_CR));
			}
			sysWarningService.bindWarning(mbrAccount,"??????:" + mbrCryptoCurrencies.getWalletAddress());
			return R.ok();
		} else {	// ??????????????????????????????
			if(!(tempCurrencies.getAccountId().equals(mbrCryptoCurrencies.getAccountId()) && 1==tempCurrencies.getIsDel())){
				return R.error("????????????????????????????????????????????????!");
			}

			// ??????????????????????????????????????????
			tempCurrencies.setIsDel(Constants.Available.disable);
			tempCurrencies.setBankCardId(mbrCryptoCurrencies.getBankCardId());
			tempCurrencies.setWalletName(mbrCryptoCurrencies.getWalletName());
			tempCurrencies.setCurrencyProtocol(mbrCryptoCurrencies.getCurrencyProtocol());
			tempCurrencies.setCurrencyCode(mbrCryptoCurrencies.getCurrencyCode());
			tempCurrencies.setAvailable(mbrCryptoCurrencies.getAvailable());
			tempCurrencies.setWalletId(mbrCryptoCurrencies.getWalletId());
			tempCurrencies.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
			super.update(tempCurrencies);
			return R.ok();
		}
	}

	public R updateCryptoCurrencies(MbrCryptoCurrencies mbrCryptoCurrencies, String userName, String ip)
	{
//		if (StringUtils.isEmpty(mbrCryptoCurrencies.getAvailable())) {
//            mbrCryptoCurrencies.setAvailable(Constants.Available.enable);
//        }
		MbrCryptoCurrencies currencies = new MbrCryptoCurrencies();
		currencies.setWalletAddress(mbrCryptoCurrencies.getWalletAddress());
		currencies.setIsDel(Constants.Available.disable);
		currencies.setId(mbrCryptoCurrencies.getId());
		if (mbrMapper.countSameCryptoCurrenciesNum(currencies)==0) {
			currencies = new MbrCryptoCurrencies();
			currencies.setWalletName(mbrCryptoCurrencies.getWalletName());
			currencies.setCurrencyProtocol(mbrCryptoCurrencies.getCurrencyProtocol());
			currencies.setCurrencyCode(mbrCryptoCurrencies.getCurrencyCode());
			currencies.setIsDel(Constants.Available.disable);
			currencies.setAccountId(mbrCryptoCurrencies.getAccountId());
			currencies.setId(mbrCryptoCurrencies.getId());
			super.update(mbrCryptoCurrencies);
			// ???????????? TODO
//			accountLogService.updateAccountBank(mbrCryptoCurrencies, userName, Constants.EVNumber.two, ip);
			return R.ok();
		} else {
			return R.error("?????????????????????!");
		}
	}

//	public void updateCryptoCurrenciesNameByAccId(int accountId, String realName) {
//		mbrMapper.updateCryptoCurrenciesNameByAccId(accountId, realName);


	public Integer qryBankAndWalletSumById(Integer id){
    	return setBasicSysCryptoCurrenciesMapper.qryBankAndWalletSumById(id);
	}


	public R availableCryptoCurrencies(MbrCryptoCurrencies mbrCryptoCurrenciesDto){
		MbrCryptoCurrencies old = mbrCryptoCurrenciesMapper.selectByPrimaryKey(mbrCryptoCurrenciesDto.getId());
		MbrAccount mbr = accountMapper.selectByPrimaryKey(old.getAccountId());
		MbrCryptoCurrencies mbrCryptoCurrencies = new MbrCryptoCurrencies();
		mbrCryptoCurrencies.setId(mbrCryptoCurrenciesDto.getId());
		mbrCryptoCurrencies.setAvailable(mbrCryptoCurrenciesDto.getAvailable());
		update(mbrCryptoCurrencies);

		// ????????????
		mbrAccountLogService.updateAccountCryptoCurrenciesStatus(mbrCryptoCurrencies, old, mbr);
		return R.ok();
	}

	public R unbindWalletList(Integer accountId, Integer walletId) {
		AccWithdraw accWithdraw = new AccWithdraw();
		accWithdraw.setCryptoCurrenciesId(walletId);
		List<AccWithdraw> accWithdrawList = mbrMapper.queryAccWithdrawList(accWithdraw);

		MbrCryptoCurrencies mbrCryptoCurrencies = mbrCryptoCurrenciesMapper.selectByPrimaryKey(walletId);
		//???????????????????????????
		if (Objects.nonNull(mbrCryptoCurrencies)) {
			if (Collections3.isEmpty(accWithdrawList)) {
				int result = mbrCryptoCurrenciesMapper.deleteByPrimaryKey(mbrCryptoCurrencies.getId());
				applicationEventPublisher.publishEvent(new BizEvent(this,
						CommonUtil.getSiteCode(), accountId, mbrCryptoCurrencies.getWalletAddress(), BizEventType.ACCOUNT_UNBIND_CR));
				return R.ok().put("status", result);
			}
		}
		return R.ok().put("status", 0);
	}
}
