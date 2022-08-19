package com.wsdy.saasops.modules.analysis.controller;

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
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/bkapi/analysis/mbrFundReport")
@Api(value = "MbrFundStatement", tags = "会员资金报表")
public class MbrFundStatementController extends AbstractController {

	@Value("${mbr.fund.excel.path}")
	private String mbrAccountExcelTempPath;
	
    private final String exportListModule = "mbrExportList";
    @Autowired
    private FundStatementService fundStatementService;

    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    @GetMapping("/list")
    @RequiresPermissions("analysis:mbrFundReport:list")
    @ApiOperation(value = "会员资金报表", notes = "会员资金报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findFundReportPage(FundStatementModel model) {
    	return R.ok().put("page", fundStatementService.fundMbrList(model));
    }

    @GetMapping("/exportList")
    @ApiOperation(value = "导出文件", notes = "会员资金报表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R exportList(FundStatementModel model) {
        SysFileExportRecord record = fundStatementService.exportListMbr(model, getUserId(), exportListModule, mbrAccountExcelTempPath);
        if(record == null){
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("/checkFile")
    @ApiOperation(value = "检查文件", notes = "会员资金报表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R checkFile(@RequestParam("module")String module) {

        // 查询用户的module下载记录
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(getUserId(), module);
        if(null != record){
            String  fileName = "";
            if(module.equals(exportListModule)){
                fileName = "会员资金报表.xls";
            }
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);

    }

}
