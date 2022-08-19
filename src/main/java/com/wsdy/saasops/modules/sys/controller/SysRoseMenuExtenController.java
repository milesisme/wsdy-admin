package com.wsdy.saasops.modules.sys.controller;

import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.dto.SysRoleMenuExtendDto;
import com.wsdy.saasops.modules.sys.service.SysRoseMenuExtendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 权限扩展
 */
@RestController
@RequestMapping("/bkapi/sys/roseMenuExten")
@Api(value = "SysRoseMenuExtenController", tags = "权限扩展")
@Slf4j
public class SysRoseMenuExtenController extends AbstractController {
    @Autowired
    private SysRoseMenuExtendService sysRosePermExtenService;


    /**
     * 获取权限
     */
    @GetMapping("/getSysRoleByRoleMenuExten")
    @RequiresPermissions(value = {"agent:account:info", "agent:report:comviewlist"})
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getSysRoleByRoleMenuExten(@ModelAttribute SysRoleMenuExtendDto sysRoleMenuExtenDto) {
        return R.ok().put(sysRosePermExtenService.getSysRoleByRoleMenuExten(sysRoleMenuExtenDto));
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @RequiresPermissions(value = {"agent:account:info", "agent:report:comviewlist"})
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody SysRoleMenuExtendDto sysRolePermExtenDto, HttpServletRequest request) {
        sysRosePermExtenService.save(sysRolePermExtenDto);
        return R.ok();
    }


    /**
     * 删除
     */
    @PostMapping("/delete")
    @RequiresPermissions(value = {"agent:account:info", "agent:report:comviewlist"})
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody List<SysRoleMenuExtendDto> sysRoleMenuExtenDtoList, HttpServletRequest request) {
        for(SysRoleMenuExtendDto sysRoleMenuExtenDto : sysRoleMenuExtenDtoList){
            if(sysRoleMenuExtenDto.getMenuId() == null){
                throw new R200Exception("菜单ID不能为空");
            }

            if(sysRoleMenuExtenDto.getRoleId() == null){
                throw new R200Exception("角色ID不能为空");
            }


            if(sysRoleMenuExtenDto.getType() == null){
                throw new R200Exception("菜单类型不能为空不能为空");
            }
        }

        for(SysRoleMenuExtendDto sysRoleMenuExtenDto : sysRoleMenuExtenDtoList){
            sysRosePermExtenService.delete(sysRoleMenuExtenDto);
        }
        return R.ok();
    }

}
