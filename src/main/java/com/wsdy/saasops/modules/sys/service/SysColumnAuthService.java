package com.wsdy.saasops.modules.sys.service;

import com.wsdy.saasops.modules.sys.dao.SysColumnAuthDao;
import com.wsdy.saasops.modules.sys.dto.ColumnAuthTreeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("sysColumnAuthService")
public class SysColumnAuthService {

    @Autowired
    private SysColumnAuthDao sysColumnAuthDao;

    /**
     * 获取所有列权限列表
     */
    public List<ColumnAuthTreeDto> getColumnAuth(Long menuId, Long type, Integer roleId) {
        // 1.查出这个角色的所有菜单；
        List<ColumnAuthTreeDto> AllColumnAuthList = sysColumnAuthDao.getColumnAuthOptimization(menuId, type, roleId);
        // 2. 获得一级权限
        List<ColumnAuthTreeDto> rootColumnAuthList = new ArrayList<>();
        AllColumnAuthList.stream().forEach(ls -> {
            if (ls.getParentId().equals(menuId) && ls.getType().equals(type)) {
                rootColumnAuthList.add(ls);
            }
        });

        // 3.获得二级权限
        rootColumnAuthList.stream().forEach(ls -> {
            List<ColumnAuthTreeDto> columnAuthList = new ArrayList<>();
            for (ColumnAuthTreeDto col : AllColumnAuthList) {
                if (col.getParentId().equals(ls.getMenuId())) {
                    columnAuthList.add(col);
                }
            }
            ls.setChildList(columnAuthList);

            // 4.获得三级权限
            columnAuthList.stream().forEach(lsInner -> {
                List<ColumnAuthTreeDto> childColumnAuthList = new ArrayList<>();
                for (ColumnAuthTreeDto col : AllColumnAuthList) {
                    if (col.getParentId().equals(lsInner.getMenuId())) {
                        childColumnAuthList.add(col);
                    }
                }
                lsInner.setChildList(childColumnAuthList);

                // 5. 获得四级权限
                childColumnAuthList.stream().forEach(lsInners -> {
                    List<ColumnAuthTreeDto> grandsonColumnAuthList = new ArrayList<>();
                    for (ColumnAuthTreeDto col : AllColumnAuthList) {
                        if (col.getParentId().equals(lsInners.getMenuId())) {
                            grandsonColumnAuthList.add(col);
                        }
                    }
                    lsInners.setChildList(grandsonColumnAuthList);
                });

            });
        });
        return rootColumnAuthList;
    }

    /**
     * 获取所有列权限列表
     */
    public List<ColumnAuthTreeDto> getRoleColumnAuth(Long menuId, Long type, Integer roleId) {
        List<ColumnAuthTreeDto> columnAuthList = sysColumnAuthDao.getRoleColumnAuth(menuId, type, roleId);
        return columnAuthList;
    }
    public List<ColumnAuthTreeDto> getRoleColumnAuthByFlag(Long menuId, Integer roleId) {
        List<ColumnAuthTreeDto> columnAuthList = sysColumnAuthDao.getRoleColumnAuthByFlag(menuId, roleId);
        return columnAuthList;
    }

    /**
     * 获取所有列权限列表
     */
    public List<ColumnAuthTreeDto> getRoleAuth(ColumnAuthTreeDto columnAuthTreeDto) {
        List<ColumnAuthTreeDto> columnAuthList = sysColumnAuthDao.getRoleAuth(columnAuthTreeDto);
        return columnAuthList;
    }
}

