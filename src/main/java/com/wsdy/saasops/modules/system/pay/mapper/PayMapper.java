package com.wsdy.saasops.modules.system.pay.mapper;

import com.wsdy.saasops.api.modules.pay.dto.OnlinePayPicture;
import com.wsdy.saasops.api.modules.pay.dto.PayPictureData;
import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.system.pay.dto.StatisticsSucRateDto;
import com.wsdy.saasops.modules.system.pay.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface PayMapper {

    List<SysDeposit> findDepositList(SysDeposit deposit);

    List<SetBacicOnlinepay> findOnlinePayList(SetBacicOnlinepay onlinepay);

    List<MbrGroup> findDepGroupByDepositId(
            @Param("depositId") Integer depositId);

    List<MbrGroup> findOnlineGroupByDepositId(
            @Param("onlinePayId") Integer onlinePayId);

    SysDeposit findDepositByBank(
            @Param("bankAccount") String bankAccount,
            @Param("id") Integer id);

    List<TPay> findPayBySiteCode(
            @Param("siteCode") String siteCode,
            @Param("paymentTypes") List<Integer> paymentTypes);

    List<BaseBank> findBankByPayId(
            @Param("payId") Integer payId);

    List<SysDeposit> findDepositByGroupId(
            @Param("groupId") Integer groupId);

    List<SysDeposit> findDepositAll();

    List<SetBacicOnlinepay> findOnlineListByGroupId(
            @Param("groupId") Integer groupId);

    List<SysDeposit> findDepositBankList(
            @Param("accountId") Integer accountId);

    List<SetBacicOnlinepay> findOnlineList();

    SysDeposit findDepositByGroupIdAndDepositId(
            @Param("groupId") Integer groupId,
            @Param("depositId") Integer depositId);

    String findPayId(@Param("fastPayId") Integer fastPayId);

    List<OnlinePayPicture> findOnlinePayListByAccountId(
            @Param("accountId") Integer accountId,
            @Param("devSource") String devSource,
            @Param("onlinePayId") Integer onlinePayId,
            @Param("isSign") Boolean isSign);

    List<PayPictureData> findBankListByPayId(
            @Param("payId") Integer payId, @Param("terminal") Integer terminal);

    SetBacicOnlinepay findOnlinePayAndPay(
            @Param("onlinePayId") Integer onlinePayId);

    int updateDepositAmount(
            @Param("depositAmount") BigDecimal depositAmount,
            @Param("depositId") Integer depositId);

    int updateQrCodeAmount(
            @Param("depositAmount") BigDecimal depositAmount,
            @Param("depositId") Integer depositId);

    int updateOnlinePayAmount(
            @Param("depositAmount") BigDecimal depositAmount,
            @Param("onlinePayId") Integer onlinePayId);

    int clearDepositAmount();

    int clearOnlinePayAmount();

    List<SetBacicOnlinepay> querySetBacicOnlinepayList();

    List<SetBacicFastPay> findBasicFastPay(SetBacicFastPay fastPay);

    List<MbrGroup> findFastPayGroupById(
            @Param("fastPayId") Integer fastPayId);

    List<SetBacicFastPay> findFastPayBankList(
            @Param("accountId") Integer accountId);

    List<SetBacicFastPay> findFastDepositWithdrawList(
            @Param("accountId") Integer accountId);

    List<SysDeposit> fundFastPayDepositList(
            @Param("fastPayId") Integer fastPayId);

    SysDeposit findFastPayDepositByGroupId(
            @Param("groupId") Integer groupId,
            @Param("depositId") Integer depositId);

    SysDeposit findSysDepositById(
            @Param("id") Integer id);

    List<SysDeposit> findSysDepositByFastPayId(
            @Param("fastPayId") Integer fastPayId);

    int findFastDepositCount(
            @Param("id") Integer id,
            @Param("bankAccount") String bankAccount);

    int findPayMbrGroupRelationMaxSort(
            @Param("groupId") Integer groupId);

    int findFastPayGroupMaxSort(
            @Param("groupId") Integer groupId);

    int findSetBasicSysDepMbrMaxSort(
            @Param("groupId") Integer groupId);

    BaseBank findBankById(@Param("bankId") Integer bankId);

    List<SetBasicPaymbrGroupRelation> getOnLinePayGroupIsQueue(@Param("paymentType") Integer paymentType);

    List<SetBasicFastPayGroup> getFastPayGroupIsQueue();

    List<SetBasicSysDepMbr> getBankPayGroupIsQueue();

    List<StatisticsSucRateDto>  statisticSucRate(StatisticsSucRateDto statisticsSucRateDto);

    void deletFastPayByIdEx(SetBasicFastPayGroup fastPayGroup );
    void deleteSysDepMbrEx(SetBasicSysDepMbr depMbr );
    void deleteGroupRelationEx(SetBasicPaymbrGroupRelation depMbr);
}
