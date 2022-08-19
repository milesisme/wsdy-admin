package com.wsdy.saasops.agapi.modulesV2.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2LoginLogDto;
import com.wsdy.saasops.agapi.modulesV2.service.AgentV2LoginLogService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
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


@RestController
@Slf4j
@RequestMapping("/agapi/v2/log/login")
@Api(tags = "外围系统-登入记录")
public class AgentV2LoginLogController extends AbstractController {
    @Autowired
    private AgentV2LoginLogService agentV2LoginLogService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    // 导出
    @Value("${agentv2.loginLog.excel.path}")
    private String LoginLogExcelPath;
    private final String LoginLogtModule = "loginLogtModule";

    @AgentLogin
    @GetMapping("/getLoginLogList")
    @ApiOperation(value = "获取登入日志列表", notes = "登入日志列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getLoginLogList(@ModelAttribute AgentV2LoginLogDto agentV2LoginLogDto, HttpServletRequest request) {
        // 参数校验 TODO
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        agentV2LoginLogDto.setAgyAccount(agentAccount.getAgyAccount());
        agentV2LoginLogDto.setAgyId(agentAccount.getId());

        // 默认搜索当前登录账号
        if(StringUtils.isEmpty(agentV2LoginLogDto.getSearchName())){
            agentV2LoginLogDto.setSearchName(agentAccount.getAgyAccount());
            agentV2LoginLogDto.setUserType("agent");
        }


        return R.ok().putPage(agentV2LoginLogService.getLoginLogListPage(agentV2LoginLogDto));
    }

    @AgentLogin
    @GetMapping("/getWinLosePayout")
    @ApiOperation(value = "获取登录日志-输赢金额", notes = "登录日志-输赢金额")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getWinLosePayout(@ModelAttribute AgentV2LoginLogDto agentV2LoginLogDto, HttpServletRequest request) {
        // 参数校验 TODO
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        agentV2LoginLogDto.setAgyAccount(agentAccount.getAgyAccount());
        agentV2LoginLogDto.setAgyId(agentAccount.getId());

        // 默认搜索当前登录账号
        if(StringUtils.isEmpty(agentV2LoginLogDto.getSearchName())){
            agentV2LoginLogDto.setSearchName(agentAccount.getAgyAccount());
            agentV2LoginLogDto.setUserType("agent");
        }

        return R.ok().put(agentV2LoginLogService.getWinLosePayout(agentV2LoginLogDto));
    }

    @AgentLogin
    @GetMapping("/exportLoginLogList")
    @ApiOperation(value = "导出登入日志列表", notes = "导出登入日志列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R exportLoginLogList(@ModelAttribute AgentV2LoginLogDto agentV2LoginLogDto, HttpServletRequest request){
        // 参数校验 TODO
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        agentV2LoginLogDto.setAgyAccount(agentAccount.getAgyAccount());
        agentV2LoginLogDto.setAgyId(agentAccount.getId());

        // 默认搜索当前登录账号
        if(StringUtils.isEmpty(agentV2LoginLogDto.getSearchName())){
            agentV2LoginLogDto.setSearchName(agentAccount.getAgyAccount());
            agentV2LoginLogDto.setUserType("agent");
        }
        SysFileExportRecord record = agentV2LoginLogService.exportLoginLogList(agentV2LoginLogDto,Long.valueOf(agentAccount.getId()),LoginLogtModule,LoginLogExcelPath);
        if(record == null){
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }

    @AgentLogin
    @GetMapping("/checkFile")
    @ApiOperation(value = "查询文件是否可下载",notes = "查询文件是否可下载")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字",  required = true, dataType = "Integer", paramType = "header")
    })
    public R checkFile(@RequestParam("module") String module, HttpServletRequest request){
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(Long.valueOf(agentAccount.getId()),module);
        if(null != record){
            String fileName = LoginLogExcelPath.substring(LoginLogExcelPath.lastIndexOf("/")+1,LoginLogExcelPath.length());
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }
}
