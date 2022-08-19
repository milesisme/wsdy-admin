package com.wsdy.saasops.modules.sys.service;

import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.sys.dao.SysMenuDao;
import com.wsdy.saasops.modules.sys.dao.SysRoleMenuDao;
import com.wsdy.saasops.modules.sys.dao.SysRoleMenuExtendMapper;
import com.wsdy.saasops.modules.sys.dto.ColumnAuthTreeDto;
import com.wsdy.saasops.modules.sys.dto.SysRoleMenuEntities;
import com.wsdy.saasops.modules.sys.entity.SysMenuEntity;
import com.wsdy.saasops.modules.sys.entity.SysRoleEntity;
import com.wsdy.saasops.modules.sys.entity.SysRoleMenuEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色与菜单对应关系
 */
@Service("sysRoleMenuService")
public class SysRoleMenuService{

	@Autowired
	private SysRoleMenuDao sysRoleMenuDao;
	@Autowired
	private SysMenuService sysMenuService;
	@Autowired
	private SysMenuDao sysMenuDao;
	@Autowired
	private MbrAccountLogService mbrAccountLogService;
	@Autowired
	private SysRoleMenuExtendMapper sysRoleMenuExtendDto;

	/**
	 *
	 * @param roleId
	 * @param menuIdList
	 */
	@Transactional
	@Deprecated
	public void saveOrUpdate(Long roleId, List<Long> menuIdList) {
		// 先删除角色与菜单关系
		sysRoleMenuDao.delete(roleId);
		if (menuIdList == null || menuIdList.size() == 0) {
			return;
		}

		// 保存角色与菜单关系
		Map<String, Object> map = new HashMap<>(2);
		map.put("roleId", roleId);
		map.put("menuIdList", menuIdList);
		sysRoleMenuDao.save(map);
		sysMenuService.deleteTreeByRole(roleId.toString(),CommonUtil.getSiteCode());
	}

	public List<Long> queryMenuIdList(Long roleId) {
		return sysRoleMenuDao.queryMenuIdList(roleId);
	}

	public void deleteByRoleId(Long roleId, String userName, String ip) {
		SysRoleEntity sysRoleEntity = sysRoleMenuDao.queryRoleInfoById(roleId);
		sysRoleMenuDao.delete(roleId);

		//添加操作日志
		mbrAccountLogService.deleteSysRoleLog(sysRoleEntity, userName, ip);
	}

	public List<SysRoleMenuEntity> queryRoleMenu(Long roleId) {
		return sysRoleMenuDao.queryMenuList(roleId);
	}

	public List<SysRoleMenuEntity> queryRoleMenuAuth(Long roleId) {
		return sysRoleMenuDao.queryMenuAuthList(roleId);
	}

	public List<Integer> queryRoleMenuAuthNew(Long roleId) {
		return sysRoleMenuDao.queryMenuAuthListNew(roleId);
	}
	
	/**
	 * 
	 * 	给多个角色保存菜单权限 
	 * @param sysRoleMenuEntities
	 * @param user
	 * @param siteCode
	 * @param ipAddress
	 * @return
	 */
	@Transactional
	@CacheEvict(value = "SysRoleMenuTree", key = "#siteCode")
	public R saveMenuForManyRole(SysRoleMenuEntities sysRoleMenuEntities, SysUserEntity user, String siteCode,
			String ipAddress) {
		Set<Long> menuIds = sysRoleMenuEntities.getMenuIds();
		List<SysRoleMenuEntity> sysRoleMenuEntitys = sysRoleMenuEntities.getSysRoleMenuEntitys();
		sysRoleMenuDao.saveNotExists(menuIds, sysRoleMenuEntitys);
		return R.ok();
	}
	
	/**
	 * 
	 * 删除多个角色的菜单权限
	 * @param sysRoleMenuEntities
	 * @param user
	 * @param siteCode
	 * @param ipAddress
	 * @return
	 */
	@Transactional
	@CacheEvict(value = "SysRoleMenuTree", key = "#siteCode")
	public Integer deleteMenuForManyRole(SysRoleMenuEntities sysRoleMenuEntities, SysUserEntity user, String siteCode,
			String ipAddress) {
		Set<Long> menuIds = sysRoleMenuEntities.getMenuIds();
		List<SysRoleMenuEntity> sysRoleMenuEntitys = sysRoleMenuEntities.getSysRoleMenuEntitys();
		Set<Long> roleIds = sysRoleMenuEntitys.stream().map(a -> a.getRoleId()).collect(Collectors.toSet());
		return sysRoleMenuDao.deleteByRolesAndMenuIdsBatch(menuIds, roleIds);
	}   

	@Transactional
	public R saveOrUpdate2(List<SysRoleMenuEntity> sysRoleMenuEntities,Long roleId) {
		sysRoleMenuDao.delete(roleId);
		sysRoleMenuExtendDto.deleteSysRoleMenuExtendByRoleId(roleId);
		List<SysRoleMenuEntity> sysRoleMenuEntities1 = new ArrayList<>();
		List<SysRoleMenuEntity> sysRoleMenuEntities2 = new ArrayList<>();

		sysRoleMenuEntities.forEach(e -> {
			e.setRoleId(roleId);
			if(e.getMenuId() >= 10000000){
				sysRoleMenuEntities2.add(e);
			}else{
				sysRoleMenuEntities1.add(e);
			}
		});
		//排除重复  
		List<SysRoleMenuEntity> repeatList = getNoRepeatList(sysRoleMenuEntities1);

		List<SysRoleMenuEntity> repeatList2 = getNoRepeatList(sysRoleMenuEntities2);

		List<SysRoleMenuEntity> addRoleMenuList = new ArrayList<>();
	     for(SysRoleMenuEntity sysRoleMenu : repeatList) {
			 if (sysRoleMenu.getMenuId() == 106) {
			     List<Long> menuIds = repeatList.stream().map(SysRoleMenuEntity :: getMenuId).collect(Collectors.toList());
			 	//根据会员详情下的列权限查询
				 List<SysMenuEntity> list = sysMenuDao.querySearchMenuAuthList();	// 修改只查询出搜索权限
				 if (list.size() > 0) {
					 list.stream().forEach(menuList -> {
					 	if(menuIds.contains(menuList.getParentId())) {
							SysRoleMenuEntity sysRoleMenuEntity = new SysRoleMenuEntity();
							sysRoleMenuEntity.setParentId(menuList.getParentId());
							sysRoleMenuEntity.setMenuId(menuList.getMenuId());
							sysRoleMenuEntity.setRoleId(roleId);
							sysRoleMenuEntity.setType(menuList.getType());
							addRoleMenuList.add(sysRoleMenuEntity);
						}
					 });
				 }
			 }
		 }

	     if(addRoleMenuList.size()>0) {
             repeatList.addAll(getNoRepeatList(addRoleMenuList));
         }

////	syscSaveRelationColumn(repeatList);
		sysRoleMenuDao.saveBatch(repeatList);

		 if(repeatList2!= null && repeatList2.size() > 0){
			 sysRoleMenuExtendDto.saveBatchMenuExtend(repeatList2);
		 }
		//删除角色缓存
		sysMenuService.deleteTreeByRole(roleId.toString(),CommonUtil.getSiteCode());
		return R.ok().put("保存成功");
	}
	
	public void syscSaveRelationColumn(List<SysRoleMenuEntity> repeatList) {
		Set<Long> set = new HashSet<>();
		ColumnAuthTreeDto dto = new ColumnAuthTreeDto();
		for (SysRoleMenuEntity sysRoleMenuEntity : repeatList) {
			set.add(sysRoleMenuEntity.getMenuId());
		}
		dto.setParamList(set);
		//通过menuId查询columnName相等得数据
		List<SysRoleMenuEntity> roleMenuList = sysMenuDao.findMenuByColumn(dto);
		if(Collections3.isEmpty(roleMenuList)) {
			return;
		}
		for (SysRoleMenuEntity sysRoleMenuEntity : roleMenuList) {
			sysRoleMenuEntity.setRoleId(repeatList.get(0).getRoleId());
		}
		sysRoleMenuDao.saveBatch(roleMenuList);
	}
	
	/** 
     * 去除List内复杂字段重复对象 
     * @author : lebron 
     * @param oldList 
     * @return 
     */  
    public List<SysRoleMenuEntity> getNoRepeatList(List<SysRoleMenuEntity> oldList){  
        List<SysRoleMenuEntity> list = new ArrayList<>();  
        if(CollectionUtils.isNotEmpty(oldList)){  
            for (SysRoleMenuEntity roleMenuEntity : oldList) {  
                //list去重复，内部重写equals  
                if(!list.contains(roleMenuEntity)){  
                    list.add(roleMenuEntity);  
                }  
            }  
        }
        return list;          
    }

}
