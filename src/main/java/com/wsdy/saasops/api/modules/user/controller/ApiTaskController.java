package com.wsdy.saasops.api.modules.user.controller;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.task.service.TaskAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/task")
@Api(value = "任务中心", tags = "任务中心")
public class ApiTaskController {

    @Autowired
    private TaskAccountService accountService;

    @Login
    @GetMapping("taskCenter")
    @ApiOperation(value = "任务中心", notes = "任务中心")
    public R taskCenter(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(accountService.taskCenter(accountId,null));
    }

    @Login
    @GetMapping("clickRate")
    @ApiOperation(value = "统计每个点击量", notes = "统计每个点击量")
    public R taskClickRate(@RequestParam Integer configId) {
        Assert.isNull(configId, "ID不能为空");
        accountService.taskClickRate(configId, CommonUtil.getSiteCode());
        return R.ok();
    }

    @Login
    @GetMapping("getTask")
    @ApiOperation(value = "领取任务", notes = "领取任务")
    public R getTask(@RequestParam Integer configId,
                     @RequestParam(value = "level", required = false) Integer level,
                     HttpServletRequest request) {
        Assert.isNull(configId, "ID不能为空");
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        accountService.getTask(configId, accountId, CommonUtil.getSiteCode(), loginName, level);
        return R.ok();
    }

    @Login
    @GetMapping("bonus")
    @ApiOperation(value = "明细", notes = "明细")
    public R taskBonus(HttpServletRequest request,
                       @RequestParam("pageNo") @NotNull Integer pageNo,
                       @RequestParam("pageSize") @NotNull Integer pageSize) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().putPage(accountService.taskBonus(accountId, pageNo, pageSize));
    }
}
