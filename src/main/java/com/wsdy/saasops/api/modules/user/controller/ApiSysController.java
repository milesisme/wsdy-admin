package com.wsdy.saasops.api.modules.user.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.api.modules.transferNew.service.GatewayDepotService;
import com.wsdy.saasops.api.modules.unity.dto.PlayGameModel;
import com.wsdy.saasops.api.modules.user.dto.ElecGameDto;
import com.wsdy.saasops.api.modules.user.service.ApiSysService;
import com.wsdy.saasops.api.modules.user.service.PtService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.GameTypeEnum;
import com.wsdy.saasops.api.utils.HttpsRequestUtil;
import com.wsdy.saasops.common.constants.AdvConstant;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.base.entity.BaseArea;
import com.wsdy.saasops.modules.base.entity.ToprAdv;
import com.wsdy.saasops.modules.base.service.BaseAreaService;
import com.wsdy.saasops.modules.base.service.BaseBankService;
import com.wsdy.saasops.modules.base.service.TWinTopService;
import com.wsdy.saasops.modules.base.service.ToprAdvService;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.operate.dao.TGmDepotMapper;
import com.wsdy.saasops.modules.operate.dao.TGmGameMapper;
import com.wsdy.saasops.modules.operate.entity.AdvBanner;
import com.wsdy.saasops.modules.operate.entity.SetGameCategory;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.entity.TGmGame;
import com.wsdy.saasops.modules.operate.service.*;
import com.wsdy.saasops.modules.system.systemsetting.dto.StationSet;
import com.wsdy.saasops.modules.system.systemsetting.dto.SysWebTerms;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * ???????????????????????????????????????????????????
 */
@Slf4j
@RestController
@RequestMapping("/api/sys")
@Api(value = "api/sys", tags = "?????????????????????????????????")
public class ApiSysController {
	
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private TGmCatService tGmCatService;
    @Autowired
    private TGmGameService tGmGameService;
    @Autowired
    private ApiSysService apiSysService;
    @Autowired
    private BaseBankService baseBankService;
    @Autowired
    private BaseAreaService baseAreaService;
    @Autowired
    private OprNoticeService oprNoticeService;
    @Autowired
    private PtService ptService;
    @Autowired
    private TWinTopService tWinTopService;
    @Autowired
    private OprActActivityService oprActActivityService;
    @Autowired
    private OprActCatService oprActCatService;
    @Autowired
    private OprAdvService oprAdvService;
    @Autowired
    private ToprAdvService toprAdvService;
    @Autowired
    private TCpSiteService tCpSiteService;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private TGmGameMapper tGmGameMapper;
    @Autowired
    private TGmDepotMapper tGmDepotMapper;
    @Autowired
    MbrAccountService mbrAccountService;
    @Autowired
    private GatewayDepotService gatewayDepotService;
	@Autowired
	private SetGameCategoryService setGameCategoryService;
	@Autowired
	private HelpGuessAskService helpGuessAskService;


    @GetMapping(value = "/getSiteCode")
    @ApiOperation(value = "????????????", notes = "")
    public R getSitePre(@RequestParam("url") String url) {
        TCpSite tCpSite = tCpSiteService.queryOneCond(url);
        try {
            return R.ok().put("SToken", AESUtil.encrypt(tCpSite.getSiteCode())).put("isI18n", tCpSite.getIsI18n()).put("language", tCpSite.getLanguage());
        } catch (Exception e) {
            log.error("error:" + e);
        }
        return null;
    }

    @GetMapping(value = "/getsitecode")
    @ApiOperation(value = "????????????", notes = "")
    public R getSitePreEx(@RequestParam("url") String url) {
        TCpSite tCpSite = tCpSiteService.queryOneCond(url);
        try {
            return R.ok().put("SToken", AESUtil.encrypt(tCpSite.getSiteCode())).put("isI18n", tCpSite.getIsI18n()).put("language", tCpSite.getLanguage());
        } catch (Exception e) {
            log.error("error:" + e);
        }
        return null;
    }

    @GetMapping(value = "/findSiteCode")
    @ApiOperation(value = "???????????????", notes = "???????????????")
    public R findSiteCode(@RequestParam("url") String url) {
        TCpSite tCpSite = tCpSiteService.queryOneCond(url);
        return R.ok().put("siteCode", tCpSite.getSiteCode());
    }


    @Login
    @GetMapping("/transit")
    @ApiOperation(value = "??????????????????", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R transit(@ModelAttribute BillRequestDto requestDto, HttpServletRequest request) {
        // ???????????????gameId
        Assert.isNull(requestDto.getGameId(), "??????Id????????????!");

        // ???????????????userId/loginName/cpSite
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);

        log.info("userName==" + loginName + "==gameid==" + requestDto.getGameId());

        // requestDto????????????
        requestDto.setAccountId(userId);                            // ??????id
        requestDto.setLoginName(loginName);                         // ?????????
        requestDto.setIp(CommonUtil.getIpAddress(request));         // ip
        String dev = getTransferSourceDev(requestDto, request);
        requestDto.setDev(dev);                                     // dev???0 PC???3 H5
        TGmGame tGmGame = tGmGameMapper.selectByPrimaryKey(requestDto.getGameId());
        requestDto.setDepotId(tGmGame.getDepotId());                // ??????id
        if (StringUtils.isEmpty(requestDto.getTerminal())) {
            requestDto.setTerminal(ApiConstants.Terminal.pc);       // terminal ?????? 0:pc??? 1:mobile???
        }

        // ??????????????????????????????
        // 1.????????????????????????
        List<TGmDepot> tGmDepotList = tGmDepotService.findDepotList(userId, requestDto.getTerminal(), cpSite.getSiteCode(), Constants.EVNumber.zero);
        // 2.?????????????????????????????????
        List<Byte> listAvailableWh = tGmDepotList.stream().filter(ls -> ls.getId().equals(tGmGame.getDepotId()))
                .map(TGmDepot::getAvailableWh).collect(Collectors.toList());
        // 3.???????????????????????????
        List<String> listDepotName = tGmDepotList.stream().filter(ls -> ls.getId().equals(tGmGame.getDepotId()))
                .map(TGmDepot::getDepotName).collect(Collectors.toList());
        // 4.????????????????????????????????????
        if (Collections3.isNotEmpty(listAvailableWh) && listAvailableWh.get(0).byteValue() == Constants.EVNumber.two) {
            throw new R200Exception("??????" + listDepotName.get(0) + "??????????????????????????????????????????" + listDepotName.get(0) + "??????????????????????????????????????????");
        }
        if (Collections3.isEmpty(listAvailableWh)) {
            throw new R200Exception("???????????????");
        }

        // ????????????
        String key = RedisConstants.ACCOUNT_DEPOT_TRANSFER + CommonUtil.getSiteCode() + userId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, userId, 50, TimeUnit.SECONDS);
        if (isExpired) {
            try {
                R r = apiSysService.transit(cpSite, requestDto, IpUtils.getUrl(request));
                redisService.del(key);
                return r;
            } finally {
                redisService.del(key);
            }
        }

        // ??????10???????????????????????????????????????
        throw new RRException("????????????????????????????????????");
    }

    public String getTransferSourceDev(@ModelAttribute BillRequestDto requestDto, HttpServletRequest request) {
        String dev = request.getHeader("dev");
        Byte transferSource = HttpsRequestUtil.getHeaderOfDev(dev);
        requestDto.setTransferSource(transferSource);
        return dev;
    }

    @GetMapping("/protocol")
    @ApiOperation(value = "????????????,????????????", notes = "display:???????????????????????????????????????1??????0???;serviceTerms:????????????????????????")
    public R protocol(HttpServletRequest request) {
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        SysWebTerms bb = sysSettingService.getMbrSysWebTerms(cpSite.getSiteCode());
        return R.ok().put("protocol", bb);
    }

    @GetMapping("/depotList")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token????????????????????????", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R depotList(HttpServletRequest request, @RequestParam(value = "terminal", required = false) Byte terminal, @RequestParam(value = "flag", required = false) Integer flag) {
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        if(flag == null){
            flag = Constants.EVNumber.one;
        }
        return R.ok().put("tGmDepots", tGmDepotService.findDepotList(userId, terminal, CommonUtil.getSiteCode(), flag));
    }

    @GetMapping("/catLabelList")
    @ApiOperation(value = "?????????????????????", notes = "?????????????????????")
    public R catList(@RequestParam("depotId") Integer depotId) {
        return R.ok().put("categorys", tGmCatService.queryCatLabelList(depotId));
    }

    @GetMapping("/catDepotList")
    @ApiOperation(value = "???????????????????????????????????????????????????()", notes = "1,??????,3??????12,??????,49??????")
    public R catDepotList(@RequestParam("catId") Integer catId, @RequestParam(value = "terminal", required = false) Byte terminal) {
        return R.ok().put("catDepots", tGmCatService.queryDepotCat(catId, terminal, CommonUtil.getSiteCode()));
    }

    @GetMapping("/gameList")
    @ApiOperation(value = "????????????", notes = "????????????")
    public R gamelist(@ModelAttribute ElecGameDto elecGameDto, @RequestParam("pageNo") @NotNull Integer pageNo,
                      @RequestParam("pageSize") @NotNull Integer pageSize) {
        if (StringUtils.isEmpty(elecGameDto.getTerminal())) {
            elecGameDto.setTerminal(ApiConstants.Terminal.pc);
        }
        return R.ok().put("page", tGmGameService.queryWebListPage(elecGameDto, pageNo, pageSize));
    }

    @GetMapping("/gameLotteryList")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public R gameLotteryList(@ModelAttribute ElecGameDto elecGameDto, @RequestParam("pageNo") @NotNull Integer pageNo,
                             @RequestParam("pageSize") @NotNull Integer pageSize) {
        if (StringUtils.isEmpty(elecGameDto.getTerminal())) {
            elecGameDto.setTerminal(ApiConstants.Terminal.pc);
        }
        return R.ok().put("page", tGmGameService.queryLotteryListPage(elecGameDto, pageNo, pageSize));
    }

    @GetMapping("/gameChessList")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public R gameChessList(@ModelAttribute ElecGameDto elecGameDto, @RequestParam("pageNo") @NotNull Integer pageNo,
                           @RequestParam("pageSize") @NotNull Integer pageSize) {
        if (StringUtils.isEmpty(elecGameDto.getTerminal())) {
            elecGameDto.setTerminal(ApiConstants.Terminal.pc);
        }
        return R.ok().put("page", tGmGameService.queryChessListPage(elecGameDto, pageNo, pageSize));
    }

    @GetMapping("/gameFishList")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public R gameFishList(@ModelAttribute ElecGameDto elecGameDto, @RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize) {
        if (StringUtils.isEmpty(elecGameDto.getTerminal())) {
            elecGameDto.setTerminal(ApiConstants.Terminal.pc);
        }
        elecGameDto.setSiteCode(CommonUtil.getSiteCode());
        return R.ok().put("page", tGmGameService.queryFishListPage(elecGameDto, pageNo, pageSize));
    }

    @GetMapping("/gameAllList")
    @ApiOperation(value = "?????????????????????", notes = "?????????????????????")
    public R gameAllList(@RequestParam("pageNumber") @NotNull int pageNumber, @RequestParam(value = "terminal", required = false) Byte terminal,
                         @RequestParam(value = "siteId", required = false) Integer siteId) {
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        return R.ok().put("page", tGmGameService.gameAllList(pageNumber, terminal, siteId));
    }

    @GetMapping("/hotList")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public R hotList(@RequestParam("pageNumber") @NotNull int pageNumber, @RequestParam(value = "terminal", required = false) Byte terminal) {
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        return R.ok().put("page", tGmGameService.gameHotGameList(pageNumber, terminal, CommonUtil.getSiteCode()));
    }

    @GetMapping("/recommendedList")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public R recommendedList(@RequestParam("pageNumber") @NotNull int pageNumber, @RequestParam(value = "terminal", required = false) Byte terminal, @RequestParam("url") String url) {
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        TCpSite tCpSite = tCpSiteService.queryOneCond(url);
        return R.ok().put("page", tGmGameService.gameRecommendedList(pageNumber, terminal, tCpSite.getId()));
    }


    @Login
    @GetMapping("/recentlyGameList")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token????????????????????????", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R recentlyGameList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("page", tGmGameService.recentlyGameList(userId));
    }

    @GetMapping("/catList")
    @ApiOperation(value = "???????????????????????????", notes = "????????????")
    public R lotteryList(@RequestParam("depotId") @NotNull Integer depotId) {
        Integer catId = new Integer(12);
        return R.ok().put("page", tGmGameService.queryCatGameList(depotId, catId));
    }

    @GetMapping("/elecDepotList")
    @ApiOperation(value = "????????????????????????", notes = "????????????????????????")
    public R elecDepotList(@RequestParam(value = "terminal", required = false) Byte terminal) {
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        return R.ok().put("tGmDepots", tGmDepotService.findelecDepotList(terminal, CommonUtil.getSiteCode()));
    }

    @GetMapping("/chessDepotList")
    @ApiOperation(value = "????????????????????????", notes = "????????????????????????")
    public R chessDepotList(@RequestParam(value = "terminal", required = false) Byte terminal) {
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        return R.ok().put("tGmDepots", tGmDepotService.findChessDepotList(terminal, CommonUtil.getSiteCode()));
    }

    @GetMapping("/lotteryDepotList")
    @ApiOperation(value = "????????????????????????", notes = "????????????????????????")
    public R lotteryDepotList(@RequestParam(value = "terminal", required = false) Byte terminal) {
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        return R.ok().put("tGmDepots", tGmDepotService.findLotteryDepotList(terminal, CommonUtil.getSiteCode()));
    }

    @GetMapping("/siteCatList")
    @ApiOperation(value = "????????????????????????", notes = "????????????????????????")
    public R siteCatList(@RequestParam(value = "terminal", required = false) Byte terminal) {
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        return R.ok().put("tGmCats", tGmDepotService.findelecCatList(terminal, CommonUtil.getSiteCode()));
    }

    @GetMapping("/findSiteCatAll")
    @ApiOperation(value = "??????????????????????????????", notes = "??????????????????????????????")
    public R findSiteCatAll() {
        return R.ok().put("tGmCats", tGmDepotService.findCatListBySiteCode(CommonUtil.getSiteCode()));
    }

    @GetMapping("/catchFishDepoList")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public R catchFishDepoList(@RequestParam(value = "terminal", required = false) Byte terminal) {
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        return R.ok().put("tGmDepots", tGmDepotService.catchFishDepoList(terminal, CommonUtil.getSiteCode()));
    }

    @GetMapping("/banks")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public R getBanks() {
        return R.ok().put("banks", baseBankService.selectAll());
    }

    @GetMapping("/provs")
    @ApiOperation(value = "????????????", notes = "????????????")
    public R getProvs() {
        BaseArea sysBaseArea = new BaseArea();
        return R.ok().put("provs", baseAreaService.findArea(sysBaseArea));
    }

    @GetMapping("/citys")
    @ApiOperation(value = "????????????", notes = "????????????")
    public R getCitys(@RequestParam("prov") String prov) {
        BaseArea sysBaseArea = new BaseArea();
        sysBaseArea.setProv(prov);
        return R.ok().put("citys", baseAreaService.findArea(sysBaseArea));
    }

    @GetMapping("/noticeList")
    @ApiOperation(value = "????????????", notes = "????????????")
    public R list(@RequestParam("pageNo") @NotNull Integer pageNo,
                  @RequestParam("pageSize") @NotNull Integer pageSize,
                  @ApiParam(value = "????????????,0?????????(?????????),1?????????(??????)") @RequestParam(value = "showType", required = false) String showType) {
        return R.ok().putPage(oprNoticeService.queryNoticeListPage(showType, pageNo, pageSize, ""));
    }

    @GetMapping("/getJackPot")
    @ApiOperation(value = "????????????", notes = "????????????")
    public R getJackPot() {
        return ptService.getJackPot(CommonUtil.getSiteCode());
    }

    @GetMapping("/topWinerList")
    @ApiOperation(value = "???????????????", notes = "???????????????")
    public R getTopWiner(String startDate, String endDate, Integer rows) {
        return R.ok().put("winers", tWinTopService.topWinerList(startDate, endDate, rows));
    }

    @GetMapping("/ActivityList")
    @ApiOperation(value = "????????????", notes = "????????????")
    public R ActivityList(@RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize,
                          @RequestParam(value = "discount", required = false) Integer discount,
                          @RequestParam(value = "actCatId", required = false) Integer actCatId,
                          @RequestParam(value = "buttonShow", required = false) Integer buttonShow,
                          @ApiParam("?????????PC ,PC 0,????????? 1") @RequestParam(value = "terminal", required = false) Byte terminal) {
        R result = R.ok();
        PageUtils page = oprActActivityService.webActivityList(pageNo, pageSize, actCatId, null, terminal, discount, buttonShow, null, Constants.EVNumber.one, null);
        result.put("page", page);
        List<AdvBanner> youhuiBanners = new ArrayList<>();
        // ??????????????????banner
        if (terminal == null || terminal == ApiConstants.Terminal.pc) {
            youhuiBanners = oprAdvService.queryYouhuiBannerList();
        }
        JSONObject data = new JSONObject();
        data.put("youhuiBanners", youhuiBanners);
        result.put("data", data);
        return result;
    }

    @GetMapping("/ActivityCatList")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public R listAll() {
        return R.ok().putPage(oprActCatService.findOprActCatList());
    }

    @GetMapping("/indexadvList")
    @ApiOperation(value = "???????????????", notes = "???????????????")
    public R indexadvList(@ApiParam("????????????: 1????????????2????????????3????????????4????????????5????????????6??? ????????? 7?????????") @RequestParam(value = "advType", required = false) Integer advType,
                          @ApiParam("??????Id, 1-12???") @RequestParam(value = "evebNum", required = false) Integer evebNum,
                          @ApiParam("?????????PC ,PC 0,????????? 1") @RequestParam(value = "terminal", required = false) Byte terminal) {
        AdvBanner oprAdv = new AdvBanner();
        oprAdv.setAdvType(advType);
        oprAdv.setEvebNum(evebNum);
        if (!StringUtils.isEmpty(terminal) && terminal.equals(ApiConstants.Terminal.mobile)) {
            oprAdv.setClientShow(AdvConstant.CLIENT_MB);
        } else {
            oprAdv.setClientShow(AdvConstant.CLIENT_PC);
        }
        List<AdvBanner> bannerList = oprAdvService.queryBannerList(oprAdv);
        R result = R.ok().putPage(bannerList);

        // ??????????????????
        if (Integer.valueOf(Constants.EVNumber.one).equals(advType)) {
            List<AdvBanner> popUpList = oprAdvService.queryPopUpList(oprAdv);
            result.put("popUpList", popUpList);
        }
        return result;
    }

    @GetMapping("/coupletList")
    @ApiOperation(value = "????????????", notes = "????????????")
    public R coupletList() {
        return R.ok().putPage(oprAdvService.coupletList());
    }

    @GetMapping("/secondadvList")
    @ApiOperation(value = "?????????????????????", notes = "?????????????????????")
    public R secondadvList(@ApiParam("????????????:1?????????2?????????3????????????4??????") @RequestParam(value = "gameCat", required = false) Byte gameCat, @ApiParam("??????Id???") @RequestParam(value = "depotId", required = false) Integer depotId, @ApiParam("?????????PC ,PC 0,????????? 1") @RequestParam(value = "terminal", required = false) Byte terminal) {
        ToprAdv oprAdv = new ToprAdv();
        oprAdv.setAdvType(AdvConstant.ADV_ACROUSEL);
        if (!StringUtils.isEmpty(terminal) && terminal.equals(ApiConstants.Terminal.mobile)) {
            oprAdv.setClientShow(AdvConstant.CLIENT_MB);
        } else {
            oprAdv.setClientShow(AdvConstant.CLIENT_PC);
        }
        oprAdv.setGameCat(gameCat);
        oprAdv.setDepotId(depotId);
        return R.ok().putPage(toprAdvService.queryWebOprAdvList(oprAdv));
    }

    @GetMapping("/getSerUrl")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public R getSerUrl(@ApiParam("?????????PC ,PC 0,????????? 1") @RequestParam(value = "terminal", required = false) Byte terminal) {
        return R.ok().put(sysSettingService.getCustomerSerUrl(terminal));
    }

    /**
     * ?????????????????????????????????????????????????????????
     */
    @RequestMapping(value = "/queryConfigDaysAndScope", method = RequestMethod.GET)
    @ApiOperation(value = "queryConfigDaysAndScope", notes = "queryRegisterSet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token????????????????????????", required = true, dataType = "Integer", paramType = "header")})
    public R queryConfigDaysAndScope() {
        StationSet stationSet = sysSettingService.queryConfigDaysAndScope();
        if (Objects.isNull(stationSet)) {
            throw new RRException("???????????????????????????????????????!");
        }
        return R.ok().put(stationSet);
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @return
     */
    @GetMapping("/findGameCatList")
    @ApiOperation(value = "???????????????????????????????????????????????????", notes = "???????????????????????????????????????????????????")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token????????????????????????", required = true, dataType = "Integer", paramType = "header")})
    public R findGameCatList(@RequestParam(value = "platFormId", required = false) String platFormId) {
        if (platFormId != null && !"".equals(platFormId.trim())) {
            return R.ok().put("page", analysisService.getGameType(platFormId, 0, CommonUtil.getSiteCode()));
        }
        return R.ok();
    }

    @GetMapping("/findFreeWalletSwitch")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public R findFreeWalletSwitch() {
        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.FREE_WALLETSWITCH);
        setting.setSysvalue("1");
        return R.ok().put(setting);
    }

    @GetMapping("/siteUrlList")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public R siteUrlList(@RequestParam("siteId") @NotNull Integer siteId) {
        return R.ok().put("page", tGmGameService.siteUrlList(siteId));
    }

    @GetMapping("/siteDepotList")
    @ApiOperation(value = "????????????????????????", notes = "????????????????????????")
    public R siteDepotList(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", tGmGameService.siteDepotList(pageNo, pageSize, CommonUtil.getSiteCode()));
    }

    @GetMapping("/queryStationSet")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public R queryStationSet() {
        return R.ok().put(sysSettingService.queryApiStationSet());
    }


    @RequestMapping("/getSiteurl")
    @ApiOperation(value = "??????siteUrl", notes = "??????siteUrl")
    public R getSiteurl(@RequestParam(value = "siteCode", required = false) String siteCode) {
        log.info("getSiteurl-params:" + "siteCode:" + siteCode);
        // ????????????
        return R.ok().put(tCpSiteService.getSiteurl(siteCode));
    }

    @GetMapping("/tryPlayGame")
    @ApiOperation(value = "????????????????????????", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R tryPlayGame(@ModelAttribute BillRequestDto requestDto, HttpServletRequest request) {
        Assert.isNull(requestDto.getGameId(), "??????Id????????????!");
        Assert.isNull(CommonUtil.getSiteCode(), "SToken???????????????");
        Assert.isNull(request.getHeader("dev"), "dev???????????????");

        Integer gameId = requestDto.getGameId();            // ????????????Id
        String siteCode = CommonUtil.getSiteCode();         // ??????????????????
        String origin = request.getHeader("dev");    // ??????origin
        String domain = IpUtils.getUrl(request);            // ????????????domain
        String userName = null;                             // ????????????????????????
        String password = null;

        // ??????gameId??????gameType/gameCode
        TGmGame tGmGame = tGmGameMapper.selectByPrimaryKey(gameId);
        if (Objects.isNull(tGmGame)) {
            return R.ok().put("tryPlayFlag", false).put("errMsg", "????????????!");
        }
        String gameCode = tGmGame.getGameCode();
        String gameType = "";
        switch (tGmGame.getCatId()) {
            case Constants.EVNumber.one:
                gameType = GameTypeEnum.ENUM_ONES.getValue();
                break;
            case Constants.EVNumber.three:
                gameType = GameTypeEnum.ENUM_THREE.getValue();
                break;
            case Constants.EVNumber.five:
                gameType = GameTypeEnum.ENUM_FIVE.getValue();
                break;
            case Constants.EVNumber.eight:
                gameType = GameTypeEnum.ENUM_EIGHT.getValue();
                break;
            case Constants.EVNumber.twelve:
                gameType = GameTypeEnum.ENUM_TWELVE.getValue();
                break;
            case Constants.EVNumber.six:
                gameType = GameTypeEnum.ENUM_SIX.getValue();
                break;
            case Constants.EVNumber.nine:
                gameType = GameTypeEnum.ENUM_NINE.getValue();
                break;
            default:
        }
        // ??????depotId??????depotCode
        TGmDepot tGmDepot = tGmDepotMapper.selectByPrimaryKey(tGmGame.getDepotId());
        if (Objects.isNull(tGmDepot)) {
            return R.ok().put("tryPlayFlag", false).put("errMsg", "????????????");
        }
        String depotCode = tGmDepot.getDepotCode();

        // ??????????????????
        PlayGameModel playGameModel = new PlayGameModel();
        playGameModel.setOrigin(origin);
        playGameModel.setUserName(userName);
        playGameModel.setPassword(password);
        playGameModel.setDepotId(tGmGame.getDepotId());
        playGameModel.setDepotCode(depotCode);
        playGameModel.setSiteCode(siteCode);
        playGameModel.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        playGameModel.setDomain(domain);
        playGameModel.setGameType(gameType);
        playGameModel.setGamecode(gameCode);
        if ("PC".equals(origin)) {
            playGameModel.setGameId(tGmGame.getGameCode());
        } else {
            playGameModel.setGameId(tGmGame.getMbGameCode());
        }
        String resultString = gatewayDepotService.tryPlayGame(playGameModel);
        Map resultMaps = (Map) JSON.parse(resultString);
        if (Objects.isNull(resultMaps)) {
            return R.ok().put("tryPlayFlag", false).put("errMsg", "??????????????????????????????");
        }

        if ("200".equals(resultMaps.get("msgCode"))) {
            if (StringUtil.isEmpty(resultMaps.get("message"))) {
                return R.ok().put("tryPlayFlag", false).put("errMsg", "gateway??????url?????????");
            } else {
                return R.ok(resultMaps.get("message").toString()).put("tryPlayFlag", true);
            }
        } else if ("601".equals(resultMaps.get("msgCode"))) { // ?????????????????????
            return R.ok().put("tryPlayFlag", false).put("errMsg", "?????????????????????");
        } else {
            return R.ok().put("tryPlayFlag", false).put("errMsg", resultString);
        }
    }

    @GetMapping("/getGameListWithoutRebate")
    @ApiOperation(value = "???????????????????????????", notes = "???????????????????????????")
    @ApiImplicitParams({@ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getGameListWithoutRebate() {
        return R.ok().put(tGmGameService.getGameListWithoutRebate());
    }


    @GetMapping("/indexNoticeAndAdv")
    @ApiOperation(value = "????????????+??????", notes = "????????????+??????")
    public R indexNoticeAndAdv(@ApiParam("????????????: 1????????????2????????????3????????????4????????????5????????????6??? ????????? 7?????????") @RequestParam(value = "advType", required = false) Integer advType,
                               @ApiParam("??????Id, 1-12???") @RequestParam(value = "evebNum", required = false) Integer evebNum,
                               @ApiParam("?????????PC ,PC 0,????????? 1") @RequestParam(value = "terminal", required = false) Byte terminal,

                               @RequestParam("pageNo") @NotNull Integer pageNo,
                               @RequestParam("pageSize") @NotNull Integer pageSize,
                               @ApiParam(value = "????????????,0?????????(?????????),1?????????(??????)") @RequestParam(value = "showType", required = false) String showType) {
        AdvBanner oprAdv = new AdvBanner();
        oprAdv.setAdvType(advType);
        oprAdv.setEvebNum(evebNum);
        if (!StringUtils.isEmpty(terminal) && terminal.equals(ApiConstants.Terminal.mobile)) {
            oprAdv.setClientShow(AdvConstant.CLIENT_MB);
        } else {
            oprAdv.setClientShow(AdvConstant.CLIENT_PC);
        }
        List<AdvBanner> bannerList = oprAdvService.queryBannerList(oprAdv);
        R result = R.ok().putPage(bannerList);
        
        // ??????????????????
        if (Integer.valueOf(Constants.EVNumber.one).equals(advType)) {
            List<AdvBanner> popUpList = oprAdvService.queryPopUpList(oprAdv);
            result.put("popUpList", popUpList);
        }
        // ??????
        Object noticeList = oprNoticeService.queryNoticeListPage(showType, pageNo, pageSize, "");
        result.put("noticeList", noticeList);
        return result;
    }
    
    @GetMapping("/indexNoticeAndAdvSwitch")
    @ApiOperation(value = "????????????+??????????????????", notes = "????????????+??????????????????")
    public R indexNoticeAndAdvSwitch() {
    	R ok = R.ok();
    	SysSetting openCarousel = sysSettingService.queryObject(SystemConstants.IS_OPEN_CAROUSEL);
    	if (openCarousel != null) {
    		ok.put("isPopUp", openCarousel.getSysvalue());
    	}
    	SysSetting openNotice = sysSettingService.queryObject(SystemConstants.IS_OPEN_NOTICE);
    	if (openNotice != null) {
    		ok.put("isNotice", openNotice.getSysvalue());
    	}
    	return ok;
    }
    
    @GetMapping("/guessAsk")
    @ApiOperation(value = "????????????", notes = "????????????")
    public R guessAsk(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
    	return R.ok().put("page", helpGuessAskService.queryListPage(pageNo, pageSize, true));
    }

    @GetMapping("/getLotteryCategory")
    @ApiOperation(value = "??????????????????????????????", notes = "??????????????????????????????")
    public R getLotteryCategory(@ModelAttribute SetGameCategory setGameCategory) {
        Assert.isNull(setGameCategory.getGamelogoid(), "??????id????????????");
        return R.ok().put("category", setGameCategoryService.getLotteryCategory(setGameCategory.getGamelogoid()));
    }
    
    @GetMapping("/getGameByCategory")
    @ApiOperation(value = "????????????????????????", notes = "??????????????????")
    public R getGameByCategory(@ModelAttribute ElecGameDto elecGameDto, @RequestParam("pageNo") @NotNull Integer pageNo,
            @RequestParam("pageSize") @NotNull Integer pageSize) {
    	Assert.isNull(elecGameDto.getId(), "??????id????????????");
    	if (StringUtils.isEmpty(elecGameDto.getTerminal())) {
            elecGameDto.setTerminal(ApiConstants.Terminal.pc);
        }
    	return R.ok().put("games", setGameCategoryService.getGameByCategory(elecGameDto,  pageNo, pageSize));
    }

    @GetMapping("/getTrunmanShowCategory")
    @ApiOperation(value = "??????????????????????????????", notes = "??????????????????????????????")
    public R getTrunmanShowCategory(@RequestParam("gamelogoid") Integer gamelogoid, @RequestParam(value = "terminal", required = false) Byte terminal) {
        Assert.isNull(gamelogoid, "??????id????????????");
        return R.ok().put("category", tGmCatService.getTrunmanShowCategory(gamelogoid));
    }
    
    
    @GetMapping("/getGameByTrunmanShowCategory")
    @ApiOperation(value = "????????????????????????", notes = "????????????????????????")
    public R getGameByTrunmanShowCategory(@ModelAttribute ElecGameDto elecGameDto, @RequestParam("pageNo") @NotNull Integer pageNo,
            @RequestParam("pageSize") @NotNull Integer pageSize) {
//    	Assert.isNull(elecGameDto.getId(), "??????id????????????");
    	if (StringUtils.isEmpty(elecGameDto.getTerminal())) {
            elecGameDto.setTerminal(ApiConstants.Terminal.pc);
        }
    	return R.ok().put("games", tGmGameService.getGameByTrunmanShowCategory(elecGameDto,  pageNo, pageSize));
    }
    
    @GetMapping("/queryVenturePlanPic")
    @ApiOperation(value = "????????????????????????", notes = "????????????????????????")
    public R queryVenturePlanSet() {
    	return R.ok().put(sysSettingService.queryVenturePlanSet());
    }
    
    
}
