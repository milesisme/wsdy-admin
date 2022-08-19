package com.wsdy.saasops.modules.analysis.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.analysis.dto.WinLostReportModelDto;
import com.wsdy.saasops.modules.analysis.service.GameWinLoseNewService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

import static com.wsdy.saasops.modules.analysis.controller.GameWinLoseController.dealCatCodes;


@RestController
@RequestMapping("/bkapi/analysis/gameWinLose")
@Api(value = "GameWinLoseNewController", tags = "输赢报表-无限代理")
public class GameWinLoseNewController extends AbstractController {
    @Autowired
    private GameWinLoseNewService winLoseService;

    public static    final String exportWinLostReportModule = "exportWinLostReport";

    public static    final String exportAccountWinLostReportModule = "exportAccountWinLostReport";

    @GetMapping("/findWinLostReportView")
    @RequiresPermissions(value = {"analysis.gameWinLose.view", "analysis:betDetails:finalBetDetailsAll"}, logical = Logical.OR)
    @ApiOperation(value = "代理的视图切换表头", notes = "代理的视图切换表头")
    public R findWinLostReportView(@ModelAttribute WinLostReportModelDto reportModelDto) {
        // 处理catCodes
        dealCatCodes(reportModelDto);
        return R.ok().putPage(winLoseService.findWinLostReportView(reportModelDto));
    }

    @GetMapping("/findWinLostReportViewAgent")
    @RequiresPermissions(value = {"analysis.gameWinLose.view", "analysis:betDetails:finalBetDetailsAll"}, logical = Logical.OR)
    @ApiOperation(value = "视图--直属代理", notes = "视图--直属代理")
    public R findWinLostReportViewAgent(@ModelAttribute WinLostReportModelDto reportModelDto,
                                        @RequestParam("pageNo") @NotNull Integer pageNo,
                                        @RequestParam("pageSize") @NotNull Integer pageSize) {
        // 处理catCodes
        dealCatCodes(reportModelDto);
        return R.ok().putPage(winLoseService.findWinLostReportViewAgent(reportModelDto,pageNo,pageSize));
    }


    @GetMapping("/findWinLostReportMbrView")
    @RequiresPermissions(value = {"analysis.gameWinLose.view", "analysis:betDetails:finalBetDetailsAll"}, logical = Logical.OR)
    @ApiOperation(value = "会员的视图切换表头", notes = "会员的视图切换表头")
    public R findWinLostReportMbrView(@ModelAttribute WinLostReportModelDto reportModelDto) {
        // 处理catCodes
        dealCatCodes(reportModelDto);
        return R.ok().putPage(winLoseService.findWinLostReportMbrView(reportModelDto));
    }



    @GetMapping("/exportWinLostReport")
    @ApiOperation(value = "导出文件", notes = "总代列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R exportWinLostReport(@ModelAttribute WinLostReportModelDto reportModelDto) {
        winLoseService.exportWinLostReport(reportModelDto, getUserId(), exportWinLostReportModule);
        return R.ok();
    }


    @GetMapping("/exportAccountWinLostReport")
    @ApiOperation(value = "导出文件", notes = "总代列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R exportAccountWinLostReport(@ModelAttribute WinLostReportModelDto reportModelDto) {
        winLoseService.exportAccountWinLostReport(reportModelDto, getUserId(), exportAccountWinLostReportModule);
        return R.ok();
    }
}
