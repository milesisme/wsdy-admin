package com.wsdy.saasops.modules.analysis.controller;

import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.analysis.dto.WinLostReportModelDto;
import com.wsdy.saasops.modules.analysis.service.GameWinLoseService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;


@RestController
@RequestMapping("/bkapi/analysis/gameWinLose")
@Api(value = "GameWinLoseController", tags = "输赢报表")
public class GameWinLoseController extends AbstractController {

    private final String mbrLoseModule = "mbrWinLoseDetail";
    private final String mbrLoseDailyModule = "mbrWinLoseDailyDetail";
    @Value("${analysis.mbrWinLose.excel.path}")
    private String mbrWinLoseExcelPath;

    @Value("${analysis.mbrWinLose.daily.excel.path}")
    private String mbrWinLoseDailyExcelPath;

    @Autowired
    private GameWinLoseService winLoseService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    @GetMapping("/findWinLostReportList")
    @RequiresPermissions(value = {"analysis.gameWinLose.view", "analysis:betDetails:finalBetDetailsAll"}, logical = Logical.OR)
    @ApiOperation(value = "按游戏类别汇总列表-表头", notes = "按游戏类别汇总列表-表头")
    public R findWinLostReportList(@ModelAttribute WinLostReportModelDto reportModelDto) {
        // 处理catCodes
        dealCatCodes(reportModelDto);
        return R.ok().put(winLoseService.findWinLostReportList(reportModelDto));
    }

    @GetMapping("/findWinLostSum")
    @RequiresPermissions(value = {"analysis.gameWinLose.view", "analysis:betDetails:finalBetDetailsAll"}, logical = Logical.OR)
    @ApiOperation(value = "按游戏类别汇总列表-表头-总计", notes = "按游戏类别汇总列表-表头-总计")
    public R findWinLostSum(@ModelAttribute WinLostReportModelDto reportModelDto) {
        // 处理catCodes
        dealCatCodes(reportModelDto);
        return R.ok().putPage(winLoseService.findWinLostSum(reportModelDto));
    }

    @GetMapping("/findWinLostLoginNameList")
    @RequiresPermissions(value = {"analysis.gameWinLose.view", "analysis:betDetails:finalBetDetailsAll"}, logical = Logical.OR)
    @ApiOperation(value = "股东视图下查询范围全部会员的会员列表", notes = "仅查询 会员列表")
    public R findWinLostLoginNameList(@ModelAttribute WinLostReportModelDto reportModelDto,
                                      @RequestParam("pageNo") @NotNull Integer pageNo,
                                      @RequestParam("pageSize") @NotNull Integer pageSize) {
        // 处理catCodes
        dealCatCodes(reportModelDto);
        return R.ok().putPage(winLoseService.findWinLostLoginNameList(reportModelDto, pageNo, pageSize));
    }

    @GetMapping("/findWinLostListLevel")
    @RequiresPermissions(value = {"analysis.gameWinLose.view", "analysis:betDetails:finalBetDetailsAll"}, logical = Logical.OR)
    @ApiOperation(value = "直属会员列表", notes = "直属会员 1.代理视图-代理直属会员 2.会员视图--点击会员或 查询会员 ")
    public R findWinLostListLevel(@ModelAttribute WinLostReportModelDto reportModelDto,
                                  @RequestParam("pageNo") @NotNull Integer pageNo,
                                  @RequestParam("pageSize") @NotNull Integer pageSize) {
        // 处理catCodes
        dealCatCodes(reportModelDto);
        return R.ok().putPage(winLoseService.findWinLostListLevel(reportModelDto, pageNo, pageSize));
    }

    @GetMapping("/findWinLostLoginName")
    @RequiresPermissions(value = {"analysis.gameWinLose.view", "analysis:betDetails:finalBetDetailsAll"}, logical = Logical.OR)
    @ApiOperation(value = "会员-跟进会员查询 只查询属于自己", notes = "1.点击会员进入会员视图， 2. 查询会员的会员视图")
    public R findWinLostLoginName(@ModelAttribute WinLostReportModelDto reportModelDto) {
        // 处理catCodes
        dealCatCodes(reportModelDto);
        return R.ok().putPage(winLoseService.findWinLostLoginName(reportModelDto));
    }

    @GetMapping("/findWinLostAccount")
    @RequiresPermissions(value = {"analysis.gameWinLose.view", "analysis:betDetails:finalBetDetailsAll"}, logical = Logical.OR)
    @ApiOperation(value = "会员-详情-类别统计表头-只查询自己", notes = "会员详情--表头")
    public R findWinLostAccount(@ModelAttribute WinLostReportModelDto reportModelDto) {
        Assert.isBlank(reportModelDto.getLoginName(), "会员名不能为空");
        // 处理catCodes
        dealCatCodes(reportModelDto);
        return R.ok().putPage(winLoseService.findWinLostAccount(reportModelDto));
    }

    @GetMapping("/findWinLostSumByLoginName")
    @RequiresPermissions(value = {"analysis.gameWinLose.view", "analysis:betDetails:finalBetDetailsAll"}, logical = Logical.OR)
    @ApiOperation(value = "会员-详情-类别统计表-会员自己sum", notes = "会员详情--会员总计")
    public R findWinLostSumByLoginName(@ModelAttribute WinLostReportModelDto reportModelDto) {
        Assert.isBlank(reportModelDto.getLoginName(), "会员名不能为空");
        // 处理catCodes
        dealCatCodes(reportModelDto);
        return R.ok().putPage(winLoseService.findWinLostListSumLoginName(reportModelDto));
    }

    @GetMapping("/exportMbrWinLoseInfo")
    @RequiresPermissions(value = {"analysis:betDetails:exportMbrWinLoseInfo"}, logical = Logical.OR)
    @ApiOperation(value = "导出会员信息", notes = "导出会员信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R exportMbrWinLoseInfo(@ModelAttribute WinLostReportModelDto reportModelDto) {
        // 处理catCodes
        dealCatCodes(reportModelDto);
        String module = mbrLoseModule;
        String filePath = mbrWinLoseExcelPath;
        if (reportModelDto.getExportType() == 1) {
            module = mbrLoseDailyModule;
            filePath = mbrWinLoseDailyExcelPath;
        }
        SysFileExportRecord record = winLoseService.exportMbrWinLoseList(reportModelDto, getUserId(), module, filePath);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("/checkFile")
    @ApiOperation(value = "查询文件是否可下载", notes = "查询文件是否可下载")
    public R checkFile(@RequestParam("module") String module, HttpServletRequest request) {
        Long userId = getUserId();
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if (null != record) {

            if (mbrLoseModule.equals(module)) {
                String fileName = mbrWinLoseExcelPath.substring(mbrWinLoseExcelPath.lastIndexOf("/") + 1, mbrWinLoseExcelPath.length());
                record.setDownloadFileName(fileName);
            } else if (GameWinLoseNewController.exportWinLostReportModule.equals(module)) {
                record.setDownloadFileName("输赢报表.xls");
            } else if (GameWinLoseNewController.exportAccountWinLostReportModule.equals(module)) {
                record.setDownloadFileName("会员输赢报表.xls");
            } else if (mbrLoseDailyModule.equals(module)) {
                record.setDownloadFileName("会员每日输赢报表.xls");
            }
            return R.ok().put(record);
        }
        return R.ok(false);
    }

    // 处理catCodes
    public static void dealCatCodes(WinLostReportModelDto reportModelDto) {
        // 处理传入的参数
        List<String> catCodes = reportModelDto.getCatCodes();
        if (!Objects.isNull(catCodes) && catCodes.size() > 0) {
//            if(catCodes.contains("Sport")){
//                catCodes.add("Esport");
//            }
            if (catCodes.contains("Others")) {
//                catCodes.add("Tip");
                catCodes.add("Activity");
                catCodes.add("Unknown");
            }
        }
    }
}
