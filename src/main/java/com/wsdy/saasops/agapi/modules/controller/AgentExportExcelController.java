package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.dto.DirectMemberParamDto;
import com.wsdy.saasops.agapi.modules.dto.ReportParamsDto;
import com.wsdy.saasops.agapi.modules.service.AgentExportExcelService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.service.AgentExportService;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/agapi/agent/export")
@Api(tags = "代理导出")
public class AgentExportExcelController {

    @Autowired
    private AgentExportExcelService exportService;
    @Autowired
    private AgentExportService agentExportService;

    @Value("${agent.review.excel.path:excelTemplate/agent/直属会员.xls}")
    private String directMemberExcelPath;
    private final String directMemberModule = "directMemberModule";

    @Value("${agent.review.excel.path:excelTemplate/agent/下级代理.xls}")
    private String subAgentListExcelPath;
    private final String subAgentListModule = "subAgentListModule";

    @Value("${agent.review.excel.path:excelTemplate/agent/分线代理.xls}")
    private String superiorCloneExcelPath;
    private final String superiorCloneModule = "superiorCloneModule";

    @Value("${subagent.review.excel.path:excelTemplate/agent/下级代理直属会员.xls}")
    private String subAgentAccountPath;
    private final String subAgentAccountModule = "subAgentAccountModule";

    @Value("${subagent.finaceReport.excel.path:excelTemplate/agent/财务报表.xls}")
    private String finaceReportAgentPath;
    private final String finaceReportModule = "finaceReportModule";


	
    @AgentLogin
    @GetMapping("/checkFile")
    @ApiOperation(value = "查询文件是否可下载", notes = "查询文件是否可下载")
    public R checkFile(@RequestParam("module") String module, HttpServletRequest request) {
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        String path = directMemberExcelPath;
        if (subAgentListModule.equals(module)) {
            path = subAgentListExcelPath;
        }
        if (superiorCloneModule.equals(module)){
            path = superiorCloneExcelPath;
        }
        if (finaceReportModule.equals(module)){
            path = finaceReportAgentPath;
        }
        return agentExportService.checkFile(module, Long.valueOf(agentAccount.getId()), path);
    }

    @AgentLogin
    @GetMapping("directMemberExport")
    @ApiOperation(value = "直属会员导出", notes = "代理申请导出")
    public R directMemberExport(@ModelAttribute DirectMemberParamDto paramDto,
                                HttpServletRequest request) {
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (agentAccount.getAttributes() == Constants.EVNumber.one) {
            paramDto.setSubCagencyId(agentAccount.getId());
        } else if (agentAccount.getAttributes() == Constants.EVNumber.four) {
            paramDto.setCagencyId(agentAccount.getAgentId());
        } else {
            paramDto.setCagencyId(agentAccount.getId());
        }
        SysFileExportRecord record = exportService.directMemberExport(
                paramDto, Long.valueOf(agentAccount.getId()), directMemberExcelPath, directMemberModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @AgentLogin
    @GetMapping("subAgentListExport")
    @ApiOperation(value = "下级代理导出", notes = "下级代理导出")
    public R subAgentListExport(@ModelAttribute DirectMemberParamDto paramDto,
                                HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        paramDto.setSubAgentId(account.getId());
        SysFileExportRecord record = exportService.subAgentListExport(
                paramDto, Long.valueOf(account.getId()), subAgentListExcelPath, subAgentListModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @AgentLogin
    @GetMapping("superiorCloneExport")
    @ApiOperation(value = "分线代理导出", notes = "分线代理导出")
    public R superiorCloneExport(@ModelAttribute DirectMemberParamDto paramDto,
                                HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        paramDto.setCagencyId(account.getId());
        SysFileExportRecord record = exportService.superiorCloneExport(
                paramDto, Long.valueOf(account.getId()), superiorCloneExcelPath, superiorCloneModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }


    @AgentLogin
    @GetMapping("subAgentAccountExport")
    @ApiOperation(value = "下级代理直属会员导出", notes = "下级代理直属会员导出")
    public R subAgentAccountExport(@ModelAttribute DirectMemberParamDto paramDto,
                                HttpServletRequest request) {
        Assert.isNull(paramDto.getAgyAccount(), "代理名不能为空");
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        SysFileExportRecord record = exportService.subAgentAccountExport(
                paramDto, Long.valueOf(agentAccount.getId()), subAgentAccountPath, subAgentAccountModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @AgentLogin
    @GetMapping("finaceReportExport")
    @ApiOperation(value = "财务报表", notes = "财务报表")
    public R finaceReportExport(@ModelAttribute ReportParamsDto dto,
                                HttpServletRequest request) {
        Assert.isBlank(dto.getStartTime(), "开始时间不能为空");
        Assert.isBlank(dto.getEndTime(), "结束时间不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        dto.setCagencyId(account.getId());
        if (account.getAttributes() == Constants.EVNumber.one) {
            dto.setCagencyId(null);
            dto.setSubCagencyId(account.getId());
        }
        SysFileExportRecord record = exportService.finaceReportExport(
                dto, Long.valueOf(account.getId()), finaceReportAgentPath, finaceReportModule);
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

}
