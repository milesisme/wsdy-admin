package com.wsdy.saasops.api.modules.activity.service;


import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.activity.dto.*;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.utils.HttpsRequestUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.service.FundWithdrawService;
import com.wsdy.saasops.modules.log.entity.LogMbrRegister;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.api.modules.activity.mapper.ApiHuPengMapper;
import com.wsdy.saasops.api.modules.user.mapper.ApiPromotionMapper;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.modules.activity.dto.HuPengRewardDto;
import com.wsdy.saasops.modules.mbrRebateAgent.service.MbrRebateAgentService;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrWalletMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.entity.MbrWithdrawalCond;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.member.service.MbrWithdrawalCondService;
import com.wsdy.saasops.modules.operate.entity.OprActRule;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_25_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Slf4j
@Service
@Transactional
public class HuPengRebateService {
    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private ApiPromotionMapper apiPromotionMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private MbrRebateAgentService mbrRebateAgentService;
    @Autowired
    private ApiHuPengMapper apiHuPengMapper;
    @Autowired
    private MbrWalletMapper mbrWalletMapper;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private MbrWithdrawalCondService mbrWithdrawalCondService;

    @Autowired
    private FundWithdrawService fundWithdrawService;


    public OprActRule findActRuleByCode(String tmplCode){
        return operateActivityMapper.findActRuleByCode(tmplCode);
    }

    public PageUtils getApiHuPengRebateDtoList(String startTime, String endTime, Integer accountId, String subLoginName, Integer pageNo, Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        List<HuPengRewardDto>  apiFriendRebateDtoList =  apiHuPengMapper.getApiHuPengRebateDtoList( startTime,  endTime,  accountId,  subLoginName);
        return BeanUtil.toPagedResult(apiFriendRebateDtoList);
    }


    public PageUtils getHuPengRebateRewardReportForDay(String startTime, String endTime, Integer accountId, Integer pageNo, Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        if(StringUtil.isNotEmpty(startTime)){
            startTime = startTime + " 00:00:00";
        }
        if(StringUtil.isNotEmpty(endTime)){
            endTime  = endTime + " 23:59:59";
        }
        List<HuPengRebateRewardDto> huPengRebateRewardReportForDay = apiHuPengMapper.getHuPengRebateRewardReportForDay( startTime,  endTime,  accountId);
        huPengRebateRewardReportForDay.removeAll(Collections.singleton(null));
        return BeanUtil.toPagedResult(huPengRebateRewardReportForDay);
    }

    public PageUtils getHuPengRebateRewardReportForMonth(String startTime, String endTime, Integer accountId, Integer pageNo, Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        if(StringUtil.isNotEmpty(startTime)){
            startTime = startTime + " 00:00:00";
        }
        if(StringUtil.isNotEmpty(endTime)){
            endTime  = endTime + " 23:59:59";
        }
        List<HuPengRebateRewardDto> apiFriendRebateRewardDtoList = apiHuPengMapper.getHuPengRebateRewardReportForMonth( startTime,  endTime,  accountId);
        apiFriendRebateRewardDtoList.removeAll(Collections.singleton(null));
        return BeanUtil.toPagedResult(apiFriendRebateRewardDtoList);
    }

    public HuPengFriendRewardSummaryDto getHupengRebateRewardSummary( String startTime, String endTime, Integer accountId, String subLoginName){
        return apiHuPengMapper.getHupengRebateRewardSummary( startTime,  endTime,  accountId,  subLoginName);
    }

    public HuPengRebateInfoDto getHuPengRebateActInfo(Integer accountId) {
        HuPengRebateInfoDto huPengRebateInfoDto = new HuPengRebateInfoDto();
        huPengRebateInfoDto.setIsShowHupengRebate(Boolean.FALSE);
        OprActRule  oprActRule =  findActRuleByCode( TOpActtmpl.mbrRebateHuPengCode);

        if(oprActRule!= null && oprActRule.getAvailable() == 1){
            huPengRebateInfoDto.setIsShowHupengRebate(Boolean.TRUE);
        }

        if(accountId == null){
            huPengRebateInfoDto.setCodeId("");
            return huPengRebateInfoDto;
        }
        // 获得代理会员信息
        MbrAccount mbrAccount = mbrAccountMapper.selectByPrimaryKey(accountId);
        // 获取推荐人code

        if(mbrAccount == null){
            huPengRebateInfoDto.setCodeId("");
            return huPengRebateInfoDto;
        }
        if(StringUtil.isEmpty(mbrAccount.getDomainCode())){
            mbrAccount.setDomainCode(mbrRebateAgentService.getDomainCode());
            mbrAccountMapper.updateByPrimaryKeySelective(mbrAccount);
        }
        huPengRebateInfoDto.setCodeId(mbrAccount.getDomainCode());
        return huPengRebateInfoDto;
    }

    public HuPengBalanceDto getHuPengBalance(Integer accountId){
        MbrWallet wallet = new MbrWallet();
        wallet.setAccountId(accountId);
        // 账户余额
        MbrWallet mbrWallet = mbrWalletMapper.selectOne(wallet);
        HuPengBalanceDto huPengBalanceDto = new HuPengBalanceDto();
        huPengBalanceDto.setBalance(mbrWallet.getHuPengBalance());
        
//        // 获取取款条件
        MbrWithdrawalCond mbrWithdrawalCond = mbrWithdrawalCondService.getMbrWithDrawal(accountId);
        // 如果最低取款配置不等于0 并且大于0
       if (mbrWithdrawalCond != null && mbrWithdrawalCond.getRebateMinimum() != null
       		&& mbrWithdrawalCond.getRebateMinimum().compareTo(BigDecimal.ZERO) == 1) {
       	huPengBalanceDto.setMinAmount(mbrWithdrawalCond.getRebateMinimum());
        }
        return huPengBalanceDto;
    }


    public void withdrawal(Integer accountId, String loginName, BigDecimal amount, Integer type, Integer bankCardId, HttpServletRequest request  ){
    	// 如果是银行卡取款，校验最低取款限额
    	if (type == Constants.EVNumber.two) {
    		// 获取取款条件
            MbrWithdrawalCond mbrWithdrawalCond = mbrWithdrawalCondService.getMbrWithDrawal(accountId);
            // 如果最低取款配置不等于0 并且大于0
            if (mbrWithdrawalCond != null && mbrWithdrawalCond.getRebateMinimum() != null
            		&& mbrWithdrawalCond.getRebateMinimum().compareTo(BigDecimal.ZERO) == 1) {
            	// 如果取款金额小于最低配置
            	if (amount.compareTo(mbrWithdrawalCond.getRebateMinimum()) == -1) {
            		throw new R200Exception("最低领取金额不可以小于" + mbrWithdrawalCond.getRebateMinimum());
            	}
           }
    	}
    	
        MbrWallet wallet = new MbrWallet();
        wallet.setAccountId(accountId);
        // 账户余额
        MbrWallet mbrWallet = mbrWalletMapper.selectOne(wallet);
        if(mbrWallet.getHuPengBalance().compareTo(amount) ==-1){
            throw new R200Exception("余额不够无法提款");
        }

        if(type == Constants.EVNumber.one){
            MbrWallet subWallet = new MbrWallet();
            subWallet.setAccountId(accountId);
            subWallet.setHuPengBalance(amount);
            // 保存帐变和减少钱包余额
            MbrBillDetail mbrBillDetail = new MbrBillDetail();
            mbrBillDetail.setLoginName(loginName);
            mbrBillDetail.setAccountId(accountId);

            mbrBillDetail.setFinancialCode(OrderConstants.FUND_ORDER_HUPENG_ACCWITHDRAW_CENTER);
            mbrBillDetail.setOrderNo(new SnowFlake().nextId() + "");
            mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
            mbrBillDetail.setDepotId(Constants.SYS_DEPOT_ID);
            mbrBillDetail.setOpType(MbrBillDetail.OpTypeStatus.expenditure);
            mbrBillDetail.setAmount(amount);

            // 提款手续费
            BigDecimal fee = new BigDecimal("0.0");
            mbrBillDetail.setMemo("提款金额:" + amount + ",手续费:" + CommonUtil.adjustScale(fee));
            boolean rt = mbrWalletService.hPWalletSubtract(subWallet, mbrBillDetail);
            if(rt){
                MbrWallet addWallet = new MbrWallet();
                addWallet.setBalance(amount);
                addWallet.setAccountId(accountId);
                mbrWalletService.castWalletAndBillDetail(loginName,
                        accountId,OrderConstants.FUND_ORDER_HUPENG,amount,
                        null, Boolean.TRUE, null, null);
            }
        }

        if(type == Constants.EVNumber.two){
            AccWithdraw withDraw = new AccWithdraw();
            withDraw.setAccountId(accountId);
            withDraw.setLoginName(loginName);
            withDraw.setCreateUser(loginName);
            withDraw.setIp(CommonUtil.getIpAddress(request));
            withDraw.setDrawingAmount(amount);
            withDraw.setMethodType(Constants.EVNumber.zero);
            withDraw.setBankCardId(bankCardId);

            String dev = request.getHeader("dev");
            Byte withdrawSource = HttpsRequestUtil.getHeaderOfDev(dev);
            withDraw.setWithdrawSource(withdrawSource == null ? LogMbrRegister.RegIpValue.pcClient : withdrawSource);
            // 获取siteCode
            TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
            fundWithdrawService.saveHuPengApply(withDraw,null , cpSite.getSiteCode());
        }

    }

    public  PageUtils rewardList(String startTime, String endTime, Integer accountId, Integer pageNo, Integer pageSize){
        PageHelper.startPage(pageNo, pageSize);
        List<HuPengRebateRewardListDto>  friendRebateRewardListDtoList=  apiHuPengMapper.rewardList( startTime,  endTime,  accountId);
        return BeanUtil.toPagedResult(friendRebateRewardListDtoList) ;
    }

    public HuPengSummaryDto getHuPengSummary(Integer accountId){
        return apiHuPengMapper.getHuPengSummary(accountId);
    }
}
