package com.wsdy.saasops.modules.sys.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.common.utils.Constant;
import com.wsdy.saasops.modules.sys.dao.SysMenuDao;
import com.wsdy.saasops.modules.sys.dao.SysUserDao;
import com.wsdy.saasops.modules.sys.dao.SysUserTokenDao;
import com.wsdy.saasops.modules.sys.entity.SysMenuEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserTokenEntity;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ShiroService{

    @Autowired
    private SysMenuDao sysMenuDao;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysUserTokenDao sysUserTokenDao;

    public Set<String> getUserPermissions(long userId) {
 
    	List<String> permsList = new ArrayList<String>();
        PageHelper.clearPage();
        //系统管理员，拥有最高权限
        if(userId == Constant.SUPER_ADMIN){
            List<SysMenuEntity> menuList = sysMenuDao.queryList(new HashMap<>(2));
            permsList = new ArrayList<>(menuList.size());
            for(SysMenuEntity menu : menuList){
                permsList.add(menu.getPerms());
            }
        }else{
            permsList = sysUserDao.queryAllPerms(userId);
        }
        //log.info(userId + "查询权限集合" + JSON.toJSONString(permsList)+",permsList.size"+permsList.size());
        //用户权限列表
        Set<String> permsSet = new HashSet<>();
        for(String perms : permsList){
            if(StringUtils.isBlank(perms)){
                continue;
            }
            permsSet.addAll(Arrays.asList(perms.trim().split(",")));
        }
        //log.info(userId + "处理权限集合集合" + JSON.toJSONString(permsSet)+",permsSet.size"+permsSet.size());
        return permsSet;
    }

    public SysUserTokenEntity queryByToken(String token) {
        return sysUserTokenDao.queryByToken(token);
    }

    public SysUserEntity queryUser(Long userId) {
        return  sysUserDao.queryObject(userId);
    }
}
