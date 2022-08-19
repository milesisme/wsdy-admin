package com.wsdy.saasops.modules.log.controller;

import javax.validation.constraints.NotNull;

import com.wsdy.saasops.modules.sys.entity.SysOperatioLog;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.log.entity.LogSystem;
import com.wsdy.saasops.modules.log.service.LogSystemService;

@RestController
@RequestMapping("/bkapi/log/logsystem")
public class LogSystemController {
    @Autowired
    private LogSystemService logSystemService;

    /**
     * 列表
     */
    @GetMapping("/list")
    @RequiresPermissions("log:logsystem:list")
    public R list(@RequestBody LogSystem logSystem, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", logSystemService.queryListPage(logSystem,pageNo,pageSize));
    }


    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    @RequiresPermissions("log:logsystem:info")
    public R info(@PathVariable("id") Integer id) {
        LogSystem logSystem =logSystemService.queryObject(id);

        return R.ok().put("logSystem", logSystem);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @RequiresPermissions("log:logsystem:save")
    public R save(@RequestBody LogSystem logSystem) {
            logSystemService.save(logSystem);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @RequiresPermissions("log:logsystem:update")
    public R update(@RequestBody LogSystem logSystem) {
            logSystemService.update(logSystem);

        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @RequiresPermissions("log:logsystem:delete")
    public R delete(@RequestBody String ids) {
    		if (!StringUtils.isEmpty(ids)) {
                String[] idArr = ids.split(",");
			logSystemService.deleteBatch(idArr);
			return R.ok();
		} else {
                return R.error();
            }
    }

    @GetMapping("/queryLog")
    @ApiOperation(value = "日志监控查询日志", notes = "日志监控查询日志")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryLog(@ModelAttribute SysOperatioLog operatioLog, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(logSystemService.queryLog(pageNo, pageSize, operatioLog));
    }

}
