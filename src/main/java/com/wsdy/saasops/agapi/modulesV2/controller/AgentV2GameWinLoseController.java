package com.wsdy.saasops.agapi.modulesV2.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2WinLostReportDto;
import com.wsdy.saasops.agapi.modulesV2.service.AgentV2GameWinLoseService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


@RestController
@Slf4j
@RequestMapping("/agapi/v2/gameWinLose")
@Api(tags = "外围系统-输赢报表")
public class AgentV2GameWinLoseController extends AbstractController {
    @Autowired
    private AgentV2GameWinLoseService agentV2GameWinLoseService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    // 导出
    @Value("${agentv2.gameWinLose.winLostReportList.excel.path}")
    private String winLostReportListExcelPath;
    @Value("${agentv2.gameWinLose.winLostListLevel.excel.path}")
    private String winLostListLevelExcelPath;
    @Value("${agentv2.gameWinLose.winLostListLevelMbr.excel.path}")
    private String winLostListLevelMbrExcelPath;
    private final String winLostReportListModule = "winLostReportList";
    private final String winLostListLevelModule = "winLostListLevel";
    private final String winLostListLevelMbrModule = "winLostListLevelMbr";


    @AgentLogin
    @GetMapping("/getBetLastDate")
    @ApiOperation(value = "更新时间", notes = "查询输赢报表最后一次更新时间")
    public R getBetLastDate() {
        String siteCode= CommonUtil.getSiteCode();
        Map<String,String> map=new HashMap<>(2);
        String betLastDateStr = agentV2GameWinLoseService.getBetLastDate(siteCode);
        map.put("betLastDate",betLastDateStr);
        return  R.ok().put(map);
    }

    @AgentLogin
    @GetMapping("/findWinLostReportList")
    @ApiOperation(value = "获取输赢报表汇总", notes = "汇总表头")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findWinLostReportList(@ModelAttribute AgentV2WinLostReportDto reportModelDto, HttpServletRequest request) {
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if(StringUtils.isEmpty(reportModelDto.getAgyAccount())){
            reportModelDto.setAgyAccount(agentAccount.getAgyAccount());
        }
        return R.ok().put("data",agentV2GameWinLoseService.findWinLostReportList(reportModelDto)).put("sum",agentV2GameWinLoseService.findWinLostReportListMbrSum(reportModelDto));
    }

    @AgentLogin
    @GetMapping("/findWinLostListLevel")
    @ApiOperation(value = "获取下级代理明细", notes = "获取下级代理明细")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findWinLostListLevel(@ModelAttribute AgentV2WinLostReportDto reportModelDto,  HttpServletRequest request) {
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if(StringUtils.isEmpty(reportModelDto.getAgyAccount())){
            reportModelDto.setAgyAccount(agentAccount.getAgyAccount());
        }
        return R.ok().putPage(agentV2GameWinLoseService.findWinLostListLevel(reportModelDto)).put("total",agentV2GameWinLoseService.findWinLostListLevelSum(reportModelDto));
    }

    @AgentLogin
    @GetMapping("/findWinLostListLevelMbr")
    @ApiOperation(value = "获取直属会员明细", notes = "获取直属会员明细")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findWinLostListLevelMbr(@ModelAttribute AgentV2WinLostReportDto reportModelDto,  HttpServletRequest request) {
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if(StringUtils.isEmpty(reportModelDto.getAgyAccount())){
            reportModelDto.setAgyAccount(agentAccount.getAgyAccount());
        }
        return R.ok().putPage(agentV2GameWinLoseService.findWinLostListLevelMbr(reportModelDto)).put("total",agentV2GameWinLoseService.findWinLostListLevelMbrSum(reportModelDto));
    }


    @AgentLogin
    @GetMapping("/exportWinLostReportList")
    @ApiOperation(value = "导出输赢报表汇总", notes = "导出汇总表头")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R exportWinLostReportList(@ModelAttribute AgentV2WinLostReportDto reportModelDto, HttpServletRequest request){
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if(StringUtils.isEmpty(reportModelDto.getAgyAccount())){
            reportModelDto.setAgyAccount(agentAccount.getAgyAccount());
        }
        SysFileExportRecord record = null;
        if(winLostReportListModule.equals(reportModelDto.getModule())){ // 表头
            record = agentV2GameWinLoseService.exportWinLostReportList(reportModelDto,Long.valueOf(agentAccount.getId()),winLostReportListModule,winLostReportListExcelPath);
        }
        if(winLostListLevelModule.equals(reportModelDto.getModule())){  // 下级代理
            record = agentV2GameWinLoseService.exportWinLostListLevel(reportModelDto,Long.valueOf(agentAccount.getId()),winLostListLevelModule,winLostListLevelExcelPath);
        }
        if(winLostListLevelMbrModule.equals(reportModelDto.getModule())){   // 下级会员
            record = agentV2GameWinLoseService.exportWinLostListLevelMbr(reportModelDto,Long.valueOf(agentAccount.getId()),winLostListLevelMbrModule,winLostListLevelMbrExcelPath);
        }

        if(record == null){
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @AgentLogin
    @GetMapping("/checkFile")
    @ApiOperation(value = "查询文件是否可下载",notes = "查询文件是否可下载")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字",  required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "module", value = "汇总：winLostReportList 下级代理：winLostListLevel  下级会员：winLostListLevelMbr", required = true, dataType = "String")
    })
    public R checkFile(@RequestParam("module") String module, HttpServletRequest request){
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(Long.valueOf(agentAccount.getId()),module);
        if(null != record){
            String fileName = "";
            if(winLostReportListModule.equals(module)){
                fileName = winLostReportListExcelPath.substring(winLostReportListExcelPath.lastIndexOf("/")+1,winLostReportListExcelPath.length());
            }
            if(winLostListLevelModule.equals(module)){
                fileName = winLostListLevelExcelPath.substring(winLostListLevelExcelPath.lastIndexOf("/")+1,winLostListLevelExcelPath.length());
            }
            if(winLostListLevelMbrModule.equals(module)){
                fileName = winLostListLevelMbrExcelPath.substring(winLostListLevelMbrExcelPath.lastIndexOf("/")+1,winLostListLevelMbrExcelPath.length());
            }
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }
}
