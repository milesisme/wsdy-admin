package com.wsdy.saasops.modules.sys.controller;

import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.dto.SysWarningDealWithDto;
import com.wsdy.saasops.modules.sys.dto.SysWarningDto;
import com.wsdy.saasops.modules.sys.dto.SysWarningQueryDto;
import com.wsdy.saasops.modules.sys.entity.SysMenuEntity;
import com.wsdy.saasops.modules.sys.service.SysWarningService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
    @RequestMapping("/bkapi/sys/warning")
@Api(value = "SysWarningController", tags = "系统预警")
@Slf4j
public class SysWarningController  extends AbstractController {


    @Autowired
    private SysWarningService sysWarningService;

    /**
     * 分页查询预警信息
     */
    @GetMapping("/list")
    @RequiresPermissions("sys:warning:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@ModelAttribute SysWarningQueryDto sysWarningQueryDto) {
        Assert.isNull(sysWarningQueryDto.getPageNo(), "PageNo不能为空");
        Assert.isNull(sysWarningQueryDto.getPageSize(), "pageSize不能为空");
        PageUtils pageUtils = sysWarningService.pageList(sysWarningQueryDto);
        return R.ok().put(pageUtils);
    }



    /**
     * 处理预警信息
     */
    @PostMapping("/dealWith")
     @RequiresPermissions("sys:warning:dealWith")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R dealWith(@RequestBody SysWarningDealWithDto sysWarningQueryDto) {
        Assert.isNull(sysWarningQueryDto.getId(), "ID不能为空");
        sysWarningService.dealWith(sysWarningQueryDto, getUser().getUsername());
        return R.ok();
    }
}
