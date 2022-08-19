package com.wsdy.saasops.modules.sys.controller;

import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.entity.SysLogMonitorEntity;
import com.wsdy.saasops.modules.sys.service.SysLogMonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志监控
 */
@RestController
@RequestMapping("/bkapi/sys/logMonitor")
@Api(value = "SysLogMonitorController", tags = "操作日志")
@Slf4j
public class SysLogMonitorController extends AbstractController {

    @Autowired
    private SysLogMonitorService sysLogMonitorService;

    /**
     * 所有日志列表
     */
    @GetMapping("/list")
    @RequiresPermissions("sys:log:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@ModelAttribute SysLogMonitorEntity sysLogMonitorEntity) {
        //查询列表数据
        PageUtils pageUtil = sysLogMonitorService.queryList(sysLogMonitorEntity);
        return R.ok().put("page", pageUtil);
    }

}
