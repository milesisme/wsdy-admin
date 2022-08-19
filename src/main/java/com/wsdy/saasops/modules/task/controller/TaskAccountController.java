package com.wsdy.saasops.modules.task.controller;

import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.task.entity.TaskBonus;
import com.wsdy.saasops.modules.task.entity.TaskConfig;
import com.wsdy.saasops.modules.task.service.TaskAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/task")
@Api(tags = "任务中心")
public class TaskAccountController extends AbstractController {

    @Autowired
    private TaskAccountService taskAccountService;

    @GetMapping("configList")
    @RequiresPermissions("task:account:configList")
    @ApiOperation(value = "任务中心配置", notes = "任务中心配置")
    public R agyAccountList() {
        return R.ok().put(taskAccountService.configList());
    }

    @GetMapping("configInfo")
    @RequiresPermissions("task:account:configList")
    @ApiOperation(value = "任务中心配置查询单个", notes = "任务中心配置查询单个")
    public R configInfo(@RequestParam("id") Integer id) {
        return R.ok().put(taskAccountService.configInfo(id));
    }

    @GetMapping("updateAvailable")
    @RequiresPermissions("task:account:updateAvailable")
    @ApiOperation(value = "启用状态", notes = "启用状态")
    public R updateAvailable(@RequestParam("id") Integer id,
                             @RequestParam("available") Integer available) {
        Assert.isNull(id, "id不能为空");
        Assert.isNull(available, "状态不能为空");
        taskAccountService.updateAvailable(id, available, getUser().getUsername());
        return R.ok();
    }

    @PostMapping("updateTaskRule")
    @RequiresPermissions("task:account:updateTask")
    @ApiOperation(value = "编辑规则", notes = "编辑规则")
    public R updateTaskRule(@RequestBody TaskConfig taskConfig) {
        taskAccountService.updateTaskRule(taskConfig, getUser().getUsername());
        return R.ok();
    }

    @GetMapping("taskBlackList")
    @RequiresPermissions("task:account:blacklist")
    @ApiOperation(value = "黑名单查询", notes = "黑名单查询")
    public R taskBlackList(@RequestParam("id") Integer id,
                           @RequestParam("pageNo") @NotNull Integer pageNo,
                           @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(taskAccountService.taskBlackList(id, pageNo, pageSize));
    }

    @GetMapping("deletTaskBlackList")
    @RequiresPermissions("task:account:deleteBlacklist")
    @ApiOperation(value = "黑名单删除", notes = "黑名单删除")
    public R deletTaskBlackList(@RequestParam("id") Integer id) {
        taskAccountService.deletTaskBlackList(id);
        return R.ok();
    }

    @GetMapping("addTaskBlackList")
    @RequiresPermissions("task:account:addBlacklist")
    @ApiOperation(value = "黑名单新增", notes = "黑名单新增")
    public R addTaskBlackList(@RequestParam("configId") Integer configId,
                              @RequestParam("loginName") String loginName) {
        taskAccountService.addTaskBlackList(configId, loginName);
        return R.ok();
    }

    @GetMapping("bounsStatistical")
    @RequiresPermissions("task:account:bounsList")
    @ApiOperation(value = "任务中心统计", notes = "任务中心统计")
    public R bounsStatistical(@ModelAttribute TaskBonus taskBonus) {
        return R.ok().put(taskAccountService.bounsStatistical(taskBonus));
    }

    @GetMapping("bounsDetail")
    @RequiresPermissions("task:account:bounsdetail")
    @ApiOperation(value = "任务中心领取记录", notes = "任务中心领取记录")
    public R bounsDetail(@ModelAttribute TaskBonus taskBonus,
                         @RequestParam("pageNo") @NotNull Integer pageNo,
                         @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(taskBonus.getConfigId(), "类型ID不能为空");
        return R.ok().putPage(taskAccountService.bounsDetail(taskBonus, pageNo, pageSize));
    }

    @GetMapping("exportBounsStatistical")
    @RequiresPermissions("task:account:bounsList")
    @ApiOperation(value = "任务中心统计导出", notes = "任务中心统计导出")
    public R exportBounsStatistical(@ModelAttribute TaskBonus taskBonus) {
        SysFileExportRecord record = taskAccountService.exportBounsStatistical(taskBonus,getUser().getUserId());
        if (record == null) {
            throw new R200Exception("正在处理中!");
        }
        return R.ok();
    }
    @GetMapping("/checkFile")
    @ApiOperation(value = "任务中心统计导出文件是否可下载",notes = "任务中心统计导出文件是否可下载\"")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字",  required = true, dataType = "Integer", paramType = "header"),
    })
    public R checkFile(@RequestParam("module") String module){
        return taskAccountService.checkFile(module,getUser().getUserId());
    }
}
