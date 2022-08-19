package com.wsdy.saasops.modules.analysis.mapper;

import com.wsdy.saasops.modules.analysis.dto.DepositOrBetDailyDto;
import com.wsdy.saasops.modules.analysis.dto.RetentionRatePlayerDto;
import com.wsdy.saasops.modules.analysis.dto.RetentionRateResultDto;
import com.wsdy.saasops.modules.analysis.dto.RetentionRateUserResultDto;
import com.wsdy.saasops.modules.analysis.entity.*;
import com.wsdy.saasops.modules.base.entity.GmDepot;
import com.wsdy.saasops.modules.member.entity.MbrAccountOther;
import com.wsdy.saasops.modules.operate.entity.TGmDepotcat;
import com.wsdy.saasops.modules.operate.entity.TGmGame;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface AnalysisMapper {

    List<TGmGame> getGameList(@Param("platFormNames") List<String> platFormNames);

    List<SelectModel> getPlatForm(@Param("siteCode") String siteCode);


    List<SelectModel> getPlatFormByCatCode(@Param("siteCode") String siteCode, @Param("catCodeList") List<String> catCodeList);


    List<SelectModel> getPlatFormWithOrder(@Param("siteCode") String siteCode);

    List<SelectModel> getGameType(@Param("platFormId") String platFormId, @Param("parentId") Integer parentId, @Param("siteCode") String siteCode);

    List<TGmGame> getGameCodeByCat(@Param("depotName") String depotName, @Param("catId") String catId, @Param("subCatId") String SubCatId);

    List<Map> getAgentAccount();

    List<RptBetDayModel> getRptBetDay(@Param("parentAgentid") Integer parentAgentid, @Param("agentid") Integer agentid, @Param("groupid") Integer groupid,
                                      @Param("loginName") String loginName, @Param("platform") String platform, @Param("gametype") String gametype,
                                      @Param("betStrTime") String betStrTime, @Param("betEndTime") String betEndTime, @Param("groups") String group, @Param("group_head") String group_head);

    List<FundStatisticsModel> getFundStatistics(@Param("parentAgentid") Integer parentAgentid, @Param("agentid") Integer agentid, @Param("groupid") Integer groupid,
                                                @Param("loginName") String loginName, @Param("platform") String platform, @Param("gametype") String gametype,
                                                @Param("betStrTime") String betStrTime, @Param("betEndTime") String betEndTime);

    FundReportModel getFundReport(@Param("parentAgentid") Integer parentAgentid, @Param("agentid") Integer agentid, @Param("groupid") Integer groupid,
                                  @Param("betStrTime") String betStrTime, @Param("betEndTime") String betEndTime);

    List<String> getApiPrefixBySiteCode(@Param("siteCode") String siteCode);

    List<RptWinLostModel> getRptWinLostList(WinLostReportModel model);

    RptWinLostModel getRptWinLostTotal(WinLostReportModel model);

    List<RptWinLostModel> getRptWinLostGroup(WinLostReportModel model);

    List<RptWinLostModel> getRptWinLostGroupAgent(WinLostReportModel model);

    List<RptWinLostModel> getRptWinLostGroupUser(WinLostReportModel model);

    /***
     * 查询输赢报表总计，根据会员 会员组进行分组
     * @return
     */
    RptWinLostModel getRptWinLostGroupUserTotal(WinLostReportModel model);

    /***
     * 根据时间获取红利，按天聚合
     * @return
     */
    List<RptWinLostModel> getBonusReportList(BounsReportQueryModel model);

    /***
     * 根据时间获取红利，按代理聚合
     * @return
     */
    List<RptWinLostModel> getBonusGroupTopAgentReportList(BounsReportQueryModel model);

    List<RptWinLostModel> getBonusGroupAgentReportList(BounsReportQueryModel model);

    List<RptWinLostModel> getBonusGroupUserReportList(BounsReportQueryModel model);

    List<RptWinLostModel> getBonusGroupUserTotal(BounsReportQueryModel model);

    RptWinLostModel getBonusReportListTotal(BounsReportQueryModel model);

    List<TransactionModel> getBonusList(BounsReportQueryModel model);

    TransactionModel getBonusListTotal(BounsReportQueryModel model);

    List<RptBetTotalModel> getRptBetTotalList(GameReportModel model);

    RptBetTotalModel getRptBetTotals(GameReportModel model);

    List<RptBetTotalModel> getBetDayGroupGameTypeList(GameReportModel model);

    List<RptBetTotalModel> getBetDayGroupTopAgentList(GameReportModel model);

    List<RptBetTotalModel> getBetDayGroupAgentList(GameReportModel model);

    List<RptBetTotalModel> getBetDayGroupUserList(GameReportModel model);

    RptBetTotalModel getBetDayByAgentTotal(GameReportModel model);

    List<RptMemberModel> getRptMemberList(@Param("lmt") Integer limit, @Param("formate") String formate, @Param("type") String type);
    List<RptMemberModel> getRptMemberListEx( @Param("formate") String formate, @Param("startTime") String startTime, @Param("endTime") String endTime);

    Integer getRegisterCounts(@Param("date") String date);
    Integer getRegisterCountsByMonth(@Param("date") String date);

    List<WinLostReport> findWinLostList(WinLostReport winLostReport);

    List<WinLostReport> findWinLostListOfTagency(WinLostReport winLostReport);

    List<WinLostReport> findWinLostListByTagencyId(WinLostReport winLostReport);

    List<WinLostReport> findWinLostListByCagencyId(WinLostReport winLostReport);


    List<SelectModel> getDepot(@Param("siteCode") String siteCode);

    Integer getValidBetAccountCounts(WinLostReport winLostReport);

    List<SelectModel> getGameCat(String depotId);

    List<SelectModel> getSubGameCat(@Param("depotId") String depotId, @Param("catId") String catId);

    String getDepotName(Integer depotId);

    String getCatName(Integer catId);

    String getGameCatName(@Param("depotCode") String depotCode, @Param("gameCode") String gameCode);

    String getDepotNameToDepotCode(@Param("depotName") String depotName);

    // 2019.5.15
    List<GmDepot>  getAllDepotCodeToDepotName(Map<String, Object> depotCodeMap);

    int findAccountRptCount(@Param("username") String username);


    List<RetentionRatePlayerDto> getRetentionRatePlayer(String startTime, String endTime, String agentName);


    List<RetentionRateResultDto> getRetentionRateResult(String startTime, String endTime,  List<Integer> retentionRateAccountIdList);


    List<RetentionRatePlayerDto> getRetentionRateDailyActiveReport(String startTime, String endTime, String userName, String agentName);

    List<DepositOrBetDailyDto> getUserDailyDepositAmount(String startTime, String endTime, List<String> userNames);

    List<DepositOrBetDailyDto> getUserDailyBetAmount(String startTime, String endTime, List<String> userNames);

    List<String> getBetPointUsernameByDate(@Param("startTime")String startTime, @Param("endTime")String endTime);

    List<MbrAccountOther> getBetPointByUsername(@Param("usernames")List<String> usernames);
}
