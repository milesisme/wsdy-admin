package com.wsdy.saasops.modules.analysis.controller;

import com.wsdy.saasops.api.utils.GameTypeEnum;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.analysis.entity.FundReportModel;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.analysis.service.ExportBetDetailsService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.operate.entity.TGmCat;
import com.wsdy.saasops.modules.operate.service.TGmCatService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/bkapi/analysis/betDetails")
@Api(value = "Analysis", tags = "经营分析")
public class BetDetailsController extends AbstractController {

    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    private final String betModule = "betDetail";
    @Value("${analysis.betDetails.excel.path}")
    private String betDetailsExcelPath;
    @Autowired
    private TGmCatService tGmCatService;
    @Autowired
    private ExportBetDetailsService exportBetDetailsService;


    /**
     * 跳转到全部游戏注单页面
     *
     * @return
     */
    @GetMapping("betDetailsList")
    public String goBetDetailsList() {
        return "/analysis/betDetailsList";
    }

    /**
     * 查询投注明细统计
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("/finalBetDetailsAll")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "投注记录", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R betDetailsData(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, GameReportQueryModel model) {
        model.setSiteCode(CommonUtil.getSiteCode());
        if (Integer.valueOf(Constants.EVNumber.one).equals(model.getIsTip())) {
            model.setGametype(String.valueOf(GameTypeEnum.ENUM_TIPS.getKey()));     // 设置为小费
        }
        return R.ok().put("page", analysisService.getBkRptBetListPage(pageNo, pageSize, model)).put("total", analysisService.getRptBetListReport(model));
    }

    /**
     * 查询日统计
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("/findRptBetDay")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "经营报表", notes = "经营报表")
    public R findRptBetDay(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam(value = "parentAgentid", required = false) Integer parentAgentid,
                           @RequestParam(value = "agentid", required = false) Integer agentid, @RequestParam(value = "groupid", required = false) Integer groupid, @RequestParam(value = "loginName", required = false) String loginName,
                           @RequestParam(value = "platform", required = false) String platform, @RequestParam(value = "gametype", required = false) String gametype,
                           @RequestParam(value = "betStrTime", required = false) String betStrTime, @RequestParam(value = "betEndTime", required = false) String betEndTime, @RequestParam(value = "orderBy", required = false) String orderBy,
                           @RequestParam(value = "group", required = false) String group) {
        Map resultMap = new HashMap<>(4);
        FundReportModel report = analysisService.getFundReport(parentAgentid, agentid, groupid, betStrTime, betEndTime);
        Map fundMap = analysisService.getFundStatistics(parentAgentid, agentid, groupid, loginName,
                platform, gametype, betStrTime, betEndTime);
        PageUtils page = analysisService.getRptBetDay(pageNo, pageSize, parentAgentid, agentid, groupid, loginName,
                platform, gametype, betStrTime, betEndTime, orderBy, group);
        resultMap.put("profit", report);
        resultMap.put("option", fundMap);
        resultMap.put("page", page);
        return R.ok().put("data", resultMap);
    }

    /**
     * 查询彩金下注
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("/findJackpotBet")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "彩金下注", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findJackpotBetDetails(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, GameReportQueryModel model) {
        model.setSiteCode(CommonUtil.getSiteCode());
        return R.ok().put("page", analysisService.getJackpotBetListPage(pageNo, pageSize, model));
    }

    /**
     * 查询彩金中奖
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("/findJackpotRewardBet")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "彩金中奖", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findJackpotRewardBetDetails(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, GameReportQueryModel model) {
        model.setSiteCode(CommonUtil.getSiteCode());
        return R.ok().put("page", analysisService.getJackpotBetListPage(pageNo, pageSize, model));
    }

    /**
     * 查询游戏代码及查平台的所属的类别
     *
     * @return
     */
    @GetMapping("/finalGameCode")
//    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "游戏代码", notes = "游戏代码")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R finalGameCodByType(@RequestParam("codetype") String depotCode, @RequestParam(value = "catCode",required = false) String catCode) {
        // 查平台的所属的类别，key：类型id，value: 类型名
        if (depotCode != null && !"".equals(depotCode.trim())) {
            return R.ok().put("page", analysisService.getGameType(depotCode, 0, CommonUtil.getSiteCode()));
        }
        // 查站点所有平台code和名称
        return R.ok().put("page", analysisService.getPlatFormByCatCode(CommonUtil.getSiteCode(), catCode));
    }

    /**
     * 查询所有代理账号
     *
     * @return
     */
    @GetMapping("/finalAgentAccount")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "代理账号", notes = "代理账号")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R finalAgentAccount() {
        return R.ok().put("page", analysisService.getAgentAccount());
    }


    @Deprecated
    @GetMapping("/exportBetDetails_1")
    @RequiresPermissions("analysis:betDetails:exportExcel")
    @ApiOperation(value = "导出投注记录", notes = "导出投注记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R exportBetDetails1(GameReportQueryModel model) {
        String module = StringUtil.isEmpty(model.getGametype()) ? betModule : betModule + "_" + model.getGametype();
        log.info("export==userId==" + getUserId() + "==module==gameType==" + module);
        SysFileExportRecord record = analysisService.betDetailsExportExcel(model, getUserId(), module);

        if (Objects.isNull(record)) {
            throw new R200Exception("正在处理中,请10分钟后再试!");
        }
        if ("fail".equals(record.getSaveFlag())) {
            throw new R200Exception("导出失败，请重试！");
        }
        return R.ok();
    }


    @GetMapping("/exportBetDetails")
    @RequiresPermissions("analysis:betDetails:exportExcel")
    @ApiOperation(value = "导出投注记录", notes = "导出投注记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R exportBetDetails(GameReportQueryModel model, HttpServletRequest request, HttpServletResponse response) {
        String module = StringUtil.isEmpty(model.getGametype()) ? betModule : betModule + "_" + model.getGametype();
        SysFileExportRecord record = exportBetDetailsService.betDetailsExportExcel(model, getUserId(), module);
        if (Objects.isNull(record)) {
            throw new R200Exception("正在处理中,请10分钟后再试!");
        }
        if ("fail".equals(record.getSaveFlag())) {
            throw new R200Exception("导出失败，请重试！");
        }
        return R.ok();
    }

    @GetMapping("checkFile")
    @ApiOperation(value = "查询文件是否可下载", notes = "查询文件是否可下载")
    public R checkFile(GameReportQueryModel model) {
        Long userId = getUserId();
        String module = StringUtil.isEmpty(model.getGametype()) ? betModule : betModule + "_" + model.getGametype();
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if (null != record) {
            String gameType = getGameTypeName(model);
            String commonFileName = betDetailsExcelPath.substring(betDetailsExcelPath.lastIndexOf("/") + 1, betDetailsExcelPath.length());
            String fileName = commonFileName.replace("$", gameType);
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }

    private String getGameTypeName(GameReportQueryModel model) {
        String gameTypeName;
        if (StringUtil.isNotEmpty(model.getGametype())) {

            TGmCat cat = tGmCatService.queryObject(Integer.valueOf(model.getGametype()));
            gameTypeName = "（" + cat.getCatName() + "）";
        } else {
            gameTypeName = "";
        }
        return gameTypeName;
    }
}

