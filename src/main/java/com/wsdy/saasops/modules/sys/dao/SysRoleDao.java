package com.wsdy.saasops.modules.sys.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.wsdy.saasops.modules.sys.entity.SysRoleEntity;
import com.wsdy.saasops.modules.sys.entity.SysRoleMenuEntity;

/**
 * 角色管理
 */
@Component
@Mapper
public interface SysRoleDao extends BaseDao<SysRoleEntity> {


    /**
     * 查询用户创建的角色ID列表
     */
    List<Long> queryRoleIdList(@Param("createUser") String createUser);

    List<Long> queryRoleList(@Param("userId") Long userId);

    /**
     * 查询角色表，并计算该角色的人数
     *
     * @param map
     * @return
     */
    List<SysRoleEntity> queryConditions(SysRoleEntity roleEntity);

    List<SysRoleEntity> queryByRolename(@Param("roleName") String roleName);

    List<SysRoleMenuEntity> getSavedMenuAuth(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    SysRoleEntity findSysRoleById(@Param("roleId") Long roleId);

	List<Integer> queryRoleIdsByMenuId(@Param("menuId") Integer menuId);
}
