package com.wsdy.saasops.modules.agent.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.service.CommissionReportService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/bkapi/agent/comm")
@Api(tags = "佣金列表")
public class CommissionReportController extends AbstractController {

    @Autowired
    private CommissionReportService commissionReportService;

    @GetMapping("agentReportList")
    @RequiresPermissions("agent:report:list")
    @ApiOperation(value = "代理报表", notes = "代理报表")
    public R agentReportList(
            @PathVariable("startTime") String startTime,
            @PathVariable("endTime") String endTime,
            @PathVariable("accountId") Integer accountId,
            @RequestParam("pageNo") @NotNull Integer pageNo,
            @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put(commissionReportService.agentReportList(startTime, endTime, accountId, pageNo, pageSize));
    }
}
