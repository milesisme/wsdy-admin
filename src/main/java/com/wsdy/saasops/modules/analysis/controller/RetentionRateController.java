package com.wsdy.saasops.modules.analysis.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.analysis.dto.RetentionRateDailyActiveDto;
import com.wsdy.saasops.modules.analysis.dto.RetentionRateDto;
import com.wsdy.saasops.modules.analysis.service.RetentionRateService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/bkapi/analysis/retentionRate")
@Api(value = "RetentionRateController", tags = "留存")
public class RetentionRateController    extends AbstractController {


    public static final String exportexportRetentionRateReport = "exportRetentionRateReport";
    public static final String EXPORT_RETENTION_RATE_DAILY_ACTIVE_REPORT = "exportRetentionRateDailyActiveReport";


    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    @Autowired
    private RetentionRateService retentionRateService;

    @GetMapping("/retentionRateReport")
    @RequiresPermissions("analysis:retentionRate:list")
    @ApiOperation(value = "代理留存查询", notes = "留存报表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R retentionRateReport(@ModelAttribute RetentionRateDto retentionRateDto) {
        Assert.isNull(retentionRateDto.getStartTime(), "开始时间不能为空");
        Assert.isNull(retentionRateDto.getEndTime(), "结束时间不能为空");
        Assert.isNull(retentionRateDto.getPageNo(), "页码不能为空");
        Assert.isNull(retentionRateDto.getPageSize(), "也大小不能为空");
        return R.ok().put(retentionRateService.list(retentionRateDto));
    }

    @GetMapping("/exportRetentionRateReport")
    @RequiresPermissions("analysis:retentionRate:export")
    @ApiOperation(value = "导出文件", notes = "总代列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R exportRetentionRateReport(@ModelAttribute RetentionRateDto retentionRateDto) {
        retentionRateService.exportRetentionRateReport(retentionRateDto, getUserId(), exportexportRetentionRateReport);
        return R.ok();
    }

    @GetMapping("/checkFile")
    @ApiOperation(value = "查询文件是否可下载", notes = "查询文件是否可下载")
    public R checkFile(@RequestParam("module") String module, HttpServletRequest request) {
        Long userId = getUserId();
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if (null != record) {
          if(RetentionRateController.exportexportRetentionRateReport.equals(module)){
                record.setDownloadFileName("玩家留存.xls");
            }else if (RetentionRateController.EXPORT_RETENTION_RATE_DAILY_ACTIVE_REPORT.equals(module)) {
              record.setDownloadFileName("玩家自首充日起，往后30日活跃报表.xls");
          }
            return R.ok().put(record);
        }
        return R.ok(false);
    }

    @GetMapping("/retentionRateDailyActiveReport")
    @RequiresPermissions("analysis:retentionRateDailyActive:list")
    @ApiOperation(value = "用户留存每日活跃报表", notes = "用户留存每日活跃报表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R retentionRateDailyActiveReport(RetentionRateDailyActiveDto retentionRateDto) throws ExecutionException, InterruptedException {
        Assert.isNull(retentionRateDto.getStartTime(), "开始时间不能为空");
        Assert.isNull(retentionRateDto.getEndTime(), "结束时间不能为空");
        Assert.isNull(retentionRateDto.getPageNo(), "页码不能为空");
        Assert.isNull(retentionRateDto.getPageSize(), "页大小不能为空");
//        return R.ok().put("{}");
        return R.ok().put(retentionRateService.retentionRateDailyActiveReport(retentionRateDto));
    }


    @GetMapping("/exportRetentionRateDailyActiveReport")
    @RequiresPermissions("analysis:retentionRateDailyActive:export")
    @ApiOperation(value = "导出自首充后30日，每日活跃报告文件", notes = "导出自首充后30日，每日活跃报告文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R exportRetentionRateDailyActiveReport(RetentionRateDailyActiveDto retentionRateDto) {
        retentionRateService.exportRetentionRateDailyActiveReport(retentionRateDto, getUserId(), EXPORT_RETENTION_RATE_DAILY_ACTIVE_REPORT);
        return R.ok();
    }

}
