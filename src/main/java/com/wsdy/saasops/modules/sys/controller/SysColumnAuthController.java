package com.wsdy.saasops.modules.sys.controller;


import java.util.Objects;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.service.SysColumnAuthService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

/**
 * 列权限接口类
 */
@RestController
@RequestMapping("/bkapi/sys/columnAuth")
public class SysColumnAuthController extends AbstractController {

    @Autowired
    private SysColumnAuthService sysColumnAuthService;

    /**
     * 查询所有列权限信息
     *
     * @param menuId
     * @return
     */
    @GetMapping("/getAllColumnAuth")
    @RequiresPermissions("sys:menu:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getColumnAuthOptimization(@RequestParam("menuId") Long menuId, @RequestParam("type") Long type) {
        if (Objects.isNull(menuId) || Objects.isNull(type)) {
            return R.error(500, "参数不能为空");
        }
        return R.ok().put("data", sysColumnAuthService.getColumnAuth(menuId, type, getUser().getRoleId()));
    }

}
