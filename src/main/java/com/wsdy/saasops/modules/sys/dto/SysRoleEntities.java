package com.wsdy.saasops.modules.sys.dto;

import com.wsdy.saasops.modules.sys.entity.SysRoleEntity;
import com.wsdy.saasops.modules.sys.entity.SysRoleMenuEntity;
import lombok.Data;

import java.util.List;

@Data
public class SysRoleEntities {

    private SysRoleEntity sysRole;

    List<SysRoleMenuEntity> sysRoleMenuEntities;
}
