package com.wsdy.saasops.modules.sys.service;

import com.wsdy.saasops.modules.sys.dto.ColumnAuthTreeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ColumnAuthProviderServiceImpl")
public class ColumnAuthProviderService {

    @Autowired
    private SysColumnAuthService sysColumnAuthServiceImpl;

    public List<ColumnAuthTreeDto> getRoleColumnAuth(Integer roleId, Long menuId, Long type) {
        //判断用户是否有权限
        List<ColumnAuthTreeDto> resultList = sysColumnAuthServiceImpl.getRoleColumnAuth(menuId, type, roleId);
        return resultList;
    }

    public List<ColumnAuthTreeDto> getRoleColumnAuthByFlag(Integer roleId, Long menuId) {
        //判断用户是否有权限
        List<ColumnAuthTreeDto> resultList = sysColumnAuthServiceImpl.getRoleColumnAuthByFlag(menuId, roleId);
        return resultList;
    }


    public List<ColumnAuthTreeDto> getAllColumnAuth(Long menuId, Long type, Integer roleId) {
        List<ColumnAuthTreeDto> resultList = sysColumnAuthServiceImpl.getColumnAuth(menuId, type, roleId);
        return resultList;
    }

    public List<ColumnAuthTreeDto> getRoleAuth(ColumnAuthTreeDto columnAuthTreeDto) {
        List<ColumnAuthTreeDto> resultList = sysColumnAuthServiceImpl.getRoleAuth(columnAuthTreeDto);
        return resultList;
    }
}
