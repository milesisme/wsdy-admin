package com.wsdy.saasops.modules.analysis.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.analysis.entity.FundStatementModel;
import com.wsdy.saasops.modules.analysis.service.FundStatementService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/bkapi/analysis/fundReport")
@Api(value = "FundStatement", tags = "资金报表")
public class FundStatementController extends AbstractController {

    private final String exportListModule = "exportList";
    private final String exportTagencyListModule = "exportTagencyList";
    private final String agentSubMbrListModule = "agentSubMbrList";
    private final String exportAgentList = "exportAgentList";
    private final String exportTagencyFundList = "exportTagencyFundList";
    @Autowired
    private FundStatementService fundStatementService;

    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    @GetMapping("/list")
    @RequiresPermissions("analysis:fundReport:list")
    @ApiOperation(value = "按天查询资金报表", notes = "按天查询资金报表-总视图 + 会员自身按日明细")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findFundReportPage(FundStatementModel model) {
        return R.ok().put("page", fundStatementService.findFundReportPage(model));
    }

    @GetMapping("/totalInfo")
    @RequiresPermissions("analysis:fundReport:list")
    @ApiOperation(value = "总体详情", notes = "总体详情")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findFundTotalInfo(FundStatementModel model) {
        return R.ok().put("info", fundStatementService.findFundTotalInfo(model));
    }

    @GetMapping("/depotList")
    @RequiresPermissions("analysis:fundReport:list")
    @ApiOperation(value = "平台彩金", notes = "平台彩金")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findDepotPayoutList(FundStatementModel model) {
        return R.ok().put("page", fundStatementService.findDepotPayoutList(model));
    }


    @GetMapping("/tagencyList")
    @RequiresPermissions("analysis:fundReport:list")
    @ApiOperation(value = "总代列表", notes = "总代列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findTagencyList(FundStatementModel model) {
        return R.ok().put("page", fundStatementService.findTagencyList(model));
    }

    @GetMapping("/agentList")
    @RequiresPermissions("analysis:fundReport:agentList")
    @ApiOperation(value = "代理日期资金报表", notes = "总代列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R agentList(FundStatementModel model) {
        long startTime = System.currentTimeMillis();
        R r= R.ok().put("page", fundStatementService.agentList(model));
        long endTime = System.currentTimeMillis();
        log.info("===fundReportAgentList==={}执行查询代理日期报表查询时间范围:{}-{} 耗时{}秒", getUser().getUsername(), model.getStartTime(), model.getEndTime(), (endTime- startTime)/1000);
        return r;
    }

    @GetMapping("/tagencyFundList")
    @RequiresPermissions("analysis:fundReport:tagencyFundList")
    @ApiOperation(value = "代理资金报表", notes = "总代列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R tagencyFundList(FundStatementModel model) {
        return R.ok().put("page", fundStatementService.tagencyFundList(model));
    }



//    @GetMapping("/memberList")
//    @RequiresPermissions("analysis:fundReport:list")
//    @ApiOperation(value = "资金报表--下级会员列表--废弃", notes = "资金报表--下级会员列表--废弃")
//    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
//    public R findMemberList(FundStatementModel model) {
//        return R.ok().put("page", fundStatementService.findMemberList(model));
//    }

    @GetMapping("/agentSubMbrList")
    @RequiresPermissions("analysis:fundReport:list")
    @ApiOperation(value = "资金报表--代理直属会员列表", notes = "资金报表--代理直属会员列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R agentSubMbrList(FundStatementModel model) {
        return R.ok().put("page", fundStatementService.agentSubMbrList(model));
    }

    @GetMapping("/judgeTagency")
//    @RequiresPermissions("analysis:fundReport:list")
    @ApiOperation(value = "判断总代子代", notes = "判断总代子代")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R judgeTagency(@RequestParam(value = "agyAccount", required = false) String agyAccount) {
        return R.ok().put("info", fundStatementService.judgeTagency(agyAccount));
    }

    @GetMapping("/totalList")
    @RequiresPermissions("analysis:fundReport:list")
    @ApiOperation(value = "子代代理视图汇总表头", notes = "子代代理视图汇总表头")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R totalList(FundStatementModel model) {
        return R.ok().put(fundStatementService.totalList(model));
    }

    @GetMapping("/totalMbrList")
    @RequiresPermissions("analysis:fundReport:list")
    @ApiOperation(value = "会员的视图切换表头", notes = "会员的视图切换表头")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R totalMbrList(FundStatementModel model) {
        return R.ok().put(fundStatementService.totalMbrList(model));
    }

    @GetMapping("/mbrSubMbrList")
    @RequiresPermissions("analysis:fundReport:list")
    @ApiOperation(value = "资金报表--会员下级会员列表", notes = "会员下级会员列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R mbrSubMbrList(FundStatementModel model) {
        return R.ok().put(fundStatementService.mbrSubMbrList(model));
    }

    @GetMapping("/totalInfoMbrSelf")
    @RequiresPermissions("analysis:fundReport:list")
    @ApiOperation(value = "会员自身数据汇总", notes = "会员自身数据汇总-不含下级")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R totalInfoMbrSelf(FundStatementModel model) {
        return R.ok().put(fundStatementService.findFundTotalInfoMbrSelf(model));
    }

    @GetMapping("/listDayMbr")
    @RequiresPermissions("analysis:fundReport:list")
    @ApiOperation(value = "会员(含自身及下级)按天明细", notes = "会员(含自身及下级)按天明细")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findFundReportMbrPage(FundStatementModel model) {
        return R.ok().put("page", fundStatementService.findFundReportMbrPage(model));
    }


    @GetMapping("/exportTagencyList")
    @ApiOperation(value = "导出文件", notes = "总代列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R exportTagencyList(FundStatementModel model) {
        fundStatementService.exportTagencyList(model, getUserId(), exportTagencyListModule);
        return R.ok();
    }

    @GetMapping("/exportList")
    @ApiOperation(value = "导出文件", notes = "总代列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R exportList(FundStatementModel model) {
        fundStatementService.exportList(model, getUserId(), exportListModule);
        return R.ok();
    }


    @GetMapping("/exportAgentSubMbrList")
    @ApiOperation(value = "导出文件", notes = "总代列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R exportAgentSubMbrList(FundStatementModel model) {
        fundStatementService.exportAgentSubMbrList(model, getUserId(), agentSubMbrListModule);
        return R.ok();
    }


    @GetMapping("/exportAgentList")
    @RequiresPermissions("analysis:fundReport:exportAgentList")
    @ApiOperation(value = "代理日期资金报表", notes = "总代列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R exportAgentList(FundStatementModel model){
        long startTime = System.currentTimeMillis();
        R r = R.ok().put("page", fundStatementService.exportAgentList(model,  getUserId(), exportAgentList));
        long endTime = System.currentTimeMillis();
        log.info("===exportfundReportAgentList==={}导出查询代理日期报表查询时间范围:{}-{} 耗时{}秒", getUser().getUsername(), model.getStartTime(), model.getEndTime(), (endTime- startTime)/1000);
        return r;
    }

    @GetMapping("/exportTagencyFundList")
    @RequiresPermissions("analysis:fundReport:exportTagencyFundList")
    @ApiOperation(value = "代理资金报表", notes = "总代列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R exportTagencyFundList(FundStatementModel model){
        return R.ok().put("page", fundStatementService.exportTagencyFundList(model,  getUserId(), exportTagencyFundList));
    }


    @GetMapping("/checkFile")
    @ApiOperation(value = "检查文件", notes = "总代列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R checkFile(@RequestParam("module")String module) {

        // 查询用户的module下载记录
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(getUserId(), module);
        if(null != record){
            String  fileName = "";
            if(module.equals(exportListModule)){
                fileName = "资金报表.xls";
            }
            if(module.equals(exportTagencyListModule)){
                fileName = "代理账号资金报表.xls";
            }
            if(module.equals(agentSubMbrListModule)){
                fileName = "会员资金报表.xls";
            }
            if(module.equals(exportAgentList)){
                fileName = "代理资金日期报表.xls";
            }
            if(module.equals(exportTagencyFundList)){
                fileName = "代理资金会员报表.xls";
            }
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);

    }

}
