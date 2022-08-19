package com.wsdy.saasops.agapi.modules.service;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.wsdy.saasops.agapi.modules.mapper.AgentMenuMapper;
import com.wsdy.saasops.common.utils.Constant;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.sys.entity.SysMenuEntity;
import com.wsdy.saasops.modules.sys.entity.SysMenuTree;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AgentMenuService {

    @Autowired
    private AgentMenuMapper agentMenuMapper;

    public List<Long> queryRoleList(String agyaccount) {
        return agentMenuMapper.queryRoleList(agyaccount);
    }


    public List<SysMenuTree> selectTreeByRole(String roles, AgentAccount account) {
        List<SysMenuTree> sysMenuEntities = Lists.newArrayList();
        if (account.getAttributes() != 4) {
            sysMenuEntities = agentMenuMapper.selectRoleMenuTree(roles);
        } else {
            sysMenuEntities = agentMenuMapper.selectSubAccountRoleMenuTree(account.getAgyAccount());
        }
        List<SysMenuTree> sysMenuTree = new ArrayList<>();
        sysMenuEntities.forEach(sysMenuEntity -> {
            if (sysMenuEntity.getParentId().equals(0L)) {
                sysMenuTree.add(sysMenuEntity);
            }
        });
        getRoleMenuTree(sysMenuTree, sysMenuEntities);
        return sysMenuTree;
    }


    public void getRoleMenuTree(List<SysMenuTree> sysMenuTree, List<SysMenuTree> sysMenuEntities) {
        for (int i = 0; i < sysMenuTree.size(); i++) {
            for (int j = 0; j < sysMenuEntities.size(); j++) {
                if (sysMenuEntities.get(j).getParentId().equals(sysMenuTree.get(i).getMenuId())) {
                    List<SysMenuTree> children = sysMenuTree.get(i).getChildren();
                    if (!children.contains(sysMenuEntities.get(j))) {
                        children.add(sysMenuEntities.get(j));
                        List<SysMenuTree> sysMenuEntitiesCopy = new ArrayList<>(sysMenuEntities);
                        sysMenuEntitiesCopy.remove(i);
                        getRoleMenuTree(children, sysMenuEntitiesCopy);
                    }
                }
            }
        }
    }


    public List<SysMenuEntity> queryListParentId(Long parentId, List<Long> menuIdList) {
        List<SysMenuEntity> menuList = queryListParentId(parentId);
        if (menuIdList == null) {
            return menuList;
        }

        List<SysMenuEntity> userMenuList = new ArrayList<>();
        for (SysMenuEntity menu : menuList) {
            if (menuIdList.contains(menu.getMenuId())) {
                userMenuList.add(menu);
            }
        }
        return userMenuList;
    }

    public List<SysMenuEntity> queryListParentId(Long parentId) {
        return agentMenuMapper.queryListParentId(parentId);
    }


    public List<SysMenuEntity> getUserMenuList(AgentAccount account) {
        // 系统管理员，拥有最高权限
        /*if (userId.equals(Constant.SUPER_ADMIN) || roleId ==1) {
            return getAllMenuList(null);
        }*/

        // 用户菜单列表
        List<Long> menuIdList = Lists.newArrayList();
        if (account.getAttributes() != 4) {
            menuIdList = agentMenuMapper.queryAllMenuId(account.getAgyAccount());
        } else {
            menuIdList = agentMenuMapper.querySubAccountAllMenuId(account.getAgyAccount(),null);
        }
        return getAllMenuList(menuIdList);
    }

    /**
     * 获取所有菜单列表
     */
    private List<SysMenuEntity> getAllMenuList(List<Long> menuIdList) {
        // 查询根菜单列表
        List<SysMenuEntity> menuList = queryListParentId(0L, menuIdList);
        // 递归获取子菜单
        getMenuTreeList(menuList, menuIdList);

        return menuList;
    }

    /**
     * 递归
     */
    private List<SysMenuEntity> getMenuTreeList(List<SysMenuEntity> menuList, List<Long> menuIdList) {
        List<SysMenuEntity> subMenuList = new ArrayList<SysMenuEntity>();

        for (SysMenuEntity entity : menuList) {
            if (entity.getType() == Constant.MenuType.CATALOG.getValue()) {// 目录
                entity.setList(getMenuTreeList(queryListParentId(entity.getMenuId(), menuIdList), menuIdList));
            }
            subMenuList.add(entity);
        }
        return subMenuList;
    }

    public Set<String> getUserPermissions(String agyaccount) {
        List<String> permsList = Lists.newArrayList();
        PageHelper.clearPage();
        //系统管理员，拥有最高权限
        /*if(userId == Constant.SUPER_ADMIN){
            List<SysMenuEntity> menuList = agentMenuMapper.queryList(new HashMap<>());
            permsList = new ArrayList<>(menuList.size());
            for(SysMenuEntity menu : menuList){
                permsList.add(menu.getPerms());
            }
        }else{*/
        permsList = agentMenuMapper.queryAllPerms(agyaccount);
        //}
        //用户权限列表
        Set<String> permsSet = new HashSet<>();
        for (String perms : permsList) {
            if (StringUtils.isBlank(perms)) {
                continue;
            }
            permsSet.addAll(Arrays.asList(perms.trim().split(",")));
        }
        return permsSet;
    }
}
