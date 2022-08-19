package com.wsdy.saasops.modules.sys.dao;

import com.wsdy.saasops.modules.sys.entity.SysRoleEntity;
import com.wsdy.saasops.modules.sys.entity.SysRoleMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 角色与菜单对应关系
 */
@Component
@Mapper
public interface SysRoleMenuDao extends BaseDao<SysRoleMenuEntity> {

	@Override
    void saveBatch(List<SysRoleMenuEntity> sysRoleMenuEntities);


	/**
	 * 根据角色ID，获取菜单ID列表
	 */
	List<Long> queryMenuIdList(Long roleId);

	SysRoleEntity queryRoleInfoById(@Param("roleId") Long roleId);

    List<SysRoleMenuEntity> queryMenuList(@Param("roleId") Long roleId);

    List<SysRoleMenuEntity> queryMenuAuthList(Long roleId);

	List<Integer> queryMenuAuthListNew(Long roleId);

    Integer findSysRoleMenuByRoleIdAndMenuId(
            @Param("roleId") Integer roleId,
            @Param("menuId") Long menuId);
    
	Integer deleteByRolesAndMenuIdsBatch(@Param("menuIds") Set<Long> menuIds, 
			@Param("roleIds") Set<Long> roleIds);
	
	Integer saveOne(SysRoleMenuEntity sysRoleMenuEntity);
	
	Integer saveNotExists(@Param("menuIds") Set<Long> menuIds, @Param("sysRoleMenuEntitys") List<SysRoleMenuEntity> sysRoleMenuEntitys);
	
	int queryRoleIdMenuIdsCount(@Param("roleId") Long roleId, @Param("allMenuIds") Set<Long> allMenuIds);
}
