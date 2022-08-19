package com.wsdy.saasops.modules.sys.service;

import java.util.*;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.modules.agent.entity.AgentDepartment;
import com.wsdy.saasops.modules.agent.mapper.DepartmentMapper;
import com.wsdy.saasops.modules.sys.dao.*;
import com.wsdy.saasops.modules.sys.dto.*;
import com.wsdy.saasops.modules.sys.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wsdy.saasops.common.utils.Constant;
import com.github.mustachejava.util.InternalArrayList;

import static com.wsdy.saasops.common.constants.ColumnAuthConstants.*;

@Service("sysMenuService")
public class SysMenuService {

    List<Integer> times = new InternalArrayList<>();
    @Autowired
    private SysMenuDao sysMenuDao;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysRoleDao sysRoleDao;
    @Autowired
    private SysRoleMenuDao sysRoleMenuDao;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private SysMenuExtendMapper sysMenuExtendMapper;
    @Autowired
    private SysRoleMenuExtendMapper sysRoleMenuExtendMapper;


    public List<Long> queryRoleList(Long userId) {
        return sysRoleDao.queryRoleList(userId);
    }


    public List<SysMenuTree> selectTreeByRole(String roles) {
        List<SysMenuTree> sysMenuEntities = sysMenuDao.selectRoleMenuTree(roles);
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


    @CacheEvict(value = "SysRoleMenuTree", key = "#siteCode+':'+#roles")
    public void deleteTreeByRole(String roles, String siteCode) {

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
        return sysMenuDao.queryListParentId(parentId);
    }

    public List<SysMenuEntity> queryNotButtonList() {
        return sysMenuDao.queryNotButtonList();
    }

    public List<SysMenuEntity> getUserMenuList(Long userId) {
        // 系统管理员，拥有最高权限
        if (userId.equals(Constant.SUPER_ADMIN)) {
            return getAllMenuList(null);
        }

        // 用户菜单列表
        List<Long> menuIdList = sysUserService.queryAllMenuId(userId);
        return getAllMenuList(menuIdList);
    }

    public SysMenuEntity queryObject(Long menuId) {
        return sysMenuDao.queryObject(menuId);
    }

    public List<SysMenuEntity> queryList(Map<String, Object> map) {
        return sysMenuDao.queryList(map);
    }


    public void save(SysMenuEntity menu) {
        sysMenuDao.save(menu);
    }

    public void update(SysMenuEntity menu) {
        sysMenuDao.update(menu);
    }

    @Transactional
    public void deleteBatch(Long[] menuIds) {
        sysMenuDao.deleteBatch(menuIds);
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

    public List<TreeMenuDto2> queryTreeList(Integer roleId) {
        return sysMenuDao.queryMenuList(roleId);
    }

    //    @Cacheable(cacheNames = ApiConstants.REIDS_QUERYTREELISTNEW_KEY, key = "#siteCode+'_' + #roleId")
    public List<TreeMenuDto2> queryTreeListNew(Integer roleId, String siteCode) {
        return addAgentTreeList(sysMenuDao.queryMenuListNew(roleId));
    }

    @Cacheable(value = "initRoleMenuTree", key = "#siteCode+':initRoleMenuTree'")
    public List<TreeMenuDto2> initRoleMenuTree(String siteCode) {
        List<SysMenuEntity> menuList = sysMenuDao.queryAllMenu();
        List<TreeMenuDto2> initRoleMenuTree = new LinkedList<>();
        menuList.stream().forEach(menu -> {
            generateTree(menu, initRoleMenuTree, 0L);
        });
        generateChildrenTree(initRoleMenuTree, menuList);
        return initRoleMenuTree;
    }

    @Cacheable(value = "getMenuAuth", key = "#siteCode+':getMenuAuth:'+#menuId.toString()")
    public TreeMenuDto2 getMenuAuth(String siteCode, Long menuId) {
        List<SysMenuEntity> menuList = sysMenuDao.queryAll();
        List<TreeMenuDto2> initRoleMenuTree = new LinkedList<>();
        TreeMenuDto2 treeMenuDto2 = new TreeMenuDto2();
        treeMenuDto2.setChildren(initRoleMenuTree);
        menuList.stream().forEach(menu -> {
            generateTree(menu, initRoleMenuTree, menuId);
            if (menu.getMenuId().equals(menuId)) {
                treeMenuDto2.setId(menuId);
                treeMenuDto2.setLabel(menu.getName());
            }
        });
        times.clear();
        generateAuthTree(initRoleMenuTree, menuList, 1);
        int T = treeMenuDto2.getChildren().size() != 0 ? 1 : 0;
        for (int t : times) {
            if (t > T) {
                T = t;
            }
        }
        treeMenuDto2.setLevel(T);
        return treeMenuDto2;
    }


    public HashSet<SysRoleMenuEntity> getRoleMenuAuth(Long roleId, Long menuId) {
        List<SysRoleMenuEntity> sysRoleMenuEntities = sysRoleDao.getSavedMenuAuth(roleId, menuId);
        List<SysRoleMenuEntity> sysRoleMenuSaved = new ArrayList<>();
        //遍历已保存的所有菜单
        sysRoleMenuEntities.stream().forEach(sysRoleMenuEntity -> {
            //保存的菜单的父id = 最高节点id
            if (sysRoleMenuEntity.getParentId().equals(menuId)) {
                //获取改父菜单下保存的节点
                sysRoleMenuSaved.add(sysRoleMenuEntity);
            }
        });
        HashSet<SysRoleMenuEntity> auth = new HashSet<>(sysRoleMenuSaved); //去除重复
        if (sysRoleMenuSaved.size() != 0) {

            gengerteRoleMenuSaved(auth, sysRoleMenuSaved, sysRoleMenuEntities);
        }
        //去除type = 1
        HashSet<SysRoleMenuEntity> authCopy = new HashSet<>();
        auth.forEach(e -> {
            if (e.getType() != null && e.getType().equals(2)) {
                authCopy.add(e);
            }
        });
        return authCopy;
    }

    /**
     * @param auth                要展现的菜单
     * @param sysRoleMenuSaved    已保存的菜单（父）
     * @param sysRoleMenuEntities //所有保存的菜单数据
     */
    private void gengerteRoleMenuSaved(HashSet<SysRoleMenuEntity> auth, List<SysRoleMenuEntity> sysRoleMenuSaved, List<SysRoleMenuEntity> sysRoleMenuEntities) {
        for (int x = 0; x < sysRoleMenuEntities.size(); x++) {
            for (int i = 0; i < sysRoleMenuSaved.size(); i++) {
                SysRoleMenuEntity saved = sysRoleMenuSaved.get(i);
                if (saved.getMenuId().equals(sysRoleMenuEntities.get(x).getParentId())) {
                    //获取子节点
                    auth.add(sysRoleMenuEntities.get(x));
                    List<SysRoleMenuEntity> sysRoleMenuEntitiesCopy = new ArrayList<>(sysRoleMenuEntities);
                    sysRoleMenuEntitiesCopy.remove(x);
                    List<SysRoleMenuEntity> sysRoleMenuSavedCopy = new ArrayList<>(sysRoleMenuSaved);
                    sysRoleMenuSavedCopy.remove(sysRoleMenuSaved.get(i));
                    sysRoleMenuSavedCopy.add(sysRoleMenuEntities.get(x));
                    gengerteRoleMenuSaved(auth, sysRoleMenuSavedCopy, sysRoleMenuEntitiesCopy);
                }
            }

        }
    }


    private void generateTree(SysMenuEntity menuEntity, List<TreeMenuDto2> initRoleMenuTree, Long parentId) {
        if (menuEntity.getParentId().equals(parentId)) {
            TreeMenuDto2 groupMenu = new TreeMenuDto2(menuEntity.getMenuId(), menuEntity.getName(), null, null, new LinkedList<TreeMenuDto2>());
            initRoleMenuTree.add(groupMenu);
        }
    }

    private void generateAuthTree(List<TreeMenuDto2> initRoleMenuTree, List<SysMenuEntity> menuList, int i) {
        Integer I = new Integer(i + 1);
        for (int x = 0; x < initRoleMenuTree.size(); x++) {
            TreeMenuDto2 dto = initRoleMenuTree.get(x);
            for (int y = 0; y < menuList.size(); y++) {
                SysMenuEntity menu = menuList.get(y);
                if (menu.getParentId().equals(dto.getId())) {
                    times.add(I);
                    TreeMenuDto2 item = new TreeMenuDto2(menu.getMenuId(), menu.getName(), null, null, new LinkedList<TreeMenuDto2>());
                    List<TreeMenuDto2> sun = dto.getChildren();
                    if (!sun.contains(item)) {
                        sun.add(item);
                    }
                    List<SysMenuEntity> menuListCopy = new ArrayList<>(menuList);
                    menuListCopy.remove(y);
                    generateAuthTree(sun, menuListCopy, I);
                }
            }
        }

    }

    private void generateChildrenTree(List<TreeMenuDto2> initRoleMenuTree, List<SysMenuEntity> menuList) {
        for (int x = 0; x < initRoleMenuTree.size(); x++) {
            TreeMenuDto2 dto = initRoleMenuTree.get(x);
            for (int y = 0; y < menuList.size(); y++) {
                SysMenuEntity menu = menuList.get(y);
                if (menu.getParentId().equals(dto.getId())) {
                    TreeMenuDto2 item = new TreeMenuDto2(menu.getMenuId(), menu.getName(), null, null, new LinkedList<TreeMenuDto2>());
                    List<TreeMenuDto2> sun = dto.getChildren();
                    if (!sun.contains(item)) {
                        sun.add(item);
                    }
                    List<SysMenuEntity> menuListCopy = new ArrayList<>(menuList);
                    menuListCopy.remove(y);
                    if (menu.getParentId() == 1) {
                        generateChildrenTree(sun, menuListCopy);
                    }
                }
            }
        }
    }

    public List<TreeMenuDto> queryMenuList(List<Long> menuIdList) {
        List<SysMenuEntity> pMenuList = new ArrayList<>();
        List<SysMenuEntity> cMenuList = new ArrayList<>();
        if (null != menuIdList && 0 != menuIdList.size()) {
            for (Long menuId : menuIdList) {
                SysMenuEntity sysMenuEntity = sysMenuDao.queryObject(menuId);
                if (null != sysMenuEntity) {
                    if (sysMenuEntity.getParentId() == 0L) {
                        pMenuList.add(sysMenuEntity);
                    } else {
                        cMenuList.add(sysMenuEntity);
                    }
                }
            }
        }
        List<TreeMenuDto> treeMenuList = new ArrayList<>();
        if (pMenuList.size() != 0) {
            for (SysMenuEntity pMenu : pMenuList) {
                TreeMenuDto treeMenuDto = new TreeMenuDto();
                treeMenuDto.setId(pMenu.getMenuId());
                treeMenuDto.setLabel(pMenu.getName());
                List<PermissonDto> pdList = new ArrayList<>();
                if (cMenuList.size() != 0) {
                    for (SysMenuEntity cMenu : cMenuList) {
                        if (cMenu.getParentId().equals(pMenu.getMenuId())) {
                            PermissonDto permissonDto = new PermissonDto();
                            permissonDto.setId(cMenu.getMenuId());
                            permissonDto.setLabel(cMenu.getName());
                            List<PermissionDetailDto> pddList = new ArrayList<>();

                            for (SysMenuEntity dMenu : cMenuList) {
                                if (dMenu.getParentId().equals(cMenu.getMenuId())) {
                                    if (dMenu.getPerms() != null && (dMenu.getPerms().contains("save")
                                            || dMenu.getPerms().contains("add"))) {
                                        pAdd(pddList, dMenu);
                                    }
                                    if (!(dMenu.getPerms() != null && (dMenu.getPerms().contains("save")
                                            || dMenu.getPerms().contains("add")))) {
                                        pAdd(pddList, dMenu);
                                    }
                                    /*
                                      PermissionDetailDto permissionDetailDto = new PermissionDetailDto();
									  permissionDetailDto.setId(dMenu.getMenuId());
									  permissionDetailDto.setLabel(dMenu.getName());
									  pddList.add(permissionDetailDto);*/
                                }
                            }
                            permissonDto.setChildren(pddList);
                            pdList.add(permissonDto);
                        }
                    }
                }
                treeMenuDto.setChildren(pdList);
                treeMenuList.add(treeMenuDto);
            }
        }
        return treeMenuList;
    }

    private void pAdd(List<PermissionDetailDto> pddList, SysMenuEntity dMenu) {
        PermissionDetailDto permissionDetailDto = new PermissionDetailDto();
        permissionDetailDto.setId(dMenu.getMenuId());
        permissionDetailDto.setLabel(dMenu.getName());
        pddList.add(permissionDetailDto);
    }

    public List<TreeMenuDto2> getChildMenuList(Long menuId) {
        List<TreeMenuDto2> resultList = sysMenuDao.getChildMenuList(menuId);
        return resultList;
    }

    public StatMenuDto getStatMenuDto(SysUserEntity sysUserEntity) {
        StatMenuDto statMenuDto = new StatMenuDto();
        if (sysUserEntity.getRoleId() != Constant.SUPER_ADMIN.intValue()) {
            int depositCount = sysRoleMenuDao.findSysRoleMenuByRoleIdAndMenuId(
                    sysUserEntity.getRoleId(), MEMBER_DEPOSIT_MENU_ID);
            if (depositCount == Constants.EVNumber.zero) {
                statMenuDto.setIsDepositCount(Boolean.FALSE);
            }
            int withdrawFirstCount = sysRoleMenuDao.findSysRoleMenuByRoleIdAndMenuId(
                    sysUserEntity.getRoleId(), MEMBER_WITHDRAW_FIRST_MENU_ID);
            if (withdrawFirstCount == Constants.EVNumber.zero) {
                statMenuDto.setIsWithdrawFirstCount(Boolean.FALSE);
            }
            int withdrawReviewCount = sysRoleMenuDao.findSysRoleMenuByRoleIdAndMenuId(
                    sysUserEntity.getRoleId(), MEMBER_WITHDRAW_REVIEW_MENU_ID);
            if (withdrawReviewCount == Constants.EVNumber.zero) {
                statMenuDto.setIsWithdrawReviewCount(Boolean.FALSE);
            }
            int bonusCount = sysRoleMenuDao.findSysRoleMenuByRoleIdAndMenuId(
                    sysUserEntity.getRoleId(), MEMBER_BOUNS_MENU_ID);
            if (bonusCount == Constants.EVNumber.zero) {
                statMenuDto.setIsBonusCount(Boolean.FALSE);
            }
        }
        return statMenuDto;
    }


    private List<TreeMenuDto2> addAgentTreeList(List<TreeMenuDto2> treeMenuDto2List) {
        TreeMenuDto2   arentManageTreeMenuDto = search(treeMenuDto2List, "代理管理");


        // 添加代理
        if(arentManageTreeMenuDto != null){
            TreeMenuDto2  agentreeMenuDto = search(arentManageTreeMenuDto, "代理列表");
            if(agentreeMenuDto != null){
                TreeMenuDto2  agentviewTreeMenuDto= search(agentreeMenuDto, "代理查看");
                if(agentviewTreeMenuDto != null){
                    addChild(agentviewTreeMenuDto, agentviewTreeMenuDto.getId());
                }
            }
        }
        // 添加代理
       
        if(arentManageTreeMenuDto != null){
            TreeMenuDto2   agentReportListTreeMenuDto = search(arentManageTreeMenuDto, "代理报表");
            if(arentManageTreeMenuDto != null){
                TreeMenuDto2  agentreeMenuDto = search(agentReportListTreeMenuDto, "综合报表");
                if(agentreeMenuDto != null){
                    TreeMenuDto2  viewTreeMenuDto= search(agentreeMenuDto, "查看");
                    if(viewTreeMenuDto != null){
                        addChild(viewTreeMenuDto, viewTreeMenuDto.getId());
                    }
                }
            }
        }
       return treeMenuDto2List;
    }


    private void addChild(TreeMenuDto2 treeMenuDto2, Long parentId){
        List<TreeMenuDto2> treeMenuDto2List = sysMenuExtendMapper.getSysMenuExtend(treeMenuDto2.getId(), 1l);
        if(treeMenuDto2List.size() == 0){
            // 初始化
            initSysMenuExtend(parentId);
        }
        treeMenuDto2List = sysMenuExtendMapper.getSysMenuExtend(treeMenuDto2.getId(), 1l);
        treeMenuDto2.setChildren(treeMenuDto2List);
        for(TreeMenuDto2 treeMenuDto21 :treeMenuDto2List){
            List<TreeMenuDto2>  treeMenuDto2List2 = sysMenuExtendMapper.getSysMenuExtend(treeMenuDto21.getId(), 1l);
            if(treeMenuDto2List2.size() > 0){
                // 检查是否有全部部门
                if(!isAllExist(treeMenuDto2List2)){
                    // 初始化全部部门
                    SysMenuExtend sysMenuExtend = new SysMenuExtend();
                    sysMenuExtend.setType(Constants.EVNumber.seven);
                    sysMenuExtend.setIsInner(Constants.EVNumber.one);
                    sysMenuExtend.setName("全部部门");
                    sysMenuExtend.setRefId(Long.valueOf(0));
                    sysMenuExtend.setParentId(treeMenuDto21.getId());
                    sysMenuExtendMapper.insert(sysMenuExtend);

                    SysRoleMenuExtend sysRoleMenuExtend = new SysRoleMenuExtend();
                    sysRoleMenuExtend.setMenuId(sysMenuExtend.getMenuId());
                    sysRoleMenuExtend.setRoleId(1l);
                    sysRoleMenuExtendMapper.insert(sysRoleMenuExtend);

                    treeMenuDto2List2.add(new TreeMenuDto2());
                }
                treeMenuDto2List2 = sysMenuExtendMapper.getSysMenuExtend(treeMenuDto21.getId(), 1l);
                treeMenuDto21.setChildren(treeMenuDto2List2);
            }
        }

    }

    private TreeMenuDto2 search(TreeMenuDto2 parentTreeMenuDto2, String childName) {
        List<TreeMenuDto2> parentList = parentTreeMenuDto2.getChildren();
        if(parentList!= null){
            for (TreeMenuDto2 treeMenuDto2 : parentList) {
                if (childName.equals(treeMenuDto2.getLabel())) {
                    return  treeMenuDto2;
                }
            }
        }
        return null;
    }

    private TreeMenuDto2 search(List<TreeMenuDto2> parentList, String childName) {
        for (TreeMenuDto2 treeMenuDto2 : parentList) {
            if (childName.equals(treeMenuDto2.getLabel())) {
                 return  treeMenuDto2;
            }
        }
        return null;
    }

    private void initSysMenuExtend(Long parentId){
        long[] reportType = {0, 1, 2, 3};
        String[] reportName = {"直线代理", "分线代理", "推广员工", "招商员工"};

        // 添加菜单
        List<SysMenuExtend> sysMenuExtendList = new ArrayList<>();
        for(int i = 0; i < reportType.length; i++){
            SysMenuExtend sysMenuExtend = new SysMenuExtend();
            sysMenuExtend.setType(Constants.EVNumber.six);
            sysMenuExtend.setIsInner(Constants.EVNumber.zero);
            sysMenuExtend.setName(reportName[i]);
            sysMenuExtend.setRefId(reportType[i]);
            sysMenuExtend.setParentId(parentId);
            sysMenuExtendList.add(sysMenuExtend);
        }
        sysMenuExtendMapper.insertList(sysMenuExtendList);

        // 添加权限
        sysMenuExtendList = sysMenuExtendMapper.getSysMenuExtendByParentId(parentId);
        List<SysRoleMenuExtend> sysRoleMenuExtendList = new ArrayList<>();
        for (SysMenuExtend sysMenuExtend :sysMenuExtendList ){
            SysRoleMenuExtend sysRoleMenuExtend = new SysRoleMenuExtend();
            sysRoleMenuExtend.setMenuId(sysMenuExtend.getMenuId());
            sysRoleMenuExtend.setRoleId(1l);
            sysRoleMenuExtendList.add(sysRoleMenuExtend);
        }
        sysRoleMenuExtendMapper.insertList(sysRoleMenuExtendList);


        // 添加部门菜单
        List<AgentDepartment> agentDepartmentList = departmentMapper.departmentList(null);


        for (SysMenuExtend parentSysMenuExtend :sysMenuExtendList){
            List<SysMenuExtend> sysMenuExtendList2 = new ArrayList<>();
            if ( parentSysMenuExtend.getRefId() == 2 || parentSysMenuExtend.getRefId() == 3) {

                if(agentDepartmentList.size() > 0){
                    SysMenuExtend sysMenuExtend2 = new SysMenuExtend();
                    sysMenuExtend2.setType(Constants.EVNumber.seven);
                    sysMenuExtend2.setIsInner(Constants.EVNumber.one);
                    sysMenuExtend2.setName("全部部门");
                    sysMenuExtend2.setRefId(Long.valueOf(0));
                    sysMenuExtend2.setParentId(parentSysMenuExtend.getMenuId());
                    sysMenuExtendList2.add(sysMenuExtend2);
                }

                for (AgentDepartment agentDepartment : agentDepartmentList) {
                    SysMenuExtend sysMenuExtend2 = new SysMenuExtend();
                    sysMenuExtend2.setType(Constants.EVNumber.seven);
                    sysMenuExtend2.setIsInner(Constants.EVNumber.one);
                    sysMenuExtend2.setName(agentDepartment.getDepartmentName());
                    sysMenuExtend2.setRefId(Long.valueOf(agentDepartment.getId()));
                    sysMenuExtend2.setParentId(parentSysMenuExtend.getMenuId());
                    sysMenuExtendList2.add(sysMenuExtend2);
                }
            }

            if(sysMenuExtendList2.size() > 0){
                sysMenuExtendMapper.insertList(sysMenuExtendList2);
                sysMenuExtendList2 = sysMenuExtendMapper.getSysMenuExtendByParentId(parentSysMenuExtend.getMenuId());
                // 添加部门权限
                List<SysRoleMenuExtend> sysRoleMenuExtendList2 = new ArrayList<>();
                for (SysMenuExtend sysMenuExtend :sysMenuExtendList2 ){
                    SysRoleMenuExtend sysRoleMenuExtend = new SysRoleMenuExtend();
                    sysRoleMenuExtend.setMenuId(sysMenuExtend.getMenuId());
                    sysRoleMenuExtend.setRoleId(1l);
                    sysRoleMenuExtendList2.add(sysRoleMenuExtend);
                }
                sysRoleMenuExtendMapper.insertList(sysRoleMenuExtendList2);

            }
        }
    }


    private boolean isAllExist( List<TreeMenuDto2> treeMenuDto2List){
        boolean rt = false;
        for(TreeMenuDto2 treeMenuDto2 :treeMenuDto2List){
            if(treeMenuDto2.getType() == Constants.EVNumber.seven && "全部部门".equals(treeMenuDto2.getLabel())){
                rt = true;
                break;
            }
        }
        return rt;
    }
}