package com.wsdy.saasops.modules.member.mapper;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wsdy.saasops.modules.activity.dto.HuPengLevelDto;
import com.wsdy.saasops.modules.member.dto.*;
import com.wsdy.saasops.modules.member.entity.*;
import com.wsdy.saasops.sysapi.dto.RelationDto;
import com.wsdy.saasops.sysapi.dto.SubUserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.wsdy.saasops.modules.fund.dto.CountEntity;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.log.entity.LogMbrRegister;
import com.wsdy.saasops.modules.sys.entity.SysMenuEntity;
import com.wsdy.saasops.modules.task.entity.TaskBonus;


@Mapper
public interface MbrMapper {


    List<MbrGroup> findGroupList(MbrGroup record);

    MbrGroup selectMbrGroupById(@Param("groupId") Long groupId);

    MbrAccount findAccountLastLogin(MbrAccount record);

    List<MbrAccount> findAccountList(MbrAccount record);

    List<MbrAccount> queryAgentMbrListPage(MbrAccount record);

    List<MbrAccountOnline> findAccountOnlineList(MbrAccountOnline record);

    List<MbrDepotWallet> findAccountWallet(@Param("depotIds") Integer[] depotIds, @Param("accountId") Integer accountId);

    List<MbrDepotWallet> findDepots(Integer accountId);

    List<MbrDepotWallet> findAccByDepotId(Integer depotId);

    MbrAccount viewAccount(MbrAccount paramAccount);

    int changeMbrGroup(MbrAccount mbrAccount);

    List<MbrAccount> findMbrAccountList(MbrAccount mbrAccount);

    List<MbrAccount> findMbrAccountListForMassTexting(MbrAccount mbrAccount);

    MbrAccount accountMassTextingCount(MbrAccount record);

    MbrAccount viewOtherAccount(MbrAccount paramAccount);

    MbrBankcard findBankCardOne(Integer id);

    MbrBankcard findAccountFirstBankcard(Integer accountId);

    MbrBankcard selectBankInfoById(Integer id);

    MbrCryptoCurrencies selectCryptoCurrenciesInfoById(Integer id);

    MbrBankcard selectBankInfoByCard(@Param("cardNo") String cardNo);

    List<String> selectBankDifferentNumber(@Param("accountId") Integer accountId);

    MbrCryptoCurrencies selectCryptoCurrenciesByAddress(@Param("walletAddress") String walletAddress);

    int walletSubtract(MbrWallet record);

    int walletAdd(MbrWallet record);


    int hPWalletSubtract(MbrWallet record);


    int hPWalletAdd(MbrWallet record);

    int selectGroupCount(Long[] idArr);

    int updateGroupAvil(MbrGroup group);

    int updateGroupBatch(@Param("idArr") Integer[] idArr, @Param("groupId") Integer groupId);

    int deleteGroupBatch(Long[] idArr);

    List<MbrAccount> listAccName(@Param("accountIds") Integer[] accountIds);

    int deleteMemoBatch(@Param("ids") List<Integer> ids);

    List<String> getMemberAccountNames(MbrAccount record);


    List<String> getMemberAccountHuPengNames(String startTime, String endTime);


    List<String> getAccountNames(@Param("ids") List<Integer> ids);


    List<HuPengLevelDto> getParentList(String subLoginName);

    int updateBillManageStatus(MbrBillManage mbrBillManage);

    List<MbrBankcard> userBankCard(MbrBankcard bankCard);

    List<MbrCryptoCurrencies> userCryptoCurrencies(MbrCryptoCurrencies mbrCryptoCurrencies);


    List<AccWithdraw> queryAccWithdrawList(AccWithdraw accWithdraw);

    List<AccWithdraw> queryAccWithdrawListByCryptoCurrencies(AccWithdraw accWithdraw);

    List<Map<String, Object>> queryMbrList(MbrAccount mbrAccount);

    int countSameBankNum(MbrBankcard bankCard);
    int countSameCryptoCurrenciesNum(MbrCryptoCurrencies mbrCryptoCurrencies);

    int updateOffline(@Param("loginName") String loginName);

    int updateOfflineById(@Param("accountId") Integer accountId);

    MbrAccount findMbrAccount(
            @Param("accountId") int accountId,
            @Param("registerStartTime") String registerStartTime,
            @Param("registerStartEnd") String registerStartEnd);

    List<Integer> getAllMbrGroupIds();

    int countGroupMem(@Param("groupId") Integer groupId);

    MbrBillManage findOrder(@Param("minutes") Integer minutes, @Param("orderNo") Long orderNo);

    MbrFundTotal mbrFundsTotal(MbrAccount mbrAccount);

    List<Map> selectRiskControlAudit(MbrAccount mbrAccount);

    List<Map> queryAccountBonusReporList(@Param("accountId") Integer accountId);

    List<MbrBillDetail> queryAccountFundList(@Param("accountId") Integer accountId);

    List<TaskBonus> taskList(@Param("accountId") Integer accountId);

    List<ItemDto> queryAccountAuditInfo(@Param("accountId") Integer accountId,
                                        @Param("keys") String keys,
                                        @Param("item") String item);

    List<MbrMemo> queryAccountMemoList(MbrMemo mbrMemo);

    List<MbrMemo> queryAccountMemoListAll(MbrMemo mbrMemo);

    List<MbrMemo> queryAccountSortMemo(@Param("accountId") Integer accountId);

    String findAccountContact(@Param("userId") Long userId, @Param("perms") String perms);

    int findUserMenuId(@Param("userId") Long userId, @Param("menuId") Long menuId);

    int updateBankCardNameByAccId(@Param("accountId") Integer accountId, @Param("realName") String realName);

    List<Map> findHomePageCount(@Param("startday") String startday);
    List<Map> findHomePageCountEx(@Param("startTime") String startTime, @Param("endTime") String endTime);

    List<SysMenuEntity> findAccountMenuByRoleId(@Param("roleId") Integer roleId);

    List<MbrCollect> findCollectList(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    int findFreeWalletSwitchStatus(Integer accountId);

    List<MbrAccount> findRecommendAccounts(
            @Param("accountId") Integer accountId);

    int addMbrNode(
            @Param("parentId") Integer parentId,
            @Param("childNodeId") Integer childNodeId);

    int updatePromotion();

    int findPromotionCountByAccountId(
            @Param("accountId") Integer accountId);

    Integer findsubAccountParentid(
            @Param("childnodeid") Integer childnodeid);


    Integer getSubAccountId(@Param("accountId") Integer accountId, @Param("subLoginName") String subLoginName);

    int updateSubCagency(@Param("id") Integer id);

    int deleteMbrTreeAccountId(@Param("childnodeid") Integer childnodeid);

    List<MbrRebateReport> findRebateReportList(
            MbrRebateReportNew rebateReport);

    List<MbrRebateReport> rebateContributeReport(
            MbrRebateReportNew rebateReport);

    List<MbrRebate> findAccountRebate(
            @Param("startday") String startday);

    List<MbrAccount> getRebateMbrList(@Param("accountLevel") Integer accountLevel, @Param("startDay") String startDay,
                                      @Param("lowDepth") Integer lowDepth, @Param("highDepth") Integer highDepth);


    List<MbrRebateReportNew>  getSubMbrRptRebateList(@Param("startDay") String startDay, @Param("supAccountId") Integer supAccountId,
                                                    @Param("lowDepth") Integer lowDepth, @Param("highDepth") Integer highDepth);


    List<MbrMessage> findMbrMessageList(MbrMessage mbrMessage);

    List<MbrMessage> findMbrListByName(MbrMessageInfo mbrMessageInfo);
    List<MbrMessage> findMbrListByNameAll(MbrMessageInfo mbrMessageInfo);
    List<MbrMessage> findPushMbrListByName(MbrMessageInfo mbrMessageInfo);
    List<String> findMbrList(MbrMessageInfo mbrMessageInfo);

    Integer getAccountMaxId();

    int findMessageCountByAccountId(MbrMessageInfo mbrMessageInfo);

    int updateMessageList(MbrMessageInfo info);

    int getTagencyIdByName(@Param("loginName") String loginName);

    List<Map> getTagencyIdByNames(Map<String, Object> loginNames);

    String getTagencyNameByName(@Param("loginName") String loginName);

    List<MbrAccount> findExportList(MbrAccount mbrAccount);

    List<CountEntity> messageCountByIsRevert(MbrMessage mbrMessage);

    List<MbrAccount> findAllAccountList(MbrAccount info);

    List<MbrActivityLevel> findActivityLevelList();

    List<MbrAccount> findAccountAndValidbetList(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    List<MbrAccount> findAccountAndLevelList();

    MbrAccount findMbrLevelAndAgyInfoById(@Param("accountId") Integer accountId);

    List<MbrAccount> findMbrLevelAndAgyInfoByIds(@Param("accountIds") List<Integer> accountIds);

    int batchUpdateMbrActLevel(@Param("newLevel") Integer newLevel, @Param("oldLevels") List<Integer> oldLevels,
                               @Param("accountIds") List<Integer> accountIds);
    int batchUpdateMbrActLevelContainLock(@Param("newLevel") Integer newLevel, @Param("oldLevels") List<Integer> oldLevels,
                                          @Param("accountIds") List<Integer> accountIds);

    List<Map<String, Object>> findAccountByActLevelLock(@Param("isLevelLock") Integer isLevelLock);

    List<MbrAccount> findAccountByLevelIds(@Param("oldLevels") List<Integer> oldLevels);

    List<MbrAccount> findAccountByAccIds(@Param("accountIds") List<Integer> accountIds);
    List<MbrAccount> findAccountByAccIdsContainLock(@Param("accountIds") List<Integer> accountIds);

    List<MbrAccount> findAccountOnlineOutOfTime();

    int batchUpdateOnline(@Param("accountIds") List<Integer> ids);

    int batchUpdateLoginOutTime(@Param("accountIds") List<Integer> ids);

    int countGroupAgent(@Param("groupId") Integer groupId);

    int findAccoutnSubCount(@Param("accountId") Integer accountId);

    BigDecimal findValidBetTotalByDepotIds(@Param("accountId") Integer accountId,
                                           @Param("gameCategory") String gameCategory, @Param("depotIds") List<Integer> depotIds
            , @Param("startTime") String startTime, @Param("endTime") String endTime);

    BigDecimal findPayoutTotal(@Param("accountId") Integer accountId,
                               @Param("gameCategory") String gameCategory, @Param("depotIds") List<Integer> depotIds
            , @Param("startTime") String startTime, @Param("endTime") String endTime);

    List<MbrAccount> findSubAccounts(MbrAccount mbrAccount);

    List<MbrAccount> queryAccountMobileEncrypt(
            @Param("mobile") String mobile,
            @Param("mobileEncrypt") List<String> mobileEncrypt,
            @Param("isVerifyMoblie") Integer isVerifyMoblie);

    List<MbrAccount> queryMtAccountMobileEncrypt(
            @Param("mobile") String mobile,
            @Param("mobileEncrypt") List<String> mobileEncrypt,
            @Param("isVerifyMoblie") Integer isVerifyMoblie, @Param("tagencyId") Integer tagencyId);

    List<MbrAccount> getMbrListByLoginNames(@Param("loginNames") List<String> loginNames);

    MbrAccount getMbrByLoginName(@Param("loginName") String loginName);

    List<LogMbrRegister> getRegisterListByLoginNames(@Param("loginNames") List<String> loginNames);
    int addMbrNodeEx(
            @Param("parentId") Integer parentId,
            @Param("childNodeId") Integer childNodeId);
    List<MbrBankcard > getBandcardListByLoginNames(@Param("loginNames") List<String> loginNames);

    List<MbrAccountMobile> accountMobileList(@Param("accountId") Integer accountId);

    List<MbrBankcardHistory> queryBankHistoryListPage(@Param("accountId") Integer accountId);

    MbrAccountMobile queryLastOneByAccount(@Param("accountId") Integer accountId);
    MbrAccountMobile queryByAccountAndUpdateIsNull(@Param("accountId") Integer accountId,@Param("mobile") String mobile);

    Integer getIpAccNum(@Param("ip")String ip);

    Integer getRealNameNum(@Param("realName")String realName);
    Integer getCodeNum(@Param("codeId")Integer codeId);
    List<MbrAccountDevice> getDeviceByAccountIds(@Param("accountIds")List<String> accountIds);
    List<String> getAccountIdsByDevice(@Param("device")String device);

    List<MbrAccount> selectAccountIdsForGroupJob(@Param("thisGroup") MbrGroup thisGroup, @Param("nextGroup") MbrGroup nextGroup, @Param("queryRecent")  Boolean queryRecent);

	int updateManyGroupidForJob(@Param("ids") List<Integer> ids, @Param("groupId") Integer groupId);

	int queryMbrDeviceNum(@Param("loginName") String loginName);
    MbrUseDevice getDeviceByUuid(@Param("loginName")String loginName,@Param("exptime") String exptime,@Param("deviceUuid") String uuid,@Param("valitimes") String valitimes);

    List<CalcRebateFirstChargeDto> findFriendsRebateFirstCharge(@Param("calcDay") String calcDay, @Param("actStartDay") String actStartDay, @Param("actEndDay") String actEndDay);

    List<CalcRebateUpgradeVipDto> findFriendsRebateUpgradeVip(@Param("calcDay") String calcDay, @Param("actStartDay") String actStartDay, @Param("actEndDay") String actEndDay);

    List<CalcRebateValidBetDto> findFriendsRebateValidBet(@Param("calcDay") String calcDay, @Param("actStartDay") String actStartDay, @Param("actEndDay") String actEndDay);

    List<CalcRebateChargeDto> findFriendsRebateCharge(@Param("calcDay") String calcDay, @Param("actStartDay") String actStartDay, @Param("actEndDay") String actEndDay);

    Map findFriendRebateCount(@Param("calcDay") String calcDay, @Param("accountId") Integer accountId);

    Map findFriendRebateChargeCount(@Param("accountId") Integer accountId, @Param("actStartDay") String actStartDay, @Param("actEndDay") String actEndDay, @Param("chargeDay") String chargeDay, @Param("rebateChargeDtoList")List<RebateChargeDto> rebateChargeDtoList);

    List<Integer> getFriendRebateAccountIdList(@Param("calcDay") String calcDay, @Param("actStartDay") String actStartDay, @Param("actEndDay") String actEndDay);

    Integer findAccountLevel(@Param("accountId") Integer accountId);

    List<MbrRebateFriendsReward> getMbrRebateFriendsReward(@Param("accountId")Integer accountId,@Param("type") Integer type, @Param("activityId")Integer activityId);

    Integer isCastFriendRebate(@Param("calcDay")String calcDay, @Param("activityId")Integer activityId);

    Integer getMbrRebateFriendsMaxVip(@Param("accountId")Integer accountId, @Param("subAccountId")Integer subAccountId , @Param("activityId")Integer activityId);

    Integer getLastDaysMbrAccountLog(MbrAccountLog mbrAccountLog);

    Integer deleteMbrRebateFriendsReward(@Param("deleteDay")String deleteDay);

    Integer deleteMbrRebateFriends(@Param("deleteDay")String deleteDay);

    void procCharge(@Param("amount")BigDecimal amount, @Param("accountId")Integer accountId, @Param("chargeTime") String chargeTime);

    void procUpgrade(@Param("accountId")Integer accountId, @Param("createTime") String createTime, @Param("cont") String cont);

    void procValidbet(@Param("userName")String userName, @Param("validbet")BigDecimal validbet, @Param("createTime") String createTime, @Param("startDay") String startDay);

	/**
	 * 	会员的最后投注日（投注大于100的最近一天）
	 * @param loginNames
	 * @return
	 */
	List<MbrAccountLastBetDate> lastBetDate(@Param("loginNames") Set<String> loginNames);

	String getLastDowngradeDays(@Param("accountId") Integer id, @Param("moduleName") String accountAutoDowngrade);
	
	/**
	 * 	根据直属id更新会员组id
	 * 
	 * @param cagencyid
	 * @param setGroupId
	 * @return
	 */
	int updateGroupIdByCagencyid(Integer cagencyid, Integer setGroupId);


   int  updateFreeWalletSwitch(Integer accountId, Integer freeWalletSwitch);


   List<String> selectWarningMbrAccount(String calcDay);


   List<String> selectWarningMbrAccountWithIn(String calcDay, Integer days);

   List<MbrWarningBetInfoDto> selectWarningMbrBetInfo(String calcDay, Integer days, List<String> userNames);


    List<MbrWarningBetInfoDto> selectWarningBigBetInfo(String calcDay, Integer days, List<String> userNames);


    List<Integer> selectWarningDepositMbrAccount(String calcDay, Integer days);


    List<MbrWarningDiscountInfoDto> selectWarningDeposit(String calcDay, Integer days, List<Integer> accountIds);


    List<MbrWarningDiscountInfoDto> selectWarningDiscount(String calcDay, Integer days, List<Integer> accountIds);


    List<MbrLoginWarningInfoDto>  selectLoginByIp(String calcDay, Integer days, Integer times);


    List<MbrLoginWarningInfoDto>  selectLoginByDeviceuuid(String calcDay, Integer days,  Integer times);


    void updateCagencyIdByAccountId(Integer accountId, Integer cagencyId);

    MbrAccount findParentMbrAccount(@Param("accountId") int accountId);

	int updateAdjustment(MbrWallet mbrWallet);

	int updateMbrLabel(Integer id, int defaultId);

    MbrAccount findAccountByLoginName(String loginName);

    List<RelationDto>  checkRelation(String userName, List<String> subUserNames);

    String getFirstBetDay(String userName);

    String getLoginLastIp(Integer accountId);

    List<SubUserDto> findSubUserByParentId(Integer accountId, String startTime, String endTime, String subUserName);


    MbrWallet findWalletForUpdate(@Param("accountId") int accountId);
    
    Integer countOnline();

}
