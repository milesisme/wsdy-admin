package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.dto.AgentChargeMDto;
import com.wsdy.saasops.modules.agent.dto.DepotCostDto;
import com.wsdy.saasops.modules.agent.entity.*;
import com.wsdy.saasops.modules.agent.service.AgentExportService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.isNull;


@RestController
@RequestMapping("/bkapi/agent/export")
@Api(tags = "代理导出")
public class AgentExportController extends AbstractController {

    @Autowired
    private AgentExportService exportService;

    @Value("${agent.review.excel.path:excelTemplate/agent/代理申请.xls}")
    private String agentReviewExcelPath;
    private final String reviewmodule = "agyAccountReviewExport";

    @Value("${agent.agentDomain.excel.path:excelTemplate/agent/代理域名列表.xls}")
    private String agentDomainExport;
    private final String domainModule = "agentDomainExcel";

    @Value("${agent.agentContract.excel.path:excelTemplate/agent/契约管理.xls}")
    private String agentContractExport;
    private final String contractModule = "agentContractExcel";

    @Value("${agent.agentList.excel.path:excelTemplate/agent/代理列表.xls}")
    private String agentListExport;
    private final String agentListModule = "agentListExcel";
    
    @Value("${agent.upperScoreRecord.excel.path:excelTemplate/agent/代充记录.xls}")
    private String upperScoreRecordExport;
    private final String upperScoreRecordModule = "upperScoreRecordModule";

    @Value("${agent.commissionModule.excel.path:excelTemplate/agent/佣金报表.xls}")
    private String commissionReportExport;
    private final String commissionReportModule = "commissionReportModule";

    @Value("${agent.accountChange.excel.path:excelTemplate/agent/账变流水.xls}")
    private String accountChangeReportExport;
    private final String accountChangeModule = "accountChangeReportModule";

    @Value("${agent.depotCost.excel.path:excelTemplate/agent/平台费报表.xls}")
    private String depotCostReportExport;
    private final String depotCostChangeModule = "depotCostReportModule";

    @Value("${agent.agentOnLine.excel.path:excelTemplate/agent/代理入款.xls}")
    private String agentOnLineReportExport;
    private final String agentOnLineModule = "agentOnLineModule";

    @Value("${agent.accWithdraw.excel.path:excelTemplate/agent/代理提款.xls}")
    private String accWithdrawReportExport;
    private final String accWithdrawModule = "accWithdrawModule";

    @Value("${agent.agentAudit.excel.path:excelTemplate/agent/资金调整.xls}")
    private String agentAuditReportExport;
    private final String agentAuditModule = "agentAuditModule";

    @Value("${agent.commissionReview.excel.path:excelTemplate/agent/佣金-风控审核.xls}")
    private String commissionReviewReportExport;
    private final String commissionReviewModule = "commissionReviewModule";
    
    @Value("${agent.commissionReview.excel.path:excelTemplate/agent/上级返佣报表.xls}")
    private String commissionAllSubListReportExport;
    private final String commissionAllSubModule = "commissionAllSubModule";

    @Value("${agent.commissionFreedList.excel.path:excelTemplate/agent/佣金-财务审核.xls}")
    private String commissionFreedListReportExport;
    private final String commissionFreedListModule = "commissionFreedListModule";

    @Value("${agent.agentDepotCost.excel.path:excelTemplate/agent/平台费报表-代理.xls}")
    private String agentDepotCostReportExport;
    private final String agentDepotCostModule = "agentDepotCostModule";

    @Value("${agent.accountDepotCost.excel.path:excelTemplate/agent/平台费报表-会员.xls}")
    private String accountDepotCostReportExport;
    private final String accountDepotCostModule = "accountDepotCostModule";

    @Value("${agent.accountDepotCost.excel.path:excelTemplate/agent/服务费报表-代理.xls}")
    private String serviceChargAgentReportExport;
    private final String serviceChargAgentCostModule = "serviceChargAgentCostModule";

    @Value("${agent.accountDepotCost.excel.path:excelTemplate/agent/服务费报表-会员.xls}")
    private String serviceChargAccountReportExport;
    private final String serviceChargAccountCostModule = "serviceChargAccountCostModule";

    private final String agyChannelLogModule = "agyChannelLogModule";
    @Value("${agent.channel.excel.path}")
	private String agyChannelLogexcelPath;

    @GetMapping("/checkFile")
    @ApiOperation(value = "导出文件是否可下载", notes = "代导出文件是否可下载\"")
    public R checkFile(@RequestParam("module") String module) {
        String path = "";
        if (reviewmodule.equals(module)) {
            path = agentReviewExcelPath;
        }
        if (domainModule.equals(module)) {
            path = agentDomainExport;
        }
        if (contractModule.equals(module)) {
            path = agentContractExport;
        }
        if (agentListModule.equals(module)) {
            path = agentListExport;
        }
        if (upperScoreRecordModule.equals(module)) {
            path = upperScoreRecordExport;
        }
        if (commissionReportModule.equals(module)) {
            path = commissionReportExport;
        }
        if (accountChangeModule.equals(module)) {
            path = accountChangeReportExport;
        }
        if (depotCostChangeModule.equals(module)) {
            path = depotCostReportExport;
        }
        if (agentOnLineModule.equals(module)) {
            path = agentOnLineReportExport;
        }
        if (accWithdrawModule.equals(module)) {
            path = accWithdrawReportExport;
        }
        if (agentAuditModule.equals(module)) {
            path = agentAuditReportExport;
        }
        if (commissionReviewModule.equals(module)) {
            path = commissionReviewReportExport;
        }
        if (commissionAllSubModule.equals(module)) {
        	path = commissionAllSubListReportExport;
        }
        if (commissionFreedListModule.equals(module)) {
            path = commissionFreedListReportExport;
        }
        if (agentDepotCostModule.equals(module)) {
            path = agentDepotCostReportExport;
        }
        if (accountDepotCostModule.equals(module)) {
            path = accountDepotCostReportExport;
        }
        if (serviceChargAgentCostModule.equals(module)) {
            path = serviceChargAgentReportExport;
        }
        if (serviceChargAccountCostModule.equals(module)) {
            path = serviceChargAccountReportExport;
        }
        if (agyChannelLogModule.equals(module)){
        	path = agyChannelLogexcelPath;
        }
        return exportService.checkFile(module, getUser().getUserId(), path);
    }

    @GetMapping("agyAccountReviewExport")
    @RequiresPermissions("agent:review:export")
    @ApiOperation(value = "代理申请导出", notes = "代理申请导出")
    public R agyAccountReviewExport(@ModelAttribute AgentAccount agentAccount) {
        if (isNull(agentAccount.getStatus())) {
            agentAccount.setReviewStatus(Boolean.TRUE);
        }
        SysFileExportRecord record = exportService.agyAccountReviewExcel(
                agentAccount, getUserId(), agentReviewExcelPath, reviewmodule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("agentDomainExport")
    @RequiresPermissions("agent:domain:export")
    @ApiOperation(value = "代理域名导出", notes = "代理域名导出")
    public R agentDomainExport(@ModelAttribute AgyDomain agyDomain) {
        SysFileExportRecord record = exportService.agentDomainExport(
                agyDomain, getUserId(), agentDomainExport, domainModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("agentContractExport")
    @RequiresPermissions("agent:contract:export")
    @ApiOperation(value = "契约导出", notes = "契约导出")
    public R agentContractExport() {
        SysFileExportRecord record = exportService.agentContractExport(
                getUserId(), agentContractExport, contractModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("agentListExport")
    @RequiresPermissions("agent:account:exportv")
    @ApiOperation(value = "代理列表导出", notes = "代理列表导出")
    public R agentListExport(@ModelAttribute AgentAccount agentAccount) {
        SysFileExportRecord record = exportService.agentListExport(
                agentAccount, getUserId(), agentListExport, agentListModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }
    
    @GetMapping("upperScoreRecordExport")
    @RequiresPermissions("agent:upperScoreRecord:export")
    @ApiOperation(value = "代充记录导出", notes = "代充记录导出")
    public R upperScoreRecordExport(@ModelAttribute AgyBillDetail billDetail) {
        SysFileExportRecord record = exportService.upperScoreRecordExport(
                billDetail, getUserId(), upperScoreRecordExport, upperScoreRecordModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("commissionReportExport")
    @RequiresPermissions("agent:commissionReport:export")
    @ApiOperation(value = "佣金报表导出", notes = "佣金报表导出")
    public R commissionReportExport(@ModelAttribute AgyCommission commission) {
        SysFileExportRecord record = exportService.commissionReportExport(
                commission, getUserId(), commissionReportExport, commissionReportModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("agentAccountChangeExport")
    @RequiresPermissions("agent:repotAgentChange:export")
    @ApiOperation(value = "账变流水导出", notes = "账变流水导出")
    public R agentAccountChangeExport(@ModelAttribute AgyBillDetail billDetail) {
        SysFileExportRecord record = exportService.agentAccountChangeExport(
                billDetail, getUserId(), accountChangeReportExport, accountChangeModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @Deprecated
    @GetMapping("commissionDepotCostExport")
    @RequiresPermissions("agent:commissionDepotCost:export")
    @ApiOperation(value = "平台费报表导出", notes = "平台费报表导出")
    public R commissionDepotCostExport(@ModelAttribute DepotCostDto dto) {
        SysFileExportRecord record = exportService.commissionDepotCostExport(
                dto, getUserId(), depotCostReportExport, depotCostChangeModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("agentOnLineExport")
    @RequiresPermissions("agent:onLine:export")
    @ApiOperation(value = "代理入款导出", notes = "代理入款导出")
    public R agentOnLineExport(@ModelAttribute AgentDeposit fundDeposit) {
        SysFileExportRecord record = exportService.agentOnLineExport(
                fundDeposit, getUserId(), agentOnLineReportExport, agentOnLineModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("accWithdrawExport")
    @RequiresPermissions("agent:accWithdraw:export")
    @ApiOperation(value = "代理提款导出", notes = "代理提款导出")
    public R accWithdrawExport(@ModelAttribute AgyWithdraw accWithdraw) {
        SysFileExportRecord record = exportService.accWithdrawExport(
                accWithdraw, getUserId(), accWithdrawReportExport, accWithdrawModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("agentAuditExport")
    @RequiresPermissions("agent:audit:export")
    @ApiOperation(value = "资金调整导出", notes = "资金调整导出")
    public R agentAuditExport(@ModelAttribute AgentAudit fundAudit) {
        SysFileExportRecord record = exportService.agentAuditExport(
                fundAudit, getUserId(), agentAuditReportExport, agentAuditModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("commissionAllSubListExport")
    @RequiresPermissions("agent:commissionAllSubList:export")
    @ApiOperation(value = "上级返佣报表导出", notes = "上级返佣报表导出")
    public R commissionAllSubListExport(@ModelAttribute AgyCommission commission) {
        SysFileExportRecord record = exportService.commissionAllSubListExport(
                commission, getUserId(), commissionAllSubListReportExport, commissionAllSubModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }
    
    @GetMapping("commissionReviewExport")
    @RequiresPermissions("agent:commissionReview:export")
    @ApiOperation(value = "佣金-风控审核导出", notes = "佣金-风控审导出")
    public R commissionReviewExport(@ModelAttribute AgyCommission commission) {
    	SysFileExportRecord record = exportService.commissionReviewExport(
    			commission, getUserId(), commissionReviewReportExport, commissionReviewModule);
    	if (record == null) {
    		throw new R200Exception("正在处理中!");
    	}
    	return R.ok();
    }

    @GetMapping("commissionFreedListExport")
    @RequiresPermissions("agent:commissionFreedList:export")
    @ApiOperation(value = "佣金-财务审核导出", notes = "佣金-财务审核导出")
    public R commissionFreedListExport(@ModelAttribute AgyCommission commission) {
        SysFileExportRecord record = exportService.commissionFreedListExport(
                commission, getUserId(), commissionFreedListReportExport, commissionFreedListModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("agentDepotCostExport")
    @RequiresPermissions("agent:commissionDepotCost:export")
    @ApiOperation(value = "平台费报表-代理导出", notes = "平台费报表-代理导出")
    public R agentDepotCostExport(@ModelAttribute DepotCostDto depotCostDto) {
        SysFileExportRecord record = exportService.agentDepotCostExport(
                depotCostDto, getUserId(), agentDepotCostReportExport, agentDepotCostModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("accountDepotCostExport")
    @RequiresPermissions("agent:commissionDepotCost:export")
    @ApiOperation(value = "平台费报表-会员导出", notes = "平台费报表-会员导出")
    public R accountDepotCostExport(@ModelAttribute DepotCostDto depotCostDto) {
        SysFileExportRecord record = exportService.accountDepotCostExport(
                depotCostDto, getUserId(), accountDepotCostReportExport, accountDepotCostModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("serviceChargAgentCostExport")
    @RequiresPermissions("agent:commission:servicechargExport")
    @ApiOperation(value = "服务费费报表-代理导出", notes = "服务费费报表-代理导出")
    public R serviceChargAgentCostExport(@ModelAttribute AgentChargeMDto dto) {
        SysFileExportRecord record = exportService.serviceChargAgentCostExport(
                dto, getUserId(), serviceChargAgentReportExport, serviceChargAgentCostModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @GetMapping("serviceChargAccountCostExport")
    @RequiresPermissions("agent:commission:servicechargExport")
    @ApiOperation(value = "服务费费报表-会员导出", notes = "服务费费报表-代理导出")
    public R serviceChargAccountCostExport(@ModelAttribute AgentChargeMDto dto) {
        SysFileExportRecord record = exportService.serviceChargAccountCostExport(
                dto, getUserId(), serviceChargAccountReportExport, serviceChargAccountCostModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }
}
