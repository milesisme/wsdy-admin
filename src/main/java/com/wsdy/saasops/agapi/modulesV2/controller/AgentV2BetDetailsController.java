package com.wsdy.saasops.agapi.modulesV2.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modulesV2.service.AgentV2BetDetailsFundService;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.AESUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Slf4j
@RestController
@RequestMapping("/agapi/v2/analysis/betDetails")
@Api(value = "Analysis", tags = "经营分析")
public class AgentV2BetDetailsController extends AbstractController {

    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    private final String betModule = "AgentBetDetail";

    @Value("${agentv2.analysis.betDetails.excel.path:excelTemplate/agentv2/客户交易记录.xls}")
    private String betDetailsExcelPath;
    @Autowired
    private AgentV2BetDetailsFundService v2BetDetailsFundService;
    @Autowired
    private TCpSiteService tCpSiteService;


    /**
     * 查询投注明细统计
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @AgentLogin
    @RequestMapping("/finalBetDetailsAll")
    @ApiOperation(value = "投注记录", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R betDetailsData(@RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize,
                            GameReportQueryModel model,
                            HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        model.setSiteCode(CommonUtil.getSiteCode());
        return R.ok().put("page", v2BetDetailsFundService
                .getBkRptBetListPage(account, pageNo, pageSize, model))
                .put("time", v2BetDetailsFundService
                        .getPlatformMaxTime(CommonUtil.getSiteCode()));
    }

    @AgentLogin
    @GetMapping("/exportBetDetails")
    @ApiOperation(value = "导出投注记录", notes = "导出投注记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R exportBetDetails(GameReportQueryModel model, HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        SysFileExportRecord record = v2BetDetailsFundService.betDetailsExportExcel(model,
                account, betModule, betDetailsExcelPath, model.getGametype());
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @AgentLogin
    @GetMapping("checkFile")
    @ApiOperation(value = "查询文件是否可下载", notes = "查询文件是否可下载")
    public R checkFile(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        Long userId = Long.valueOf(account.getId());
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, betModule);
        if (null != record) {
            String commonFileName = betDetailsExcelPath.substring(betDetailsExcelPath.lastIndexOf("/") + 1, betDetailsExcelPath.length());
            record.setDownloadFileName(commonFileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }

    @GetMapping("downloadExcel")
    @ApiOperation(value = "下载excel文件", notes = "下载excel文件")
    public void downloadMbrAccountInfoExcel(@RequestParam("fileName") String fileName, @RequestParam("downloadFileName") String downloadFileName, @RequestParam("SToken") String SToken,
                                            HttpServletRequest request, HttpServletResponse response) {
        Assert.isNull(fileName, "fileId不能为空");
        try {
            SToken = URLDecoder.decode(SToken, "utf-8");
            SToken = AESUtil.decrypt(SToken);
        } catch (UnsupportedEncodingException e) {
            log.error("downloadMbrAccountInfoExcel==下载excel文件URLDecoder错误==" + e);
        } catch (Exception e) {
            log.error("downloadMbrAccountInfoExcel==下载excel文件解析SToken错误==" + e);
        }
        request.setAttribute(SystemConstants.SCHEMA_NAME, tCpSiteService.getSchemaName(SToken));
        sysFileExportRecordService.downloadFile(response, fileName, downloadFileName);
    }

    @AgentLogin
    @GetMapping("egGameNameList")
    public R egGameNameList() {
        return R.ok().put(v2BetDetailsFundService.egGameNameList());
    }

    @AgentLogin
    @RequestMapping("/finalGameResults")
    @ApiOperation(value = "游戏结果", notes = "游戏结果")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R finalGameResults(@RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize,
                              GameReportQueryModel model,
                              HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        model.setSiteCode(CommonUtil.getSiteCode());
        model.setIsSubtotal(Boolean.FALSE);
        return R.ok().putPage(v2BetDetailsFundService
                .getBkRptBetListPage(account, pageNo, pageSize, model));
    }

    @RequestMapping("/finalBetting")
    @ApiOperation(value = "同局注单", notes = "游戏结果")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R finalBetting(GameReportQueryModel model) {
        Assert.isBlank(model.getSerialId(),"同局注单不能为空");
        model.setSiteCode(CommonUtil.getSiteCode());
        model.setIsSubtotal(Boolean.FALSE);
        return R.ok().put(v2BetDetailsFundService.finalBetting(null, model));
    }
}

