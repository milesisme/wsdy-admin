package com.wsdy.saasops.modules.operate.mapper;

import java.util.List;

import com.wsdy.saasops.api.modules.apisys.entity.TGmApiprefix;
import com.wsdy.saasops.api.modules.apisys.entity.TcpSiteurl;
import com.wsdy.saasops.api.modules.user.dto.AiRecommondSevenDto;
import com.wsdy.saasops.api.modules.user.dto.ElecGameDto;
import com.wsdy.saasops.modules.member.dto.WaterDepotDto;
import com.wsdy.saasops.modules.operate.dto.DepotListDto;
import com.wsdy.saasops.modules.operate.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface OperateMapper {

    List<OprNotice> selectNoticeList(OprNotice oprNotice);

    List<OprNotice> queryValidListPage();

    TGmGame selectGameOne(Integer id);

    List<TgmCatLabel> findCatLabelList(@Param("depotId") Integer depotId);

    List<TGmDepotcat> findDepotcat(TGmDepotcat tGmDepotcat);

    List<TGmDepotcat> findDepotcatAll();

    List<TGmDepotcat> findCatDepot(@Param("catId") Integer catId);

    List<OprGame> findWebGameList(ElecGameDto elecGameDto);

    List<OprGame> findLotteryGameList(ElecGameDto elecGameDto);

    List<OprGame> findChessGameList(ElecGameDto elecGameDto);

    List<OprGame> findGameList(TGmDepotcat tGm);

    List<OprGame> findlabelGameList(ElecGameDto elecGameDto);

    List<OprGame> findFishGameList(ElecGameDto elecGameDto);

    List<DepotListDto> gameAllList(@Param("siteId") Integer siteId);

    List<DepotListDto> gameRecommendedDepotList(@Param("siteId") Integer siteId);

    List<OprGame> gameAllList1(@Param("depotId") int depotId, @Param("pageNumber") int pageNumber, @Param("terminal") int terminal);

    List<OprGame> findCatGameList(@Param("depotId") Integer depotId, @Param("catId") Integer catId);

    List<TcpSiteurl> siteUrlList(@Param("siteId") Integer siteId);

    List<TGmDepot> siteDepotList(@Param("siteCode") String siteCode);

    int updateGmClickNum(@Param("gameId") Integer gameId);

    List<TGmDepot> findelecDepotList(@Param("terminal") Byte terminal, @Param("siteCode") String siteCode);

    List<TGmDepot> findChessDepotList(@Param("terminal") Byte terminal, @Param("siteCode") String siteCode);

    List<TGmDepot> findLotteryDepotList(@Param("terminal") Byte terminal, @Param("siteCode") String siteCode);

    List<TGmDepot> findSiteTGmDepotList(@Param("siteCode") String siteCode);

    List<TGmCat> findelecCatList(@Param("terminal") Byte terminal, @Param("siteCode") String siteCode);

    List<TGmDepot> catchFishDepoList(@Param("terminal") Byte terminal, @Param("siteCode") String siteCode);

    List<TGmCat> queryCatList(TGmDepot tGmDepot);

    List<TGmGame> queryTGmGameList(TGmGame tGmGame);

    List<OprNotice> queryNoticeList(@Param("showType") String showType);

    List<TGmDepot> findDepotBalanceList(@Param("accountId") Integer accountId);

    List<TGmDepot> findDepotList(@Param("accountId") Integer accountId, @Param("terminal") Byte terminal, @Param("siteCode") String siteCode);

    List<TGmDepot> findCatOrDepotListBySiteCode(@Param("isSign") Boolean isSign, @Param("siteCode") String siteCode);

    int insertRecentlyRecord(TRecentlyGame tRecentlyGame);

    int updateRecentlyRecord(@Param("entryTime") String entryTime, @Param("gameId") Integer gameId, @Param("userId") Integer userId);

    List<TRecentlyGame> queryTRecentlyGameList(@Param("gameId") String gameId, @Param("userId") Integer userId);

    List<TRecentlyGame> findRecentlyRecord(@Param("userId") Integer userId);

    List<OprGame> hotGameList(@Param("pageNumber") int pageNumber, @Param("terminal") int terminal, @Param("siteCode") String siteCode);

    OprGame gameRecommendedList(@Param("depotId") int depotId, @Param("pageNumber") int pageNumber, @Param("terminal") int terminal);

    List<WaterDepotDto> findDepotAndCatNameList(@Param("siteCode") String siteCode);

    Integer findDepotIstryCount(ElecGameDto elecGameDto);


    List<TGmApiprefix> getTGmApiprefix(@Param("depotId") Integer depotId, @Param("siteCode") String siteCode);

	List<OprGame> getGameByTrunmanShowCategory(ElecGameDto elecGameDto);
    List<AiRecommondSevenDto> getAiRecommendSeven(@Param("userId")Integer userId,@Param("time")String time);
}
