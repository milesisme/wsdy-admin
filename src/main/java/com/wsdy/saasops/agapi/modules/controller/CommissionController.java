package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.service.CommReportService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/agapi/n2")
@Api(tags = "佣金报表")
public class CommissionController {

    @Autowired
    private CommReportService commReportService;

    @AgentLogin
    @GetMapping("/finCommission")
    @ApiOperation(value = "佣金报表 type=0 本月 1上月", notes = "佣金报表")
    public R finCommissionReport(@RequestParam("type") @NotNull Integer type, HttpServletRequest request) {
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().put(commReportService.finCommission(agentAccount, type));
    }
}
