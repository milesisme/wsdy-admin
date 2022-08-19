package com.wsdy.saasops.modules.analysis.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.analysis.entity.GameReportModel;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/bkapi/analysis/gameData")
@Api(value = "GameData",tags = "游戏数据")
public class GameDateController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 查询 游戏数据报表
     * @return
     */
    @RequestMapping("/findRptBetTotalPage")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findRptBetTotalPage(GameReportModel model) {
        return  R.ok().put("page",analysisService.findRptBetTotalPage(model));
    }

    /**
     * 查询 游戏数据报表
     * @return
     */
    @RequestMapping("/findRptBetTotalList")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findRptBetTotalList(GameReportModel model) {
        return  R.ok().put("page",analysisService.findRptBetTotalList(model));
    }

    /**
     * 查询 游戏数据报表,合计 总代 代理 总投注人数
     * @return
     */
    @RequestMapping("/findRptBetTotal")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findRptBetTotal(GameReportModel model) {
        return  R.ok().put("page",analysisService.findRptBetTotalPage(model));
    }

    /**
     * 查询 聚合游戏平台 游戏分类
     * @return
     */
    @RequestMapping("/findBetDayGroupGameTypePage")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findBetDayGroupGameTypePage(GameReportModel model) {
        return  R.ok().put("page",analysisService.findBetDayGroupGameTypePage(model));
    }

    /**
     * 查询 聚合总代理
     * @return
     */
    @RequestMapping("/findBetDayGroupTopAgentPage")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findBetDayGroupTopAgentPage(GameReportModel model) {
        return  R.ok().put("page",analysisService.findBetDayGroupTopAgentPage(model));
    }

    /**
     * 查询 聚合子代理
     * @return
     */
    @RequestMapping("/findBetDayGroupAgentPage")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findBetDayGroupAgentPage(GameReportModel model) {
        return  R.ok().put("page",analysisService.findBetDayGroupAgentPage(model));
    }

    /**
     * 查询 聚合用户
     * @return
     */
    @RequestMapping("/findBetDayGroupUserPage")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findBetDayGroupUserPage(GameReportModel model) {
        return  R.ok().put("page",analysisService.findBetDayGroupUserPage(model));
    }

    /**
     * 根据 总代 和代理 查询汇总
     * @return
     */
    @RequestMapping("/findBetDayTotal")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findBetDayTotal(GameReportModel model) {
        return  R.ok().put("page",analysisService.findBetDayByAgentPage(model));
    }

}
