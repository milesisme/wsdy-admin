package com.wsdy.saasops.modules.operate.service;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.user.dto.ElecGameDto;
import com.wsdy.saasops.api.modules.user.dto.GameWithoutRebateDto;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.api.dto.DepotCatDto;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.operate.dao.TGmGameMapper;
import com.wsdy.saasops.modules.operate.dto.DepotListDto;
import com.wsdy.saasops.modules.operate.dto.DepotRedLimitDto;
import com.wsdy.saasops.modules.operate.entity.*;
import com.wsdy.saasops.modules.operate.mapper.GameMapper;
import com.wsdy.saasops.modules.operate.mapper.OperateMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TGmGameService extends BaseService<TGmGameMapper, TGmGame> {

    @Autowired
    private OperateMapper operateMapper;
    @Value("${game.cat.excel.path}")
    private String gameCatExcelPath;
    @Value("${game.excel.path}")
    private String gameExcelPath;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private TGmGameMapper tGmGameMapper;

    public PageUtils queryWebListPage(ElecGameDto elecGameDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        if (StringUtil.isEmpty(elecGameDto.getShowType()) || "1".equals(elecGameDto.getShowType())) {
            return BeanUtil.toPagedResult(operateMapper.findWebGameList(elecGameDto));
        } else {    // 下述标签逻辑sdy作废
            return BeanUtil.toPagedResult(operateMapper.findlabelGameList(elecGameDto));
        }
    }

    public PageUtils queryLotteryListPage(ElecGameDto elecGameDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return BeanUtil.toPagedResult(operateMapper.findLotteryGameList(elecGameDto));
    }

    public PageUtils queryChessListPage(ElecGameDto elecGameDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return BeanUtil.toPagedResult(operateMapper.findChessGameList(elecGameDto));
    }

    public PageUtils queryFishListPage(ElecGameDto elecGameDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return BeanUtil.toPagedResult(operateMapper.findFishGameList(elecGameDto));

    }

    public Object gameAllList(int pageNumber, Byte terminal, Integer siteId) {
        List<DepotListDto> depotLists = operateMapper.gameAllList(siteId);
        for (DepotListDto depotListDto : depotLists) {
            //TODO 查询单个平台的几条数据
            List<OprGame> oprGames = operateMapper.gameAllList1(depotListDto.getDepotId(), pageNumber, terminal);
            depotListDto.setDepotGameList(oprGames);
        }
        return depotLists;
    }

    public Object gameHotGameList(int pageNumber, Byte terminal, String siteCode) {
        List<OprGame> oprGames = operateMapper.hotGameList(pageNumber, terminal, siteCode);
        return oprGames;
    }

    public Object gameRecommendedList(int pageNumber, Byte terminal, Integer siteId) {
        List<DepotListDto> depotLists = operateMapper.gameRecommendedDepotList(siteId);
        List<OprGame> resultList = new ArrayList<>();
        for (DepotListDto depotListDto : depotLists) {
            //TODO 查询单个平台的几条数据
            OprGame gameInfo = operateMapper.gameRecommendedList(depotListDto.getDepotId(), pageNumber, terminal);
            if (!Objects.isNull(gameInfo)) {
                resultList.add(gameInfo);
            }
        }
        return resultList;
    }

    public Object recentlyGameList(Integer userId) {
        List<TRecentlyGame> gameLists = operateMapper.findRecentlyRecord(userId);
        return gameLists;
    }

    public PageUtils queryCatGameList(Integer depotId, Integer catId) {
        PageHelper.startPage(1, 100);
        return BeanUtil.toPagedResult(operateMapper.findCatGameList(depotId, catId));
    }

    public Object siteUrlList(Integer siteId) {
        return operateMapper.siteUrlList(siteId);
    }

    public List<TGmDepot> siteDepotList(Integer pageNo, Integer pageSize, String siteCode) {
        PageHelper.startPage(pageNo, pageSize);
        return operateMapper.siteDepotList(siteCode);
    }

    public TGmGame queryObjectOne(Integer key) {
        return operateMapper.selectGameOne(key);
    }

    @Cacheable(cacheNames = ApiConstants.REDIS_GAME_COMPANY_CACHE_KEY, key = "#siteCode+'_'+#gameId")
    public TGmGame queryObjectOne(Integer gameId, String siteCode) {
        return operateMapper.selectGameOne(gameId);
    }

    @CachePut(cacheNames = ApiConstants.REDIS_GAME_COMPANY_CACHE_KEY, key = "#siteCode+'_'+#gameId")
    public TGmGame updateClickNum(Integer gameId, String siteCode) {
        operateMapper.updateGmClickNum(gameId);
        return queryObject(gameId);
    }

    public Object queryTGmGameList(TGmGame tGmGame, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<TGmGame> tGmGameList = operateMapper.queryTGmGameList(tGmGame);
        return BeanUtil.toPagedResult(tGmGameList);
    }

    public void exportGameExcel(TGmGame tGmGame, HttpServletResponse response) {
        String fileName = "游戏列表" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        List<Map<String, Object>> list = Lists.newLinkedList();
        List<TGmGame> tGmGameList = operateMapper.queryTGmGameList(tGmGame);
        tGmGameList.stream().forEach(cs -> {
            Map<String, Object> param = new HashMap<>(16);
            param.put("catName", cs.getCatName());
            param.put("depotName", cs.getDepotName());
            param.put("gameTag", cs.getGameTag());
            param.put("memo", cs.getMemo());
            param.put("enablePc", cs.getEnablePc() == 1 ? "启用" : "禁用");
            param.put("enableMb", cs.getEnableMb() == 1 ? "启用" : "禁用");
            param.put("enableTest", cs.getEnableTest() == 1 ? "启用" : "禁用");
            param.put("monthPer", cs.getMonthPer());
            param.put("lastdayPer", cs.getLastdayPer());
            param.put("available", cs.getAvailable() == 1 ? "启用" : "禁用");
            list.add(param);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", gameExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            log.error("error:" + e);
        }
    }

    public Object queryGmCatList(TGmDepot tGmDepot, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<TGmCat> gameCatList = operateMapper.queryCatList(tGmDepot);
        return BeanUtil.toPagedResult(gameCatList);
    }

    public void exportGameCatExcel(TGmDepot tGmDepot, HttpServletResponse response) {
        String fileName = "游戏分类列表" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        List<Map<String, Object>> list = Lists.newLinkedList();
        List<TGmCat> gameCatList = operateMapper.queryCatList(tGmDepot);
        gameCatList.stream().forEach(cs -> {
            Map<String, Object> param = new HashMap<>(4);
            param.put("catName", cs.getCatName());
            param.put("gameCount", cs.getGameCount());
            param.put("tMonthPer", cs.getTMonthPer());
            param.put("tLastdayPer", cs.getTLastdayPer());
            list.add(param);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", gameCatExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            log.error("error:" + e);
        }
    }

    public List<TGmCat> findGameType() {
        return gameMapper.findGameType();
    }

    public List<TGmCat> findGameCatCode() {
        return gameMapper.findGameCatCode();
    }

    public List<TGmDepot> findCatGameDepot(Integer catId, String siteCode) {
        return gameMapper.findCatGameDepot(catId, siteCode);
    }

    public PageUtils queryListGamePage(TGameLogo tGameLogo, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TGameLogo> list = gameMapper.listtGameLogos(tGameLogo);
        list.stream().forEach(e -> {
            tGameLogo.setDepotId(e.getDepotId());
            Integer nums = gameMapper.findTGmGameNums(tGameLogo.getDepotId(), tGameLogo.getCatId());
            Integer openNums = gameMapper.findTGmGameOpenNums(tGameLogo.getDepotId(), tGameLogo.getCatId());
            e.setGameCount(openNums + "/" + nums);
            if (e.getEnablePc() == 0) {
                e.setEnableDepotPc(0);
            }
            if (e.getEnableMb() == 0) {
                e.setEnableDepotMb(0);
            }
            if (e.getEnableApp() == 0) {
                e.setEnableDepotApp(0);
            }
            if (e.getEnablePc() == 1 && Objects.isNull(e.getEnableDepotPc())) {
                e.setEnableDepotPc(1);
            }
            if (e.getEnableMb() == 1 && Objects.isNull(e.getEnableDepotMb())) {
                e.setEnableDepotMb(1);
            }
            if (e.getEnableApp() == 1 && Objects.isNull(e.getEnableDepotApp())) {
                e.setEnableDepotApp(1);
            }
            BigDecimal maxwaterrate = gameMapper.maxGameWaterrate(e.getDepotId(), e.getCatId());
            BigDecimal minwaterrate = gameMapper.minGameWaterrate(e.getDepotId(), e.getCatId());
            if (Objects.nonNull(maxwaterrate) || Objects.nonNull(minwaterrate)) {
                List<BigDecimal> bigDecimals = Lists.newArrayList();
                if (Objects.nonNull(e.getWaterrate())) {
                    bigDecimals.add(e.getWaterrate());
                }
                if (Objects.nonNull(maxwaterrate)) {
                    bigDecimals.add(maxwaterrate);
                }
                if (Objects.nonNull(minwaterrate)) {
                    bigDecimals.add(minwaterrate);
                }
                e.setWaterrate(null);
                BigDecimal max = bigDecimals.stream().max((x1, x2) -> x1.compareTo(x2)).get();
                BigDecimal min = bigDecimals.stream().min((x1, x2) -> x1.compareTo(x2)).get();
                if (max.compareTo(min) == 0) {
                    e.setStrWaterrate(String.valueOf(max)+"%");
                } else {
                    e.setStrWaterrate(min+"%" + "~" + max+"%");
                }
            } else {
                if (Objects.nonNull(e.getWaterrate())) {
                    e.setStrWaterrate(e.getWaterrate().stripTrailingZeros().toString()+"%");
                }
            }
        });
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findGameList(TGmGame tGmGame, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TGmGame> list = gameMapper.findGameList(tGmGame);
        list.stream().forEach(e -> {
            if (e.getEnableMbTem() == 0) {
                e.setEnableGmaeMb(0);
            }
            if (e.getEnablePcTem() == 0) {
                e.setEnableGmaePc(0);
            }
            if (e.getEnableAppTem() == 0) {
                e.setEnableGmaeApp(0);
            }
            if (e.getEnablePcTem() == 1 && Objects.isNull(e.getEnableGmaePc())) {
                e.setEnableGmaePc(1);
            }
            if (e.getEnableMbTem() == 1 && Objects.isNull(e.getEnableGmaeMb())) {
                e.setEnableGmaeMb(1);
            }
            if (e.getEnableAppTem() == 1 && Objects.isNull(e.getEnableGmaeApp())) {
                e.setEnableGmaeApp(1);
            }
            if (Objects.nonNull(e.getWaterrate())) {
                e.setStrWaterrate(e.getWaterrate().stripTrailingZeros().toString()+"%");
            }
        });
        return BeanUtil.toPagedResult(list);
    }
    
    /**
     * 	游戏中有分类的，即：subcatid不为空
     * 
     * @param tGmGame
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils findGameHasSubcatList(TGmGame tGmGame, Integer pageNo, Integer pageSize) {
    	PageHelper.startPage(pageNo, pageSize);
    	List<TGmGame> list = gameMapper.findGameHasSubcatList(tGmGame);
    	list.stream().forEach(e -> {
    		if (e.getEnableMbTem() == 0) {
    			e.setEnableGmaeMb(0);
    		}
    		if (e.getEnablePcTem() == 0) {
    			e.setEnableGmaePc(0);
    		}
    		if (e.getEnableAppTem() == 0) {
    			e.setEnableGmaeApp(0);
    		}
    		if (e.getEnablePcTem() == 1 && Objects.isNull(e.getEnableGmaePc())) {
    			e.setEnableGmaePc(1);
    		}
    		if (e.getEnableMbTem() == 1 && Objects.isNull(e.getEnableGmaeMb())) {
    			e.setEnableGmaeMb(1);
    		}
    		if (e.getEnableAppTem() == 1 && Objects.isNull(e.getEnableGmaeApp())) {
    			e.setEnableGmaeApp(1);
    		}
    		if (Objects.nonNull(e.getWaterrate())) {
    			e.setStrWaterrate(e.getWaterrate().stripTrailingZeros().toString()+"%");
    		}
    	});
    	return BeanUtil.toPagedResult(list);
    }

    public List<TGmGame> findGameListByBiz(TGmGame tGmGame) {
        return gameMapper.findGameList(tGmGame);
    }


    public PageUtils findDepotList(String siteCode, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<DepotRedLimitDto> list = gameMapper.findDepotList(siteCode);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils listDepotGame(TGmGame tGmGame, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return BeanUtil.toPagedResult(tGmGameMapper.select(tGmGame));
    }

    @Override
    public int update(TGmGame tGmGame) {
        SetGame setGame = new SetGame();
        setGame.setDepotId(tGmGame.getDepotId());
        setGame.setGameId(tGmGame.getId());
        setGame.setMemo(tGmGame.getMemoGmae());
        setGame.setPopularity(tGmGame.getPopularityGame());
        setGame.setEnableGmaePc(tGmGame.getEnableGmaePc());
        setGame.setEnableGmaeMb(tGmGame.getEnableGmaeMb());
        setGame.setEnableGmaeApp(tGmGame.getEnableGmaeApp());
        setGame.setWaterrate(tGmGame.getWaterrate());
        return gameMapper.saveOrUpdataSetGame(setGame);
    }

    public int updateByBiz(TGmGame tGmGame) {
        SetGame setGame = new SetGame();
        setGame.setDepotId(tGmGame.getDepotId());
        setGame.setGameId(tGmGame.getId());
        setGame.setMemo(tGmGame.getMemoGmae());
        setGame.setPopularity(tGmGame.getPopularityGame());
        setGame.setEnableGmaePc(Constants.EVNumber.zero);
        setGame.setEnableGmaeMb(Constants.EVNumber.zero);
        setGame.setEnableGmaeApp(Constants.EVNumber.zero);
        return gameMapper.saveOrUpdataSetGame(setGame);
    }

    /**
     * 获取游戏类别和平台数据
     *
     * @param siteCode
     * @param flag     0 仅查询6大类，1 查询所有parentid 为0 的分类
     * @return
     */
    public List<DepotCatDto> getCategoryAndDepotRelation(String siteCode, int flag) {
        List<DepotCatDto> list = gameMapper.getCategoryAndDepotRelation(siteCode);
        return list;
    }

    public List<GameWithoutRebateDto> getGameListWithoutRebate() {
        String siteCode = CommonUtil.getSiteCode();
        List<GameWithoutRebateDto> ret = new ArrayList<>();
        List<TGmGame> list = gameMapper.getGameListWithoutRebate(siteCode);
        if (Objects.isNull(list) || list.size() == Constants.EVNumber.zero) {
            return ret;
        }
        // 分组处理
        Map<String, List<TGmGame>> groupingBy =
                list.stream().collect(
                        Collectors.groupingBy(
                                TGmGame::getDepotName));
        // 处理成返回值
        for (String depotName : groupingBy.keySet()) {
            List<TGmGame> tgm = groupingBy.get(depotName);
            GameWithoutRebateDto dto = new GameWithoutRebateDto();
            dto.setDepotName(depotName);
            dto.setGameList(tgm);
            ret.add(dto);
        }
        return ret;
    }

    /**
     * 获取catId和catCode的map,转化catId 到gameCategory
     *
     * @return
     */
    public Map<Integer, String> getCatCodeMap() {
        TGmCat tGmCat = new TGmCat();
        tGmCat.setAvailable(Integer.valueOf(Constants.EVNumber.one).byteValue());
        tGmCat.setParentId(Constants.EVNumber.zero);
        List<TGmCat> tGmCats = gameMapper.listTGmCat(tGmCat);
        // 获取catid与catCode map
        Map<Integer, String> catMap = new HashMap<>();
        for (TGmCat cat : tGmCats) {
            if (Objects.nonNull(cat.getId()) && StringUtil.isNotEmpty(cat.getCatCode())) {
                catMap.put(cat.getId(), cat.getCatCode());
            }
        }
        return catMap;
    }

	/**
	 * 	查询真人游戏，根据分类id
	 * 
	 * @param elecGameDto
	 * @param pageSize 
	 * @param pageNo 
	 * @return		
	 */
	public PageUtils getGameByTrunmanShowCategory(ElecGameDto elecGameDto, @NotNull Integer pageNo, @NotNull Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
        return BeanUtil.toPagedResult(operateMapper.getGameByTrunmanShowCategory(elecGameDto));
	}
}
