package com.wsdy.saasops.modules.sys.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.Constant;
import com.wsdy.saasops.common.utils.ExcelUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.sys.dao.SysMenuDao;
import com.wsdy.saasops.modules.sys.dao.SysRoleDao;
import com.wsdy.saasops.modules.sys.dao.SysRoleMenuDao;
import com.wsdy.saasops.modules.sys.dto.SysRoleEntities;
import com.wsdy.saasops.modules.sys.dto.SysUserDto;
import com.wsdy.saasops.modules.sys.entity.SysMenuEntity;
import com.wsdy.saasops.modules.sys.entity.SysRoleEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;

import lombok.extern.slf4j.Slf4j;


/**
 * 角色
 */
@Slf4j
@Service("sysRoleService")
public class SysRoleService{

	@Autowired
	private SysRoleDao sysRoleDao;
	
	@Autowired
	private SysRoleMenuService sysRoleMenuService;
	
	@Autowired
	private MbrAccountLogService mbrAccountLogService;
	
	@Value("${sys.authority.excel.path}")
	private String sysAuthorityExcelPath;
	
	@Resource(name = "stringRedisTemplate_0")
	private RedisTemplate redisTemplate;
	
    @Autowired
    private SysUserRoleService sysUserRoleService;
	
    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private SysMenuDao sysMenuDao;

	@Autowired
	private SysRoleMenuDao sysRoleMenuDao;
    
	public SysRoleEntity queryObject(Long roleId) {
		return sysRoleDao.queryObject(roleId);
	}

	public PageUtils queryListPage(SysRoleEntity roleEntity) {
        PageHelper.startPage(roleEntity.getPageNo(), roleEntity.getPageSize());
        if (!StringUtils.isEmpty(roleEntity.getOrder())) {
            PageHelper.orderBy(roleEntity.getOrder());
        }
        List<SysRoleEntity> list = sysRoleDao.queryList(roleEntity);
        PageUtils p = BeanUtil.toPagedResult(list);
		return p;
	}
	
	public List<SysRoleEntity> queryList(SysRoleEntity roleEntity) {
        List<SysRoleEntity> list = sysRoleDao.queryList(roleEntity);
		return list;
	}

	@Transactional
	@Deprecated
	public void save(SysRoleEntity role) {
		role.setCreateTime(new Date());
		sysRoleDao.save(role);
		
		//检查权限是否越权
		checkPrems(role);
		
		//保存角色与菜单关系
		sysRoleMenuService.saveOrUpdate(role.getRoleId(), role.getMenuIdList());
	}

	@Transactional
	@CacheEvict(value = "SysRoleMenuTree", key = "#siteCode")
	public R save(SysRoleEntities roles,SysUserEntity user,String siteCode, String ip) {
		SysRoleEntity role = roles.getSysRole();
		//d检查权限是否越权
		List<Long> roleMenuIds = new ArrayList<>();
		roles.getSysRoleMenuEntities().forEach(roleMenu -> {
			roleMenuIds.add(roleMenu.getMenuId());
		});
		role.setMenuIdList(roleMenuIds);
		checkPrems(role);
		if(role.getRoleId() ==null ) {
			role.setCreateTime(new Date());
			sysRoleDao.save(role);
			Map<String,Object> map = new HashMap<>(2);
			map.put("roleName",role.getRoleName());
			roles.setSysRole(sysRoleDao.queryList(map).get(0));

			//新增操作日志
			mbrAccountLogService.addSysRoleLog(role, user.getUsername(), ip);
		}else {
			SysRoleEntity roleEntity = sysRoleDao.findSysRoleById(role.getRoleId());
			role.setCreateUser(roleEntity.getCreateUser());
			sysRoleDao.update(role);
			//修改操作日志
			mbrAccountLogService.updateSysRoleLog(role, user.getUsername(), ip);
		}
		//保存角色与菜单关系
		return sysRoleMenuService.saveOrUpdate2(roles.getSysRoleMenuEntities(),role.getRoleId());
	}

	@Transactional
	public void update(SysRoleEntity role) {
		sysRoleDao.update(role);
		//检查权限是否越权
		checkPrems(role);
		//更新角色与菜单关系
		sysRoleMenuService.saveOrUpdate(role.getRoleId(), role.getMenuIdList());
	}

	public void updateRoleEnable(SysRoleEntity role, String userName, String ip) {
		sysRoleDao.update(role);

		//添加操作日志。
		mbrAccountLogService.updateSysRoleStatusLog(role, userName, ip);
	}

	@Transactional
	public void deleteBatch(Long[] roleIds) {
		sysRoleDao.deleteBatch(roleIds);
		Arrays.stream(roleIds).forEach(roleId ->{
			redisTemplate.delete("SaasopsV2:SysRoleMenuTree" + CommonUtil.getSiteCode() + ":" + roleId);
		});
	}
	
	public List<Long> queryRoleIdList(String createUser) {
		return sysRoleDao.queryRoleIdList(createUser);
	}

	public PageUtils queryConditions(SysRoleEntity roleEntity) {
		PageHelper.startPage(roleEntity.getPageNo(), roleEntity.getPageSize());
        if (!StringUtils.isEmpty(roleEntity.getOrder())) {
            PageHelper.orderBy(roleEntity.getOrder());
        }
        List<SysRoleEntity> list = sysRoleDao.queryConditions(roleEntity);
        PageUtils p = BeanUtil.toPagedResult(list);
		return p;
	}

	public void exportExcel(SysRoleEntity roleEntity, HttpServletResponse response) {
		String fileName = "角色权限" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		List<Map<String, Object>> list = Lists.newLinkedList();
		sysRoleDao.queryConditions(roleEntity).stream().forEach(
				cs->{
					Map<String, Object> param = new HashMap<>(8);
					param.put("role_name", cs.getRoleName());
					param.put("role_nickName", cs.getRoleNickName());
					param.put("remark", cs.getRemark());
					param.put("userNum", cs.getUserNum());
					param.put("isEnable", "1".equals(cs.getIsEnable())?"启用 ":"禁用");
					param.put("createUser", cs.getCreateUser());
					param.put("create_time", cs.getCreateTime());
					list.add(param);
				}
		);
		Workbook workbook = ExcelUtil.commonExcelExportList("mapList", sysAuthorityExcelPath, list);
		try {
			ExcelUtil.writeExcel(response, workbook, fileName);
		} catch (IOException e) {
			log.error("error:" + e);
		}
	}

	/**
	 * 检查权限是否越权
	 */
	private void checkPrems(SysRoleEntity role){
		//如果不是超级管理员，则需要判断角色的权限是否超过自己的权限
		if(role.getCreateUserId().equals(Constant.SUPER_ADMIN)){
			return ;
		}
		
		//查询用户所拥有的菜单列表
		//List<Long> menuIdList = sysUserService.queryAllMenuId(role.getCreateUserId());
		
		//判断是否越权
		/*if(!menuIdList.containsAll(role.getMenuIdList())){
			throw new RRException("新增角色的权限，已超出你的权限范围");
		}*/
	}


	public int queryCountByRoleName(String roleName){
		return sysRoleDao.queryByRolename(roleName).size();

	}

	@Transactional
	public R setManySysUserRole(SysUserDto sysUserDto, SysUserEntity sysUserLogin) {
		// 1.判断是否越权
		if (sysUserLogin.getRoleId() != 1) {
			// d 查询用户创建的角色列表
			List<Long> roleIdList = this.queryRoleIdList(sysUserLogin.getUsername());
			if (!roleIdList.contains(sysUserDto.getRoleId())) {
				throw new RRException("所选角色，不是本人创建");
			}
		}

		// d: 多个用户
		List<String> updateUsernameList = sysUserDto.getUsernameList();
		// 2. 输入的用户信息 进行必要的业务校验
		List<SysUserEntity> queryByUserNameList = sysUserService.queryByUserNameList(updateUsernameList);
		for (SysUserEntity sysUserEntity : queryByUserNameList) {
			if (sysUserEntity.getUserId().equals(sysUserLogin.getUserId())) {
				return R.error(2000, "用户：" + sysUserEntity.getUsername() + "，自己不能修改自己的数据权限");
			}
			if (sysUserEntity.getRoleId() != null && sysUserEntity.getRoleId() == 1 && sysUserEntity.getUserId() == 1L) {
				return R.error(2000, "用户：" + sysUserEntity.getUsername() + "，是超级管理员不可修改,请联系客服");
			}
		}
		List<Long> roleIdList = Arrays.asList(sysUserDto.getRoleId());
		// 3 执行保存 原有更新用户角色的逻辑代码（后台调用，暂不做性能优化处理）
		queryByUserNameList.forEach(t -> {
			sysUserRoleService.saveOrUpdate(t.getUserId(), roleIdList);
		});
		return R.ok();
	}

	/**
	 * 
	 *	 查询拥有当前菜单以及当前菜单所有下级菜单权限的角色
	 * @param menuId
	 * @return
	 */
	public List<SysRoleEntity> queryRoleIdsByMenuId(Long menuId, SysUserEntity sysUserEntity) {

		// mysql不支持递归查询
		List<SysMenuEntity> sysMenuEntityAll = sysMenuDao.queryAll();

		// d 当前菜单的所有上下级id
		Set<Long> allMenuIds = new HashSet<>();
		allMenuIds.add(menuId);
		// 1. 循环获取当前menuId的上级
		// d:当前的菜单对象
		SysMenuEntity thisSysMenuEntity = sysMenuEntityAll.stream().filter(t -> t.getMenuId().equals(menuId)).findFirst().get();
		
		// 1.1 获取当前菜单的parentid , 查询menu_id = parent_id
		List<SysMenuEntity> superSysMenuEntitys = sysMenuEntityAll.stream().filter(t -> t.getMenuId().equals(thisSysMenuEntity.getParentId())).collect(Collectors.toList());
		SysMenuEntity superSysMenuEntity = Collections3.isEmpty(superSysMenuEntitys) ? null : superSysMenuEntitys.get(0);

		// superSysMenuEntity != null 即：当前是菜单还有子菜单
		while (superSysMenuEntity != null) {
			allMenuIds.add(superSysMenuEntity.getMenuId());
			Long parentId = superSysMenuEntity.getParentId();
			List<SysMenuEntity> collect = sysMenuEntityAll.stream().filter(t -> t.getMenuId().equals(parentId)).collect(Collectors.toList());
			superSysMenuEntity = Collections3.isEmpty(collect) ? null : collect.get(0);
		}

		// 2.循环获取当前menuId的下级
		// 2.2 获取parentid = 当前menuId ,下级有多个
		List<SysMenuEntity> lowerSysMenuEntitys = sysMenuEntityAll.stream().filter(t -> t.getParentId().equals(menuId))
				.collect(Collectors.toList());

		// lowerSysMenuEntitys != null 即：当前非最低级菜单
		while (Collections3.isNotEmpty(lowerSysMenuEntitys)) {
			List<SysMenuEntity> lowerSysMenuEntitysTarget = new ArrayList(lowerSysMenuEntitys);
			lowerSysMenuEntitys.clear();
			for (SysMenuEntity lowerSysMenuEntity : lowerSysMenuEntitysTarget) {
				allMenuIds.add(lowerSysMenuEntity.getMenuId());
				lowerSysMenuEntitys.addAll(
						sysMenuEntityAll.stream().filter(t -> t.getParentId().equals(lowerSysMenuEntity.getMenuId()))
								.collect(Collectors.toList()));
			}
		}

		// 3.根据获取到的所有menuIds查询sys_role_menu表中拥有所有menuIds的角色
		// 如果不是超级管理员，则只查询自己所拥有的角色列表
		SysRoleEntity sysRoleEntityDto = new SysRoleEntity();
        if (sysUserEntity.getRoleId() != 1) {
        	sysRoleEntityDto.setCreateUser(sysUserEntity.getUsername());
        }
        sysRoleEntityDto.setIsEnable(1);
        List<SysRoleEntity> list = this.queryList(sysRoleEntityDto);
		List<SysRoleEntity> result = new LinkedList<SysRoleEntity>();
		for (SysRoleEntity sysRoleEntity : list) {
			int count = sysRoleMenuDao.queryRoleIdMenuIdsCount(sysRoleEntity.getRoleId(), allMenuIds);
			if (count == allMenuIds.size()) {
				result.add(sysRoleEntity);
			}
		}
		return result;
	}

}
