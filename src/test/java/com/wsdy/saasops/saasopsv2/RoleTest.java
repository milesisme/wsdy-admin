package com.wsdy.saasops.saasopsv2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.wsdy.saasops.modules.sys.dto.SysRoleMenuEntities;
import com.wsdy.saasops.modules.sys.dto.SysUserDto;
import com.wsdy.saasops.modules.sys.entity.SysRoleEntity;
import com.wsdy.saasops.modules.sys.entity.SysRoleMenuEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserRoleEntity;
import com.wsdy.saasops.modules.sys.service.ShiroService;
import com.wsdy.saasops.modules.sys.service.SysMenuService;
import com.wsdy.saasops.modules.sys.service.SysRoleMenuService;
import com.wsdy.saasops.modules.sys.service.SysRoleService;
import com.wsdy.saasops.modules.sys.service.SysUserRoleService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RoleTest {

	@Autowired
	private SysRoleService sysRoleService;

	@Autowired
	private SysMenuService sysMenuService;

	@Autowired
	private ShiroService shiroService;

	@Autowired
	private SysRoleMenuService sysRoleMenuService;

	@Autowired
	private SysUserRoleService sysUserRoleService;

	/**
	 * 根据权限id 查询拥有该权限以及子权限的角色id
	 * 
	 * @param menuId
	 * @return
	 */
	@Test
	public void queryRoleIdsByMenuId() {
		List<SysRoleEntity> queryRoleIdsByMenuId = sysRoleService.queryRoleIdsByMenuId(868L, new SysUserEntity());
		System.out.println("queryRoleIdsByMenuId----" + queryRoleIdsByMenuId);

	}

	// 给多个角色保存一个菜单权限 : 即、一个角色对应一个菜单权限
	@Test
	public void saveMenuForManyRole() {
		SysRoleMenuEntities sysRoleMenuEntities = new SysRoleMenuEntities();
		// 多个菜单
		Set<Long> menuIds = new HashSet();
		menuIds.add(1279L);
		menuIds.add(1278L);
		// 多个角色
		List<SysRoleMenuEntity> SysRoleMenuEntitys = new ArrayList<SysRoleMenuEntity>();
		SysRoleMenuEntity entity1 = new SysRoleMenuEntity();
		entity1.setRoleId(148L);
		entity1.setIsTotalChecked(true);
		SysRoleMenuEntity entity2 = new SysRoleMenuEntity();
		entity2.setRoleId(164L);

		SysRoleMenuEntitys.add(entity2);
		SysRoleMenuEntitys.add(entity1);
		sysRoleMenuEntities.setMenuIds(menuIds);
		sysRoleMenuEntities.setSysRoleMenuEntitys(SysRoleMenuEntitys);
		SysUserEntity user = new SysUserEntity();
		sysRoleMenuService.saveMenuForManyRole(sysRoleMenuEntities, user, "", "127.0.0.1");
	}

	// 删除多个角色的菜单权限
	@Test
	public void deleteMenuForManyRole() {
		SysRoleMenuEntities sysRoleMenuEntities = new SysRoleMenuEntities();
		// 多个菜单
		Set<Long> menuIds = new HashSet();
		menuIds.add(1279L);
		menuIds.add(1278L);
		// 多个角色
		List<SysRoleMenuEntity> SysRoleMenuEntitys = new ArrayList<SysRoleMenuEntity>();
		SysRoleMenuEntity entity1 = new SysRoleMenuEntity();
		entity1.setRoleId(148L);
		SysRoleMenuEntity entity2 = new SysRoleMenuEntity();
		entity2.setRoleId(164L);

		SysRoleMenuEntitys.add(entity2);
		SysRoleMenuEntitys.add(entity1);
		sysRoleMenuEntities.setMenuIds(menuIds);
		sysRoleMenuEntities.setSysRoleMenuEntitys(SysRoleMenuEntitys);
		SysUserEntity user = new SysUserEntity();
		sysRoleMenuService.deleteMenuForManyRole(sysRoleMenuEntities, user, "", "127.0.0.1");
	}

	// d修改多个用户的角色
	@Test
	public void setManySysUserRole() {

		SysUserDto sysUserDto = new SysUserDto();
		List<String> usernameList = new ArrayList<String>();
		usernameList.add("daimon11");
		usernameList.add("daimontest");

		SysUserEntity user = new SysUserEntity();
		user.setRoleId(1);
		sysUserDto.setUsernameList(usernameList);
		sysUserDto.setRoleId(1L);
		sysRoleService.setManySysUserRole(sysUserDto, user);
	}

	@Test
	public void deleteByUserId() {
		SysUserRoleEntity sysUserRoleEntity = new SysUserRoleEntity();
		sysUserRoleEntity.setRoleId(1L);
		sysUserRoleEntity.setUserId(57L);
		sysUserRoleService.deleteByUserId(sysUserRoleEntity, new SysUserEntity(), "6.6.6.6");
	}

}
