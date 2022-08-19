package com.wsdy.saasops.modules.sys.controller;


import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.Constant;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.dto.SysRoleEntities;
import com.wsdy.saasops.modules.sys.dto.SysRoleMenuEntities;
import com.wsdy.saasops.modules.sys.dto.TreeMenuDto2;
import com.wsdy.saasops.modules.sys.entity.*;
import com.wsdy.saasops.modules.sys.service.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 系统菜单
 */
@RestController
@RequestMapping("/bkapi/sys/menu")
public class SysMenuController extends AbstractController {
	
    @Autowired
    private SysMenuService sysMenuService;
    
    @Autowired
    private ShiroService shiroService;
    
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysRoseMenuExtendService sysRoseMenuExtenService;

    /**
     * 导航菜单
     */
    @GetMapping("/saasopsTree")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R saasopsTree() {
        String roles = sysMenuService.queryRoleList(getUserId()).toString().replace("[", "").replace("]", "");
        List<SysMenuTree> menuList = sysMenuService.selectTreeByRole(roles);
        return R.ok().put("menuList", menuList);
    }

    /**
     * 导航菜单
     */
    @GetMapping("/nav")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R nav() {
        List<SysMenuEntity> menuList = sysMenuService.getUserMenuList(getUserId());
        Set<String> permissions = shiroService.getUserPermissions(getUserId());
        return R.ok().put("menuList", menuList).put("permissions", permissions);
    }

    /**
     * 所有菜单列表
     */
    @GetMapping("/list")
    @RequiresPermissions("sys:menu:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list() {
        List<SysMenuEntity> menuList = sysMenuService.queryList(new HashMap<String, Object>(2));
        return R.ok().put(menuList);
    }

    /**
     * 获取对应菜单子菜单
     */
    @GetMapping("/getChildMenuList")
    @RequiresPermissions("sys:menu:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getChildMenuList(@RequestParam Long menuId) {
        List<TreeMenuDto2> menuList = sysMenuService.getChildMenuList(menuId);
        return R.ok().put(menuList);
    }

    /**
     * 所有菜单列表
     */
    @GetMapping("/treeList")
    @RequiresPermissions("sys:menu:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R treeList() {
        List<TreeMenuDto2> menuList = sysMenuService.queryTreeList(getUser().getRoleId());
        return R.ok().put(menuList);
    }


    @GetMapping("/initRoleMenuTree")
    @RequiresPermissions("sys:menu:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R initRoleMenuTree() {
        List<TreeMenuDto2> tree = sysMenuService.initRoleMenuTree(CommonUtil.getSiteCode());
        return R.ok().put("tree", tree);
    }

    /**
     * 获取已保存菜单，左边
     */
    @GetMapping("/getRoleMenu")
    @RequiresPermissions("sys:menu:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getRoleMenu(@RequestParam Long roleId) {
        R r = R.ok().put("roleMenus", sysRoleMenuService.queryRoleMenu(roleId));
        r.put("roleMenuAuth", sysRoleMenuService.queryRoleMenuAuth(roleId));
        return r;
    }

    /**
     * 获取角色所有权限
     */
    @GetMapping("/getRoleMenuNew")
    @RequiresPermissions("sys:menu:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getRoleMenuNew(@RequestParam Long roleId) {
        R r = R.ok().put("roleMenuAuth", sysRoleMenuService.queryRoleMenuAuthNew(roleId));
        return r;
    }

    /**
     * 初始化角色菜单
     */
    @GetMapping("/updateRoleMenuTree")
    @RequiresPermissions("sys:menu:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateRoleMenuTree(@RequestParam Long roleId) {
        List<TreeMenuDto2> tree = sysMenuService.initRoleMenuTree(CommonUtil.getSiteCode());
        List<Long> checkedIds = sysRoleMenuService.queryMenuIdList(roleId);
        SysRoleEntity sysRoleEntity = sysRoleService.queryObject(roleId);
        R r = R.ok().put("tree", tree);
        r.put("checkedIds", checkedIds);
        r.put("role", sysRoleEntity);
        return r;
    }

    /**
     * 初始化右边的菜单
     */
    @GetMapping("/getMenuAuth")
    @RequiresPermissions("sys:menu:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getMenuAuth(@RequestParam Long menuId) {
        return R.ok().put("Permissons", sysMenuService.getMenuAuth(CommonUtil.getSiteCode(), menuId));
    }

    /**
     * 获取已保存的权限根据父节点Id
     */
    @GetMapping("/getMenuAuthSaved")
    @RequiresPermissions("sys:menu:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getMenuAuth(@RequestParam Long menuId, @RequestParam Long roleId) {
        R r = R.ok().put("Permissons", sysMenuService.getMenuAuth(CommonUtil.getSiteCode(), menuId));
        r.put("roleSaved", sysMenuService.getRoleMenuAuth(roleId, menuId));
        return r;
    }

    /**
     * 保存角色菜单 : 给一个角色保存多个菜单权限
     */
    @PostMapping("/saveRoleMenu")
    @RequiresPermissions(value = {"sys:role:save", "sys:role:modify"}, logical = Logical.OR)
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R saveRoleMenu(@RequestBody SysRoleEntities sysRoleMenuEntities, HttpServletRequest request) {
        SysRoleEntity role = sysRoleMenuEntities.getSysRole();
        //先删除角色原菜单关系
        if (sysRoleMenuEntities.getSysRoleMenuEntities() == null || sysRoleMenuEntities.getSysRoleMenuEntities().size() == 0) {
            return R.ok().put("code", 2000).put("data", "未传输任何菜单");
        }
        if (role.getRoleId() == null) {
            if (sysRoleService.queryCountByRoleName(role.getRoleName()) > 0) {
                return R.ok().put("code", 2000).put("msg", "该角色名已存在");
            }
        }
        role.setCreateUserId(getUserId());
        role.setCreateUser(getUser().getUsername());
        SysUserEntity user = getUser();
        return sysRoleService.save(sysRoleMenuEntities, user, CommonUtil.getSiteCode(), CommonUtil.getIpAddress(request));
    }

    /**
     * 	给多个角色保存菜单权限
     */
    @PostMapping("/saveMenuForManyRole")
    @RequiresPermissions(value = {"sys:role:save", "sys:role:modify"}, logical = Logical.OR)
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, paramType = "header")})
    public R saveMenuForManyRole(@RequestBody SysRoleMenuEntities sysRoleMenuEntities, HttpServletRequest request) {
        if (Collections3.isEmpty(sysRoleMenuEntities.getSysRoleMenuEntitys())) {
    		return R.error(2000, "未传输任何菜单");
        }
        return sysRoleMenuService.saveMenuForManyRole(sysRoleMenuEntities, getUser(), CommonUtil.getSiteCode(), CommonUtil.getIpAddress(request));
    }


    /**
     * 	给多个角色删除菜单权限
     */
    @PostMapping("/deleteMenuForManyRole")
    @RequiresPermissions(value = {"sys:role:modify"})
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, paramType = "header")})
    public R deleteMenuForManyRole(@RequestBody SysRoleMenuEntities sysRoleMenuEntities, HttpServletRequest request) {
        if (Collections3.isEmpty(sysRoleMenuEntities.getSysRoleMenuEntitys())) {
    		return R.error(2000, "未传输任何菜单");
        }
        SysUserEntity user = getUser();
        sysRoleMenuService.deleteMenuForManyRole(sysRoleMenuEntities, user, CommonUtil.getSiteCode(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    /**
     * 选择菜单(添加、修改菜单)
     */
    @GetMapping("/select")
    @RequiresPermissions("sys:menu:select")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R select() {
        //查询列表数据
        List<SysMenuEntity> menuList = sysMenuService.queryNotButtonList();
        //添加顶级菜单
        SysMenuEntity root = new SysMenuEntity();
        root.setMenuId(0L);
        root.setName("一级菜单");
        root.setParentId(-1L);
        root.setOpen(true);
        menuList.add(root);
        return R.ok().put("menuList", menuList);
    }

    /**
     * 菜单信息
     */
    @GetMapping("/info/{menuId}")
    @RequiresPermissions("sys:menu:info")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("menuId") Long menuId) {
        SysMenuEntity menu = sysMenuService.queryObject(menuId);
        return R.ok().put("menu", menu);
    }


    /**
     * 保存
     */
//	@SysLog("保存菜单")
    @PostMapping("/save")
    @RequiresPermissions("sys:menu:save")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody SysMenuEntity menu) {
        //数据校验
        verifyForm(menu);
        sysMenuService.save(menu);
        return R.ok();
    }

    /**
     * 修改
     */
//	@SysLog("修改菜单")
    @PostMapping("/update")
    @RequiresPermissions("sys:menu:update")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody SysMenuEntity menu) {
        //数据校验
        verifyForm(menu);

        sysMenuService.update(menu);

        return R.ok();
    }

    /**
     * 删除
     */
//	@SysLog("删除菜单")
    @PostMapping("/delete")
    @RequiresPermissions("sys:menu:delete")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(long menuId) {
        //判断是否有子菜单或按钮
        List<SysMenuEntity> menuList = sysMenuService.queryListParentId(menuId);
        if (menuList.size() > 0) {
            return R.error("请先删除子菜单或按钮");
        }

        sysMenuService.deleteBatch(new Long[]{menuId});

        return R.ok();
    }

    /**
     * 验证参数是否正确
     */
    private void verifyForm(SysMenuEntity menu) {
        if (StringUtils.isBlank(menu.getName())) {
            throw new RRException("菜单名称不能为空");
        }

        if (menu.getParentId() == null) {
            throw new RRException("上级菜单不能为空");
        }

        //菜单
        if (menu.getType() == Constant.MenuType.MENU.getValue()) {
            if (StringUtils.isBlank(menu.getUrl())) {
                throw new RRException("菜单URL不能为空");
            }
        }

        //上级菜单类型
        int parentType = Constant.MenuType.CATALOG.getValue();
        if (menu.getParentId() != 0) {
            SysMenuEntity parentMenu = sysMenuService.queryObject(menu.getParentId());
            parentType = parentMenu.getType();
        }

        //目录、菜单
        if (menu.getType() == Constant.MenuType.CATALOG.getValue() ||
                menu.getType() == Constant.MenuType.MENU.getValue()) {
            if (parentType != Constant.MenuType.CATALOG.getValue()) {
                throw new RRException("上级菜单只能为目录类型");
            }
            return;
        }

        //按钮
        if (menu.getType() == Constant.MenuType.BUTTON.getValue()) {
            if (parentType != Constant.MenuType.MENU.getValue()) {
                throw new RRException("上级菜单只能为菜单类型");
            }
            return;
        }
    }

    @GetMapping("/fundShow")
    @ApiOperation(value = "会员资金统计权限", notes = "会员资金统计权限")
    public R fundShow() {
        return R.ok().put(sysMenuService.getStatMenuDto(getUser()));
    }

    /**
     *  获得登录角色的完整权限树
     */
    @GetMapping("/treeListNew")
    @RequiresPermissions("sys:menu:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R treeListNew() {
        List<TreeMenuDto2> menuList = sysMenuService.queryTreeListNew(getUser().getRoleId(),CommonUtil.getSiteCode());
        return R.ok().put(menuList);
    }
}
