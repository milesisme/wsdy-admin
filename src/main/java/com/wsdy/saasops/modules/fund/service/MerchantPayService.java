package com.wsdy.saasops.modules.fund.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.fund.dao.*;
import com.wsdy.saasops.modules.fund.entity.*;
import com.wsdy.saasops.modules.fund.mapper.FundMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;

@Service
@Transactional
public class MerchantPayService {

    @Autowired
    private FundWhiteListMapper whiteListMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private FundMerchantPayMapper merchantPayMapper;
    @Autowired
    private FundMerchantScopeMapper merchantScopeMapper;
    @Autowired
    private FundMerchantDetailMapper merchantDetailMapper;
    @Autowired
    private TChannelPayMapper channelPayMapper;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Value("${evellet.url}")
    private String evelletUrl;

    public FundWhiteList findFundWhiteList(Integer accountId) {
        FundWhiteList whiteList = new FundWhiteList();
        whiteList.setAccountId(accountId);
        return whiteListMapper.selectOne(whiteList);
    }

    public void addFundWhiteList(FundWhiteList whiteList) {
        FundWhiteList fundWhiteList = new FundWhiteList();
        fundWhiteList.setAccountId(whiteList.getAccountId());
        FundWhiteList whiteList1 = whiteListMapper.selectOne(fundWhiteList);
        if (isNull(whiteList1)) {
            MbrAccount account = accountMapper.selectByPrimaryKey(whiteList.getAccountId());
            whiteList.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            whiteList.setLoginName(account.getLoginName());
            whiteListMapper.insert(whiteList);
        }
    }

    public void deleteFundWhiteList(Integer id) {
        whiteListMapper.deleteByPrimaryKey(id);
    }

    public PageUtils findFundMerchantPayList(FundMerchantPay merchantPay, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<FundMerchantPay> list = fundMapper.findFundMerchantPayList(merchantPay);
        return BeanUtil.toPagedResult(list);
    }

    public void addFundMerchantPay(FundMerchantPay merchantPay) {
        FundMerchantPay fundMerchantPay = new FundMerchantPay();
        fundMerchantPay.setAvailable(Constants.EVNumber.one);
        fundMerchantPay.setMethodType(merchantPay.getMethodType());

        if(Constants.EVNumber.one == merchantPay.getMethodType().intValue()){  // ????????????
            fundMerchantPay.setCurrencyCode(merchantPay.getCurrencyCode());
            fundMerchantPay.setCurrencyProtocol(merchantPay.getCurrencyProtocol());
            // ??????????????????????????????????????????????????????
            int count = merchantPayMapper.selectCount(fundMerchantPay);
            merchantPay.setAvailable(count > 0 ? Constants.EVNumber.zero : Constants.EVNumber.one);

            // ????????????????????????
            merchantPay.setMerchantName(merchantPay.getCurrencyCode() + "(" + merchantPay.getCurrencyProtocol() + ")");
            merchantPay.setSort(0);
            merchantPay.setUrl(evelletUrl);
            merchantPay.setDevSource("0,3");
        }
        if(Constants.EVNumber.zero == merchantPay.getMethodType().intValue() || Constants.EVNumber.two == merchantPay.getMethodType().intValue()){  // ?????????????????????
            // ??????????????????????????????
            int count = merchantPayMapper.selectCount(fundMerchantPay);
            merchantPay.setAvailable(count > 0 ? Constants.EVNumber.zero : Constants.EVNumber.one);
        }

        // ???????????????
        merchantPay.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        merchantPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        merchantPayMapper.insert(merchantPay);
        // ?????????????????????
        if (Collections3.isNotEmpty(merchantPay.getIds())) {
            insertListMerchantScope(merchantPay);
        }

        //??????????????????
        if(Constants.EVNumber.zero == merchantPay.getMethodType().intValue()) {  // ?????????
            mbrAccountLogService.addFundMerchantPayRMB(merchantPay);
        }else{
            mbrAccountLogService.addFundMerchantPay(merchantPay);
        }
    }

    private void insertListMerchantScope(FundMerchantPay merchantPay) {
        List<FundMerchantScope> scopeList =
                merchantPay.getIds().stream().map(d -> {
                    FundMerchantScope scope = new FundMerchantScope();
                    scope.setGroupId(d);
                    scope.setMerchantId(merchantPay.getId());
                    return scope;
                }).collect(Collectors.toList());
        merchantScopeMapper.insertList(scopeList);
    }

    public void updateFundMerchantPay(FundMerchantPay merchantPay, String userName, String ip) {
        if(Constants.EVNumber.one == merchantPay.getMethodType().intValue()) {  // ????????????
            if (merchantPay.getAvailable() == Constants.EVNumber.one) {
                // ?????????????????????????????????????????????????????????
                fundMapper.updateMerchantPayAvailableCryptoCurrencies(merchantPay);
                // ???????????????
                merchantPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                merchantPayMapper.updateByPrimaryKeySelective(merchantPay);
            }
        }
        if(Constants.EVNumber.zero == merchantPay.getMethodType().intValue() || Constants.EVNumber.two == merchantPay.getMethodType().intValue()
            || Constants.EVNumber.three == merchantPay.getMethodType().intValue()){  // ???????????????????????????????????????
            if (merchantPay.getAvailable() == Constants.EVNumber.one) {
                // ?????????????????????????????????????????????????????????
                fundMapper.updateMerchantPayAvailable();
            }
            // ???????????????
            merchantPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            merchantPayMapper.updateByPrimaryKey(merchantPay);
        }
        // ?????????????????????
        FundMerchantScope scope = new FundMerchantScope();
        scope.setMerchantId(merchantPay.getId());
        merchantScopeMapper.delete(scope);
        insertListMerchantScope(merchantPay);

        //????????????????????????
        if(Constants.EVNumber.zero == merchantPay.getMethodType().intValue() || Constants.EVNumber.two == merchantPay.getMethodType().intValue()) {  // ?????????????????????
            mbrAccountLogService.updateFundMerchantPayRMB(merchantPay);
        }else{
            mbrAccountLogService.updateFundMerchantPay(merchantPay);
        }
    }

    public void deleteFundMerchantPay(Integer id) {
        FundMerchantDetail merchantDetail = new FundMerchantDetail();
        merchantDetail.setMerchantId(id);
        int count = merchantDetailMapper.selectCount(merchantDetail);
        if (count > 0) {
            throw new R200Exception("????????????????????????????????????????????????!");
        }
        merchantPayMapper.deleteByPrimaryKey(id);
        FundMerchantScope scope = new FundMerchantScope();
        scope.setMerchantId(id);
        merchantScopeMapper.delete(scope);
    }

    public void updateFundMerchantPayAvailable(FundMerchantPay merchantPay, String userName, String ip) {
        FundMerchantPay old = merchantPayMapper.selectByPrimaryKey(merchantPay.getId());
        if(Objects.isNull(old)){
            throw new R200Exception("?????????????????????");
        }
        // ???????????????
        if (merchantPay.getAvailable() == Constants.EVNumber.one) {
            FundMerchantPay merchantPay1 = new FundMerchantPay();
            merchantPay1.setAvailable(Constants.EVNumber.one);
            merchantPay1.setMethodType(old.getMethodType());
            if(Constants.EVNumber.one == old.getMethodType().intValue()){  // ????????????
                merchantPay1.setCurrencyCode(old.getCurrencyCode());
                merchantPay1.setCurrencyProtocol(old.getCurrencyProtocol());
            } else if (Constants.EVNumber.three == old.getMethodType().intValue()) { // ????????????
                merchantPay1.setCurrencyCode(old.getCurrencyCode());
            }
            // ??????????????????????????????????????????????????? ????????????????????????
            List<FundMerchantPay> merchantPays = merchantPayMapper.select(merchantPay1);
            if (Collections3.isNotEmpty(merchantPays)) {
                merchantPays.stream().forEach(ms -> {
                    ms.setAvailable(Constants.EVNumber.zero);
                    merchantPayMapper.updateByPrimaryKey(ms);
                });
            }
        }
        // ????????????????????????
        FundMerchantPay fundMerchantPay = new FundMerchantPay();
        fundMerchantPay.setAvailable(merchantPay.getAvailable());
        fundMerchantPay.setId(merchantPay.getId());
        fundMerchantPay.setMethodType(old.getMethodType());     // ??????1???????????????0
        fundMerchantPay.setMerchantName(merchantPay.getMerchantName());
        fundMerchantPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundMerchantPay.setModifyUser(merchantPay.getModifyUser());
        merchantPayMapper.updateByPrimaryKeySelective(fundMerchantPay);

        //??????????????????
        if(Constants.EVNumber.zero == old.getMethodType().intValue()) {  // ?????????
            mbrAccountLogService.updateFundMerchantPayAvailableRMB(merchantPay,old);
        }else{
            mbrAccountLogService.updateFundMerchantPayAvailable(merchantPay,old);
        }
    }

    public FundMerchantPay findFundMerchantPayOne(Integer id) {
        FundMerchantPay merchantPay = merchantPayMapper.selectByPrimaryKey(id);
        FundMerchantScope scope = new FundMerchantScope();
        scope.setMerchantId(merchantPay.getId());
        List<FundMerchantScope> scopeList = merchantScopeMapper.select(scope);
        if (Collections3.isNotEmpty(scopeList)) {
            merchantPay.setIds(scopeList.stream().map(
                    st -> st.getGroupId()).collect(Collectors.toList()));
        }
        return merchantPay;
    }

    public List<TChannelPay> findTChannelPayList(Integer methodType) {
        TChannelPay channelPay = new TChannelPay();
        channelPay.setAvailable(Constants.EVNumber.one);
        channelPay.setMethodType(methodType);
        return channelPayMapper.select(channelPay);
    }
    public Map<String, List<TChannelPay>> findTChannelPayListCr(Integer methodType) {
        TChannelPay channelPay = new TChannelPay();
        channelPay.setAvailable(Constants.EVNumber.one);
        channelPay.setMethodType(methodType);
        List<TChannelPay> list = channelPayMapper.select(channelPay);
        if(Collections3.isEmpty(list)){
            return null;
        }
        Map<String, List<TChannelPay>> groupBy = list.stream().collect(
                Collectors.groupingBy(
                        TChannelPay::getCurrencyCode));
        return groupBy;
    }

    public List<FundMerchantPay> getMerchantPay(FundMerchantPay queryParam){
        return merchantPayMapper.select(queryParam);
    }
}
