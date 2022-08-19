package com.wsdy.saasops.modules.operate.mapper;

import com.wsdy.saasops.api.modules.apisys.entity.TGmApiprefix;
import com.wsdy.saasops.modules.api.dto.DepotCatDto;
import com.wsdy.saasops.modules.operate.dto.DepotRedLimitDto;
import com.wsdy.saasops.modules.operate.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface GameMapper {
    /**
     * 根据条件查询游戏平台列表
     *
     * @param tGmDepot
     * @return
     */
    List<TGmDepot> listTGmDepot(TGmDepot tGmDepot);

    /**
     * 保存游戏开关
     *
     * @param setGame
     * @return
     */
    int saveOrUpdataSetGame(SetGame setGame);

    /**
     * 根据条件查询游戏平台
     *
     * @return
     */
    List<TGmDepot> findCatGameDepot(@Param("catId") Integer catId, @Param("siteCode") String siteCode);

    /**
     * 保存游戏平台开关
     *
     * @param setGmGame
     * @return
     */
    int saveOrUpdataSetGmGame(SetGmGame setGmGame);

    /**
     * 根据条件查询游戏分類
     *
     * @return
     */
    List<TGmCat> findGameType();

    /**
     * 根据条件查询游戏分類
     *
     * @return
     */
    List<TGmCat> findGameCatCode();


    /**
     * 根据条件查询子分类
     *
     * @return
     */
    List<TGmCat> findSubCat();

    /**
     * 根据条件查询游戏分类列表
     *
     * @param tGmCat
     * @return
     */
    List<TGmCat> listTGmCat(TGmCat tGmCat);

    /**
     * 根据条件查询游戏列表及统计
     *
     * @param tGmGame
     * @return
     */
    List<TGmGame> listTGmGame(TGmGame tGmGame);

    /**
     * 根据条件查询游戏列表(由于多个分类，故多连了几张表)
     *
     * @param tGameLogo
     * @return
     */
    List<TGameLogo> listtGameLogo(TGameLogo tGameLogo);

    /**
     * 查询API线路状态
     *
     * @param depotId
     * @param siteCode
     * @return
     */
   TGmApiprefix findAvailable(@Param("depotId") Integer depotId, @Param("siteCode") String siteCode);

    /**
     * 根据条件查询游戏列表(专供V2后台用)
     *
     * @param tGameLogo
     * @return
     */
    List<TGameLogo> listtGameLogos(TGameLogo tGameLogo);

    /**
     * 根据条件查询游戏列表
     *
     * @param tGmGame
     * @return
     */
    List<TGmGame> findGameList(TGmGame tGmGame);

    BigDecimal maxGameWaterrate(@Param("depotId") Integer depotId, @Param("catId") Integer catId);

    BigDecimal minGameWaterrate(@Param("depotId") Integer depotId, @Param("catId") Integer catId);


    /**
     * 根据条件查询各个站点下的平台
     *
     * @param siteCode
     * @return
     */
    List<DepotRedLimitDto> findDepotList(String siteCode);

    /**
     * @return
     */
    List<TGameLogo> listInfo();

    /**
     * 根据条件查询游戏平台
     *
     * @return
     */
    List<TGmDepot> findGameDepot();

    /**
     * 查询分类下各个平台的游戏总数
     *
     * @param depotId
     * @return
     */
    Integer findTGmGameNums(@Param("depotId") Integer depotId, @Param("catId") Integer catId);

    /**
     * 查询分类下各个平台打开游戏总数
     *
     * @param depotId
     * @return
     */
    Integer findTGmGameOpenNums(@Param("depotId") Integer depotId, @Param("catId") Integer catId);

    /**
     * 查询平台配置
     *
     * @param depotId
     * @return
     */
    TGameLogo selectGameLogoById(@Param("depotId") Integer depotId, @Param("gameLogoId") Integer gameLogoId);


    List<DepotCatDto> getCategoryAndDepotRelation(@Param("siteCode") String siteCode);


    List<TGmGame> getGameListWithoutRebate(@Param("siteCode") String siteCode);

    List<String> getDepotCodesByIds(@Param("ids") List<Integer> ids);

	List<TGmGame> findGameHasSubcatList(TGmGame tGmGame);
}
