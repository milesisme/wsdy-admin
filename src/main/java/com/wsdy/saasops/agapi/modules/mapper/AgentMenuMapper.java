package com.wsdy.saasops.agapi.modules.mapper;


import com.wsdy.saasops.modules.sys.entity.SysMenuEntity;
import com.wsdy.saasops.modules.sys.entity.SysMenuTree;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentMenuMapper {

    List<Long> queryRoleList(@Param("agyaccount") String agyaccount);

    List<SysMenuTree> selectRoleMenuTree(@Param("roleIds") String roles);

    List<SysMenuTree> selectSubAccountRoleMenuTree(@Param("agyaccount") String agyaccount);


    List<SysMenuEntity> queryListParentId(Long parentId);

    /**
     * 查询用户的所有菜单ID
     */
    List<Long> queryAllMenuId(String agyaccount);

    /**
     * 查询用户的所有菜单ID
     */
    List<Long> querySubAccountAllMenuId(@Param("agyaccount") String agyaccount,
                                        @Param("menuId") Long menuId);


    List<SysMenuEntity> queryList(Object id);

    /**
     * 查询用户的所有权限
     */
    List<String> queryAllPerms(String agyaccount);

    int saveUserRole(@Param("userId") Integer userId,
                     @Param("roleId") Integer roleId,
                     @Param("agyaccount") String agyaccount);
}
