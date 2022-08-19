package com.wsdy.saasops.modules.sys.service;


import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.modules.sys.dao.SysRoleMenuExtendMapper;
import com.wsdy.saasops.modules.sys.dto.SysRoleMenuExtendDto;
import com.wsdy.saasops.modules.sys.entity.SysMenuExtend;
import com.wsdy.saasops.modules.sys.entity.SysRoleEntity;
import com.wsdy.saasops.modules.sys.entity.SysRoleMenuExtend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysRoseMenuExtendService {

    @Autowired
    private SysRoleMenuExtendMapper sysRolePermExtendMapper;


    public List<SysMenuExtend> getSysRosePermExtendByUserId(Long userId){
        return sysRolePermExtendMapper.getByUserId(userId);
    }

    public List<SysRoleMenuExtend> getSysRosePermExtendByRoleId(Long roleId){
        return sysRolePermExtendMapper.getSysRosePermExtendByRoleId(roleId);
    }

    public List<SysRoleEntity> getSysRoleByRoleMenuExten(SysRoleMenuExtendDto sysRolePermExtendDto){
        return  sysRolePermExtendMapper.getSysRoleByRoleMenuExtend(sysRolePermExtendDto.getMenuId(), sysRolePermExtendDto.getType()) ;
    }

    public void save(SysRoleMenuExtendDto sysRolePermExtendDto){
        for(Long roleId : sysRolePermExtendDto.getRoleIds()){
            SysRoleMenuExtend sysRolePermExtend = sysRolePermExtendMapper.getSysRoleMenuExtend(roleId, sysRolePermExtendDto.getMenuId(), sysRolePermExtendDto.getType());
            if(sysRolePermExtend != null){
                throw new R200Exception("该权限已经存在！");
            }
            SysRoleMenuExtend srpe = new SysRoleMenuExtend();
            srpe.setMenuId(sysRolePermExtendDto.getMenuId());

            srpe.setRoleId(roleId);
            sysRolePermExtendMapper.insert(srpe);
        }
    }

    public void delete(SysRoleMenuExtendDto sysRolePermExtendDto){
        sysRolePermExtendMapper.deleteSysRoleMenuExtend(sysRolePermExtendDto.getRoleId(), sysRolePermExtendDto.getMenuId(), sysRolePermExtendDto.getType());
    }
}
