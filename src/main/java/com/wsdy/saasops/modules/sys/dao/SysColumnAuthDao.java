package com.wsdy.saasops.modules.sys.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.wsdy.saasops.modules.sys.dto.ColumnAuthTreeDto;

/**
 * 菜单管理
 */
@Component
@Mapper
public interface SysColumnAuthDao {
	
	/**
	 * 根据父菜单，查询子菜单
	 * @param parentId 父菜单ID
	 */
	List<ColumnAuthTreeDto> getColumnAuth(@Param("parentId") Long parentId, @Param("type") Long type, @Param("roleId") Integer roleId);
	
	
	/**
	 * 根据角色查询
	 * @param menuId 父菜单ID
	 */
	List<ColumnAuthTreeDto> getRoleColumnAuth(@Param("menuId") Long menuId, @Param("type") Long type, @Param("roleId") Integer roleId);

	/**
	 * 根据角色查询
	 * @param menuId 父菜单ID
	 */
	List<ColumnAuthTreeDto> getRoleColumnAuthByFlag(@Param("menuId") Long menuId, @Param("roleId") Integer roleId);


	/**
	 * 获取角色所有模块权限
	 * @param columnAuthTreeDto 父菜单ID
	 */
	List<ColumnAuthTreeDto> getRoleAuth(ColumnAuthTreeDto columnAuthTreeDto);

	/**
	 * 根据父菜单，查询子菜单
	 * @param parentId 父菜单ID
	 */
	List<ColumnAuthTreeDto> getColumnAuthOptimization(@Param("parentId") Long parentId, @Param("type") Long type, @Param("roleId") Integer roleId);
	
}
