package com.wsdy.saasops.modules.sys.service;

import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.sys.dao.SysUserRoleDao;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserRoleEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 用户与角色对应关系
 */
@Service("sysUserRoleService")
public class SysUserRoleService{

	@Autowired
	private SysUserRoleDao sysUserRoleDao;
	@Autowired
	private MbrAccountLogService mbrAccountLogService;
	@Autowired
	private SysUserService sysUserService;

	@Transactional
	public void saveOrUpdate(Long userId, List<Long> roleIdList) {
		if(roleIdList.size() == 0){
			return ;
		}

		//先删除用户与角色关系
		sysUserRoleDao.delete(userId);
		
		//保存用户与角色关系
		Map<String, Object> map = new HashMap<>(4);
		map.put("userId", userId);
		map.put("roleIdList", roleIdList);
		sysUserRoleDao.save(map);
	}

	public List<Long> queryRoleIdList(Long userId) {
		return sysUserRoleDao.queryRoleIdList(userId);
	}

	@Transactional
	public void delete(Long userId, String userName, String ip) {
		sysUserRoleDao.delete(userId);

		//添加操作日志
		SysUserEntity userEntity = sysUserService.queryUserEntityOne(userId);
		mbrAccountLogService.deleteSysUserLog(userEntity, userName, ip);
	}

	public int countRoleUsers(Long roleId) {
		return sysUserRoleDao.queryTotal(roleId);
	}

	@Transactional
	public int deleteByUserId(SysUserRoleEntity sysUserRoleEntity, SysUserEntity user, String ip) {
		int deleteByUserId = sysUserRoleDao.deleteByUserId(sysUserRoleEntity);
		if (deleteByUserId > 0) {
			//d 添加操作日志。
			SysUserEntity queryUserEntityOne = sysUserService.queryUserEntityOne(sysUserRoleEntity.getUserId());
			mbrAccountLogService.updateSysUserInfoLog(queryUserEntityOne, user.getUsername(), ip);
		}
		return deleteByUserId;
	}

}
