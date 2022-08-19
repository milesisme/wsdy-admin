package com.wsdy.saasops.modules.fund.mapper;


import com.wsdy.saasops.api.modules.pay.dto.DepositListDto;
import com.wsdy.saasops.api.modules.pay.dto.DepositPostScript;
import com.wsdy.saasops.modules.fund.dto.CountEntity;
import com.wsdy.saasops.modules.fund.dto.CuiDanDto;
import com.wsdy.saasops.modules.fund.dto.DepositDaysTotalDto;
import com.wsdy.saasops.modules.fund.dto.DepositStatisticsByPayDto;
import com.wsdy.saasops.modules.fund.entity.*;
import com.wsdy.saasops.modules.member.dto.BillRecordDto;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.entity.VerifyDeposit;
import com.wsdy.saasops.modules.operate.dto.MemDayRuleScopeDto;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@Mapper
public interface FundMapper {

    Map<String,Object> querySumFeeFreeTimes(@Param("accountId") Integer accountId, @Param("startTime") String createTimeFrom);

    List<FundDeposit> findDepositList(FundDeposit fundDeposit);

    List<FundDeposit> findDepositListApi(DepositListDto fundDeposit);

    List<FundDeposit> findDepositListApiCompany(DepositListDto fundDeposit);

    List<FundDeposit> findDepositListApiOther(DepositListDto fundDeposit);
    
    List<FundDeposit> findDepositAndOtherList(DepositListDto fundDeposit);

    Double findDepositSum(DepositListDto fundDeposit);
    
    Double findDepositSumOther(DepositListDto fundDeposit);
    
    Double findDepositSumAndAudit(DepositListDto fundDeposit);

    Double findSumDepositAmount(FundDeposit fundDeposit);

    int findDepositCount(FundDeposit fundDeposit);

    List<AccWithdraw> findAccWithdrawList(AccWithdraw accWithdraw);

    List<AccWithdraw> findFixateAccWithdraw(@Param("startTime") String startTime, @Param("endTime") String endTime,
                                            @Param("accountId") Integer accountId, @Param("status") Integer status,
                                            @Param("orderBy") String orderBy);
    
    double totalFixateAccWithdraw(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("accountId") Integer accountId);

    int findAccWithdrawCount(AccWithdraw accWithdraw);

    Double accSumDrawingAmount(AccWithdraw accWithdraw);

    List<MbrBillManage> findMbrBillManageList(MbrBillManage fundBillReport);

    List<FundAudit> findFundAuditList(FundAudit fundAudit);

    FundDeposit findFundDepositOne(FundDeposit fundDeposit);

    int updatePayStatus(FundDeposit deposit);

    AccWithdraw sumWithDraw(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("accountId") Integer accountId);
    
    Byte isFreeFee(@Param("feeTimes") Integer feeTimes, @Param("startTime") String startTime, @Param("accountId") Integer accountId);
    
    int sumApplyRec(@Param("accountId") Integer accountId);

    List<FundDeposit> findDepositActivity(FundDeposit deposit);

    int findDepositActivityCount(FundDeposit deposit);

    DepositPostScript findOfflineDepositInfo(@Param("id") Integer id);

    List<FundMerchantPay> findFundMerchantPayList(FundMerchantPay fundMerchantPay);

    int findMerchantPayCount(@Param("accountId") Integer accountId);

    List<AccWithdraw> fundAccWithdrawMerchant(@Param("accountId") Integer accountId);

    List<QuickFunction> listCount();

    AccWithdraw updateMerchantPayLock(@Param("id") Integer id);

    int updateMerchantPayAvailable();

    int updateMerchantPayAvailableCryptoCurrencies(FundMerchantPay merchantPay);

    MbrWallet findAccountBalance(@Param("loginName") String loginName);

    List<MbrWallet> queryAccountBalanceByLoginNames(@Param("loginNames") List<String> loginNames);

    List<BillRecordDto> findBillRecordList(BillRecordDto billRecordDto);

    List<FundDeposit> fundFundDepositByTime();

    FundMerchantDetail findFundMerchantDetailByTransId(@Param("orderId") String orderId);

    List<CountEntity> depositCountByStatus(FundDeposit fundDeposit);
    List<CountEntity> withdrawCountByStatus(AccWithdraw accWithdraw);

    List<AccWithdraw> findAccWithdrawListAll(AccWithdraw accWithdraw);

    BigDecimal sumFundDepositByAccountId(@Param("accountId") Integer accountId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    BigDecimal sumFundDepositVipRed(@Param("accountId") Integer accountId, @Param("startTime") String startTime, @Param("endTime") String endTime);
    
    MemDayRuleScopeDto sumAndCountFundDeposit(@Param("accountId") Integer accountId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    AccWithdraw findWithdrawAudit(@Param("accountId") Integer accountId);

    FundDeposit findDepositListSum(FundDeposit fundDeposit);

    AccWithdraw findAccWithdrawListSum(AccWithdraw accWithdraw);

    BigDecimal findTodayWithdraw();


    List<DepositStatisticsByPayDto>  depositStatisticByPay(DepositStatisticsByPayDto depositStatisticsByPayDto);

    FundDeposit getRecentDeposit(FundDeposit fundDeposit);

    FundDeposit getDepositSumAndDays(FundDeposit fundDeposit);

    DepositDaysTotalDto getMbrContinueDeposit(DepositDaysTotalDto fundDeposit);

    FundMerchantPay getMerchantPayByOrderno(@Param("orderNo") String orderNo);

    int updateAllLockStatus();

    List<FundDeposit> findVerifyDeposit();

    VerifyDeposit findSecretDepositOne(@Param("depositId") Integer depositId,
                                       @Param("siteCode") String siteCode);

	Integer findAccDepositCount(FundDeposit fundDeposit);

    Integer findTotalsDepositCount(Integer accountId);


    Integer findLastDepositCount(Integer accountId, Integer hours);


    AccWithdraw findLastAccWithdraw(Integer accountId);


    AccWithdraw findCuiDanAccWithdraw(Integer accountId, Integer orderId);


    int updateCuiDanAccWithdraw(Integer orderId);


    List<CuiDanDto> getNewCuiDan(Integer second);

}
