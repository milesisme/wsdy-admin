package com.wsdy.saasops.modules.agent.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wsdy.saasops.agapi.modules.mapper.AgapiMapper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.agent.dao.AgentCryptoCurrenciesMapper;
import com.wsdy.saasops.modules.agent.dao.AgyWithdrawMapper;
import com.wsdy.saasops.modules.agent.entity.AgentCryptoCurrencies;
import com.wsdy.saasops.modules.agent.entity.AgyWithdraw;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.fund.dao.FundMerchantPayMapper;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.system.pay.dao.SetBasicSysCryptoCurrenciesMapper;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicSysCryptoCurrencies;

import cn.hutool.core.collection.CollectionUtil;

@Service
public class AgentCryptoCurrenciesService extends BaseService<AgentCryptoCurrenciesMapper, AgentCryptoCurrencies> {

    @Autowired
    private SetBasicSysCryptoCurrenciesMapper setBasicSysCryptoCurrenciesMapper;
    @Autowired
    private AgapiMapper agapiMapper;
    @Autowired
    private FundMerchantPayMapper fundMerchantPayMapper;
    @Autowired
    private AgentCryptoCurrenciesMapper cryptoCurrenciesMapper;
    @Autowired
    private AgyWithdrawMapper agyWithdrawMapper;


    public R saveCryptoCurrencies(AgentCryptoCurrencies mbrCryptoCurrencies) {
        AgentCryptoCurrencies mbr = new AgentCryptoCurrencies();
        mbr.setAccountId(mbrCryptoCurrencies.getAccountId());
        mbr.setIsDel(Constants.Available.disable);
        if (super.selectCount(mbr) >= 3 && isNull(mbrCryptoCurrencies.getId())) {
            mbr.setIsDel(null);
            if (super.selectCount(mbr) > 30) {
                return R.error("?????????????????????????????????????????????");
            }
            return R.error("??????????????????3????????????");
        }

        if (nonNull(mbrCryptoCurrencies.getId())) {
            AgentCryptoCurrencies cryptoCurrencies = cryptoCurrenciesMapper.selectByPrimaryKey(mbrCryptoCurrencies.getId());
            if (!cryptoCurrencies.getAccountId().equals(mbrCryptoCurrencies.getAccountId())) {
                return R.error("?????????????????????????????????");
            }
        }

        AgentCryptoCurrencies tempCurrencies = agapiMapper.selectCryptoCurrenciesByAddress(
                mbrCryptoCurrencies.getId(), mbrCryptoCurrencies.getAccountId());

        if (isNull(tempCurrencies)) {    // ???????????????, ????????????
            List<AgentCryptoCurrencies> currenciesList = agapiMapper.selectCryptocurrenciesCount(null, mbrCryptoCurrencies.getWalletAddress());
            if (currenciesList.size() > 0) {
                return R.error("????????????????????????????????????????????????!");
            }
            AgentCryptoCurrencies currencies = new AgentCryptoCurrencies();
            currencies.setAccountId(mbrCryptoCurrencies.getAccountId());
            currencies.setIsDel(Constants.Available.disable);
            currencies.setWalletAddress(mbrCryptoCurrencies.getWalletAddress());
            currencies.setWalletName(mbrCryptoCurrencies.getWalletName());
            currencies.setCurrencyCode(mbrCryptoCurrencies.getCurrencyCode());
            currencies.setCurrencyProtocol(mbrCryptoCurrencies.getCurrencyProtocol());
            currencies.setWalletId(mbrCryptoCurrencies.getWalletId());
            currencies.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            currencies.setAvailable(Integer.valueOf(Constants.EVNumber.one).byteValue());
            SetBasicSysCryptoCurrencies tmp = setBasicSysCryptoCurrenciesMapper.getCrByCodeAndProtocol(currencies.getCurrencyCode(), currencies.getCurrencyProtocol());
            currencies.setBankCardId(tmp.getBankCardId());
            super.save(currencies);
            return R.ok();
        } else {
            List<AgentCryptoCurrencies> currenciesList = agapiMapper.selectCryptocurrenciesCount(
                    mbrCryptoCurrencies.getId(),
                    mbrCryptoCurrencies.getWalletAddress());
            if (currenciesList.size() > 0) {
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
            tempCurrencies.setWalletAddress(mbrCryptoCurrencies.getWalletAddress());
            super.update(tempCurrencies);
            return R.ok();
        }
    }

    public List<AgentCryptoCurrencies> listCondCryptoCurrencies(Integer accountId) {
        AgentCryptoCurrencies mbrCryptoCurrencies = new AgentCryptoCurrencies();
        mbrCryptoCurrencies.setAccountId(accountId);
        mbrCryptoCurrencies.setIsDel(Constants.Available.disable);
        mbrCryptoCurrencies.setAvailable(Constants.Available.enable);
        List<AgentCryptoCurrencies> bankcards = agapiMapper.userCryptoCurrencies(mbrCryptoCurrencies);
        bankcards.stream().forEach(bs -> {
            FundMerchantPay merchantPay = new FundMerchantPay();
            merchantPay.setAvailable(Constants.EVNumber.zero);
            merchantPay.setMethodType(Constants.EVNumber.one);
            merchantPay.setCurrencyProtocol(bs.getCurrencyProtocol());
            int count = fundMerchantPayMapper.selectCount(merchantPay);
            if (count == 0) {
                bs.setAvailable(Constants.Available.disableTwo);
            }
        });
        // ????????????
        bankcards.forEach(e -> {
            e.setWalletAddressEncryption(StringUtil.walletAddress(e.getWalletAddress()));
        });
        return bankcards;
    }
    

	public R unbindWalletList(Integer accountId, Integer walletId) {
		// ???????????????
		AgentCryptoCurrencies agentCryptoCurrencies = cryptoCurrenciesMapper.selectByPrimaryKey(walletId);
		if (agentCryptoCurrencies == null) {
			return R.error("???????????????");
		}
		// ????????????
		AgyWithdraw accWithdraw = new AgyWithdraw();
		accWithdraw.setCryptoCurrenciesId(walletId);
		List<AgyWithdraw> agyWithdrawList = agyWithdrawMapper.select(accWithdraw);
		if (CollectionUtil.isNotEmpty(agyWithdrawList)) {
			return R.error("???????????????????????????????????????????????????");
		}
		cryptoCurrenciesMapper.deleteByPrimaryKey(walletId);
		return R.ok();
	}

}
