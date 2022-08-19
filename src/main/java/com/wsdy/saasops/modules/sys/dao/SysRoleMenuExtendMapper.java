package com.wsdy.saasops.modules.sys.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.sys.entity.SysMenuExtend;
import com.wsdy.saasops.modules.sys.entity.SysRoleEntity;
import com.wsdy.saasops.modules.sys.entity.SysRoleMenuEntity;
import com.wsdy.saasops.modules.sys.entity.SysRoleMenuExtend;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface SysRoleMenuExtendMapper extends MyMapper<SysRoleMenuExtend> {
    List<SysMenuExtend> getByUserId(Long userId);

    SysRoleMenuExtend getSysRoleMenuExtend(Long roleId, Long menuId, Integer type);

    List<SysRoleMenuExtend> getSysRosePermExtendByRoleId(Long roleId);

    List<SysRoleEntity> getSysRoleByRoleMenuExtend(Long menuId, Integer type);

    int deleteSysRoleMenuExtend(Long roleId, Long menuId, Integer type);

    int deleteSysRoleMenuExtendByRoleId(Long roleId);

    int deleteSysRoleMenuExtendByMenuId(Long menuId);

    void saveBatchMenuExtend(List<SysRoleMenuEntity> sysRoleMenuEntities);

    List<Long> getSysALLRoleMenuExtend();
}
