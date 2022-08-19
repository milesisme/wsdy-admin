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

        if(Constants.EVNumber.one == merchantPay.getMethodType().intValue()){  // 加密钱包
            fundMerchantPay.setCurrencyCode(merchantPay.getCurrencyCode());
            fundMerchantPay.setCurrencyProtocol(merchantPay.getCurrencyProtocol());
            // 同类型同协议的加密货币仅一个开启状态
            int count = merchantPayMapper.selectCount(fundMerchantPay);
            merchantPay.setAvailable(count > 0 ? Constants.EVNumber.zero : Constants.EVNumber.one);

            // 补充几个非空字段
            merchantPay.setMerchantName(merchantPay.getCurrencyCode() + "(" + merchantPay.getCurrencyProtocol() + ")");
            merchantPay.setSort(0);
            merchantPay.setUrl(evelletUrl);
            merchantPay.setDevSource("0,3");
        }
        if(Constants.EVNumber.zero == merchantPay.getMethodType().intValue() || Constants.EVNumber.two == merchantPay.getMethodType().intValue()){  // 银行卡或支付宝
            // 银行卡仅一个开启状态
            int count = merchantPayMapper.selectCount(fundMerchantPay);
            merchantPay.setAvailable(count > 0 ? Constants.EVNumber.zero : Constants.EVNumber.one);
        }

        // 插入代付表
        merchantPay.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        merchantPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        merchantPayMapper.insert(merchantPay);
        // 插入会员组关联
        if (Collections3.isNotEmpty(merchantPay.getIds())) {
            insertListMerchantScope(merchantPay);
        }

        //添加操作日志
        if(Constants.EVNumber.zero == merchantPay.getMethodType().intValue()) {  // 银行卡
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
        if(Constants.EVNumber.one == merchantPay.getMethodType().intValue()) {  // 加密钱包
            if (merchantPay.getAvailable() == Constants.EVNumber.one) {
                // 当为启用时，把同协议同币种的代付都关闭
                fundMapper.updateMerchantPayAvailableCryptoCurrencies(merchantPay);
                // 更新代付表
                merchantPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                merchantPayMapper.updateByPrimaryKeySelective(merchantPay);
            }
        }
        if(Constants.EVNumber.zero == merchantPay.getMethodType().intValue() || Constants.EVNumber.two == merchantPay.getMethodType().intValue()
            || Constants.EVNumber.three == merchantPay.getMethodType().intValue()){  // 银行卡和支付宝以及其他钱包
            if (merchantPay.getAvailable() == Constants.EVNumber.one) {
                // 当为启用时，把所有银行卡类型代付都关闭
                fundMapper.updateMerchantPayAvailable();
            }
            // 更新代付表
            merchantPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            merchantPayMapper.updateByPrimaryKey(merchantPay);
        }
        // 处理会员组关联
        FundMerchantScope scope = new FundMerchantScope();
        scope.setMerchantId(merchantPay.getId());
        merchantScopeMapper.delete(scope);
        insertListMerchantScope(merchantPay);

        //添加修改操作日志
        if(Constants.EVNumber.zero == merchantPay.getMethodType().intValue() || Constants.EVNumber.two == merchantPay.getMethodType().intValue()) {  // 银行卡和支付宝
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
            throw new R200Exception("该商户号自动出款已使用，无法删除!");
        }
        merchantPayMapper.deleteByPrimaryKey(id);
        FundMerchantScope scope = new FundMerchantScope();
        scope.setMerchantId(id);
        merchantScopeMapper.delete(scope);
    }

    public void updateFundMerchantPayAvailable(FundMerchantPay merchantPay, String userName, String ip) {
        FundMerchantPay old = merchantPayMapper.selectByPrimaryKey(merchantPay.getId());
        if(Objects.isNull(old)){
            throw new R200Exception("该记录不存在！");
        }
        // 如果是开启
        if (merchantPay.getAvailable() == Constants.EVNumber.one) {
            FundMerchantPay merchantPay1 = new FundMerchantPay();
            merchantPay1.setAvailable(Constants.EVNumber.one);
            merchantPay1.setMethodType(old.getMethodType());
            if(Constants.EVNumber.one == old.getMethodType().intValue()){  // 加密钱包
                merchantPay1.setCurrencyCode(old.getCurrencyCode());
                merchantPay1.setCurrencyProtocol(old.getCurrencyProtocol());
            } else if (Constants.EVNumber.three == old.getMethodType().intValue()) { // 其他钱包
                merchantPay1.setCurrencyCode(old.getCurrencyCode());
            }
            // 加密钱包：同币种同协议的全部关闭； 银行卡：全部关闭
            List<FundMerchantPay> merchantPays = merchantPayMapper.select(merchantPay1);
            if (Collections3.isNotEmpty(merchantPays)) {
                merchantPays.stream().forEach(ms -> {
                    ms.setAvailable(Constants.EVNumber.zero);
                    merchantPayMapper.updateByPrimaryKey(ms);
                });
            }
        }
        // 然后单独打开该条
        FundMerchantPay fundMerchantPay = new FundMerchantPay();
        fundMerchantPay.setAvailable(merchantPay.getAvailable());
        fundMerchantPay.setId(merchantPay.getId());
        fundMerchantPay.setMethodType(old.getMethodType());     // 不加1会被改变为0
        fundMerchantPay.setMerchantName(merchantPay.getMerchantName());
        fundMerchantPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundMerchantPay.setModifyUser(merchantPay.getModifyUser());
        merchantPayMapper.updateByPrimaryKeySelective(fundMerchantPay);

        //添加操作日志
        if(Constants.EVNumber.zero == old.getMethodType().intValue()) {  // 银行卡
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
