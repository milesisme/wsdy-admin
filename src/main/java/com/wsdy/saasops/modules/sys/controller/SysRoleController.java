package com.wsdy.saasops.modules.sys.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.Constant;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.ValidatorUtils;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.dto.SysUserDto;
import com.wsdy.saasops.modules.sys.dto.TreeMenuDto;
import com.wsdy.saasops.modules.sys.entity.SysRoleEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserRoleEntity;
import com.wsdy.saasops.modules.sys.service.SysMenuService;
import com.wsdy.saasops.modules.sys.service.SysRoleMenuService;
import com.wsdy.saasops.modules.sys.service.SysRoleService;
import com.wsdy.saasops.modules.sys.service.SysUserRoleService;
import com.wsdy.saasops.modules.sys.service.SysUserService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 角色管理
 */
@RestController
@RequestMapping("/bkapi/sys/role")
public class SysRoleController extends AbstractController {
	
    @Autowired
    private SysRoleService sysRoleService;
    
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    
    @Autowired
    private SysMenuService sysMenuService;
    
    @Autowired
    private SysUserRoleService sysUserRoleService;
    
    @Autowired
    private SysUserService sysUserService;
    
    /**
     * 角色列表
     */
    @GetMapping("/list")
    @RequiresPermissions("sys:role:list")
    public R list(@ModelAttribute SysRoleEntity roleEntity) {
        //如果不是超级管理员，则只查询自己创建的角色列表
        if (getUser().getRoleId() != 1) {
            roleEntity.setCreateUser(getUser().getRoleName());
        }
        //查询列表数据
        PageUtils p = sysRoleService.queryListPage(roleEntity);
        return R.ok().put("page", p);
    }

    /**
     * 角色列表
     */
    @GetMapping("/listAll")
    @RequiresPermissions("sys:role:list")
    public R listAll() {
        //查询列表数据
        SysRoleEntity roleEntity = new SysRoleEntity();
        if (getUser().getRoleId() != 1) {
            roleEntity.setCreateUser(getUser().getUsername());
        }
        List<SysRoleEntity> list = sysRoleService.queryList(roleEntity);
        return R.ok().put("list", list);
    }

    @GetMapping("/queryConditions")
    @RequiresPermissions("sys:role:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryConditions(@ModelAttribute SysRoleEntity roleEntity, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        //如果不是超级管理员，则只查询自己创建的角色列表
        if (getUser().getRoleId() != 1) {
            roleEntity.setCreateUser(getUser().getUsername());
        }
        //查询列表数据
        //Query query = new Query(params);
        PageUtils p = sysRoleService.queryConditions(roleEntity);
        return R.ok().put("page", p);
    }

    /**
     * 角色列表
     */
    @GetMapping("/select")
    @RequiresPermissions("sys:role:select")
    public R select() {
        SysRoleEntity roleEntity = new SysRoleEntity();
        //如果不是超级管理员，则只查询自己所拥有的角色列表
        SysUserEntity user = getUser();
        if (user.getRoleId() != 1) {
            roleEntity.setCreateUser(user.getUsername());
        }
        roleEntity.setIsEnable(1);
        List<SysRoleEntity> list = sysRoleService.queryList(roleEntity);
        return R.ok().put("list", list);
    }

    /**
     * 角色信息
     */
    @GetMapping("/info/{roleId}")
    @RequiresPermissions("sys:role:info")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("roleId") Long roleId) {
        SysRoleEntity role = sysRoleService.queryObject(roleId);
        //查询角色对应的菜单
        List<Long> menuIdList = sysRoleMenuService.queryMenuIdList(roleId);
        role.setMenuIdList(menuIdList);
        List<TreeMenuDto> treeMenuList = sysMenuService.queryMenuList(menuIdList);
        role.setTreeMenuList(treeMenuList);
        return R.ok().put("role", role);
    }


    /**
     * 保存角色
     */
//	@SysLog("保存角色")
    @PostMapping("/saveRole")
    @RequiresPermissions("sys:role:save")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @Deprecated
    public R saveRole(@RequestBody SysRoleEntity role) {
        ValidatorUtils.validateEntity(role);
        if (sysRoleService.queryCountByRoleName(role.getRoleName()) > 0) {
            return R.error(2000, "该角色已存在");
        }
        role.setCreateUserId(getUserId());
        role.setCreateUser(getUser().getUsername());
        sysRoleService.save(role);
        return R.ok();
    }


    /**
     * 修改多个用户的角色
     */
    @PostMapping("/setManySysUserRole")	
    @RequiresPermissions(value = {
//    		"sys:role:update", 
    		"sys:user:update"})
    public R setManySysUserRole(@RequestBody SysUserDto sysUserDto) {
    	if(Collections3.isEmpty(sysUserDto.getUsernameList())) {
    		return R.error(2000, "用户列表不能为空");
    	}
    	if(sysUserDto.getRoleId() == null) {
    		return R.error(2000, "角色id不能为空");
    	}
        return sysRoleService.setManySysUserRole(sysUserDto, getUser());
    }

    /**
     * 	修改多个用户的角色 用户校验
     * @param username
     * @return
     */
    @GetMapping("/checkSysUser")
    @RequiresPermissions("sys:user:info")
    @ApiImplicitParams(@ApiImplicitParam(name = "username", value = "username", required = true, dataType = "String"))
	public R checkSysUser(String username) {
		if (StringUtils.isBlank(username)) {
			return R.error(2000, "用户名不能为空！");
		}
		SysUserEntity queryByUserName = sysUserService.queryByUserName(username);
		if (null == queryByUserName) {
			return R.error(2000, "用户不存在");
		}
		if (queryByUserName.getUserId().equals(getUserId())) {
			return R.error(2000, "自己不能修改自己的数据权限");
		}
		return R.ok();
	}
    

    /**
     * 	根据权限id 查询拥有该权限以及子权限的角色id
     * @param menuId
     * @return
     */
    @GetMapping("/queryRoleIdsByMenuId")
    @RequiresPermissions("sys:role:list")
    @ApiImplicitParams(@ApiImplicitParam(name = "menuId", value = "menuId", required = true, dataType = "Long"))
	public R queryRoleIdsByMenuId(Long menuId) {
		if (menuId == null) {
			return R.error(2000, "menuId不能为空！");
		}
		return R.ok(sysRoleService.queryRoleIdsByMenuId(menuId, getUser()));
	}
    
    /**
     * 修改角色
     */
//	@SysLog("修改角色")
    @PostMapping("/update")
    @RequiresPermissions("sys:role:update")
    @Deprecated
    public R update(@RequestBody SysRoleEntity role) {
        if (role.getRoleId().equals(Constant.SUPER_ADMIN)) {
            return R.error(2000, "超级管理员不可修改,请联系客服");
        }
        role.setCreateUserId(getUserId());
        sysRoleService.update(role);
        sysRoleMenuService.saveOrUpdate(role.getRoleId(), role.getMenuIdList());
        return R.ok();
    }

    /**
     * 修改角色
     */
//    @SysLog("修改角色是否启用")
    @PostMapping("/updateEnable")
    @RequiresPermissions("sys:role:available")
    public R updateEnable(@RequestBody SysRoleEntity role, HttpServletRequest request) {
        sysRoleService.updateRoleEnable(role, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    /**
     * 删除指定的用户角色  sys_user_role表
     */
    @PostMapping("/deleteByUserId")
    @RequiresPermissions("sys:role:delete")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
    , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R deleteByUserId(@RequestBody SysUserRoleEntity sysUserRoleEntity, HttpServletRequest request) {
    	SysUserEntity user = getUser();
    	sysUserRoleService.deleteByUserId(sysUserRoleEntity, user, CommonUtil.getIpAddress(request));
    	return R.ok();
    }
    
    /**
     * 删除角色
     */
    @PostMapping("/delete")
    @RequiresPermissions("sys:role:delete")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R delete(@RequestBody SysRoleEntity role, HttpServletRequest request) {
        //判断角色下是否有用户
        for (Long roleId : role.getRoleIds()) {
            if (sysUserRoleService.countRoleUsers(roleId) != 0) {
                return R.error(2000, "该角色下还有用户,不可删除");
            }
            sysRoleMenuService.deleteByRoleId(roleId, getUser().getUsername(), CommonUtil.getIpAddress(request));
        }
        sysRoleService.deleteBatch(role.getRoleIds());
        return R.ok();
    }

    /**
     * 导出报表
     *
     * @param response
     */
    @GetMapping("/ExportExcel")
    @RequiresPermissions("system:systemdomain:save")
    @ApiOperation(value = "导出报表", notes = "导出报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void ExportExcel(@ModelAttribute SysRoleEntity roleEntity, HttpServletResponse response) {
        //如果不是超级管理员，则只查询自己创建的角色列表
        if (!getUserId().equals(Constant.SUPER_ADMIN)) {
            roleEntity.setCreateUserId(getUserId());
        }
        //查询列表数据
        //Query query = new Query(params);
        sysRoleService.exportExcel(roleEntity, response);
    }
}
