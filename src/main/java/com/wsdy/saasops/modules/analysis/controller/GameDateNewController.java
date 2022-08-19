package com.wsdy.saasops.modules.analysis.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.analysis.entity.GameReportModel;
import com.wsdy.saasops.modules.analysis.service.GameDateNewService;
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
@Api(value = "GameData", tags = "游戏数据")
public class GameDateNewController {

    @Autowired
    private GameDateNewService gameDateNewService;

    /**
     * 根据上一级代理 查询他所有下级数据
     * 3.代理页增加“下级代理”一列，点击进入下级代理列表；代理层级面包屑导航展示 接口
     * @return
     */
    @RequestMapping("/findBetDayGroupAgentPageNew")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findBetDayGroupAgentPage(GameReportModel model) {
        return R.ok().put("page", gameDateNewService.findBetDayGroupAgentPage(model));
    }

    /**
     * 点击代理名 视图
     * 点击代理名进入代理的投注详情页，区分“下级代理”、“直属会员”、所有下级三个视图
     *
     * @return
     */
    @RequestMapping("/findBetDayBetAgent")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findBetDayBetAgent(GameReportModel model) {
        return R.ok().put(gameDateNewService.findBetDayBetAgent(model));
    }

    /**
     * 下级代理 直属会员  所有下级 视图
     * isSign 1 2  3
     * @return
     */
    @RequestMapping("/findBetDayGroupGameTypePageNew")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findBetDayGroupGameTypePage(GameReportModel model) {
        return  R.ok().put("page",gameDateNewService.findBetDayGroupGameTypePage(model));
    }

    /**
     * 下级代理 直属会员  所有下级 查询汇总
     * @return
     */
    @RequestMapping("/findBetDayTotalNew")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findBetDayTotal(GameReportModel model) {
        return  R.ok().put("page",gameDateNewService.findBetDayByAgentPage(model));
    }

    /**
     * 查询 直属用户 or 所有下级 数据
     * isSign 1 直属  4所有
     * @return
     */
    @RequestMapping("/findBetDayGroupUserPageNew")
    @RequiresPermissions(value = {"analysis:betDetails:finalBetDetailsAll", "analysis.gameData.view"}, logical = Logical.OR)
    @ApiOperation(value = "游戏数据报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findBetDayGroupUserPage(GameReportModel model) {
        return  R.ok().put("page",gameDateNewService.findBetDayGroupUserPage(model));
    }
}
