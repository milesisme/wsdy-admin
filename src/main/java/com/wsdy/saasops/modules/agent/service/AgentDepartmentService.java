package com.wsdy.saasops.modules.agent.service;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgentDepartmentMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentDepartment;
import com.wsdy.saasops.modules.agent.mapper.DepartmentMapper;
import com.wsdy.saasops.modules.sys.dao.SysMenuExtendMapper;
import com.wsdy.saasops.modules.sys.dao.SysRoleMenuExtendMapper;
import com.wsdy.saasops.modules.sys.entity.SysMenuExtend;
import com.wsdy.saasops.modules.sys.entity.SysRoleMenuExtend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;

@Slf4j
@Service
@Transactional
public class AgentDepartmentService {

    @Autowired
    private AgentDepartmentMapper agentDepartmentMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private SysMenuExtendMapper sysMenuExtendMapper;
    @Autowired
    private SysRoleMenuExtendMapper sysRoleMenuExtendMapper;

    public List<AgentAccount> agentShareholderList() {
        return departmentMapper.agentShareholderList();
    }

    public List<AgentDepartment> departmentList() {
        return departmentMapper.departmentList(null);
    }

    public void addDepartment(List<AgentDepartment> departments, String username) {
        for (AgentDepartment department : departments) {
            if (isNull(department.getId())) {
                AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(department.getAgentId());
                department.setAgyAccount(agentAccount.getAgyAccount());
                department.setCreateUser(username);
                department.setModifyUser(username);
                department.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                department.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
                agentDepartmentMapper.insert(department);
                addDepartmentMenu(department);
            } else {
                department.setModifyUser(username);
                updateDepartment(department);
                updateDepartmentMenu(department);
            }
        }
    }

    public void updateDepartment(AgentDepartment department) {
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(department.getAgentId());
        department.setAgyAccount(agentAccount.getAgyAccount());
        department.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agentDepartmentMapper.updateByPrimaryKeySelective(department);
    }

    public void deleteDepartment(AgentDepartment department) {
        List<AgentDepartment> departments = departmentMapper.departmentList(department.getId());
        if (departments.size() > 0) {
            if (departments.get(0).getAgentCount() > 0) {
                throw new R200Exception("请先解除代理跟部门关系");
            }
        }
        agentDepartmentMapper.deleteByPrimaryKey(department.getId());
        deleteDepartmentMenu(department);
    }


    private void addDepartmentMenu(AgentDepartment agentDepartment){
        List<Long>  parentIds = sysMenuExtendMapper.getMenuIdByType(Constants.EVNumber.six);
        if(parentIds!= null && parentIds.size() > 0){
            for(Long parentId: parentIds){
                SysMenuExtend sysMenuExtend = new SysMenuExtend();
                sysMenuExtend.setType(Constants.EVNumber.seven);
                sysMenuExtend.setIsInner(Constants.EVNumber.one);
                sysMenuExtend.setName(agentDepartment.getDepartmentName());
                sysMenuExtend.setRefId(Long.valueOf(agentDepartment.getId()));
                sysMenuExtend.setParentId(parentId);
                sysMenuExtendMapper.insert(sysMenuExtend);

                SysRoleMenuExtend sysRoleMenuExtend = new SysRoleMenuExtend();
                sysRoleMenuExtend.setMenuId(sysMenuExtend.getMenuId());
                sysRoleMenuExtend.setRoleId(1l);
                sysRoleMenuExtendMapper.insert(sysRoleMenuExtend);

                // 检查是否选中全部
                List<Long> roles = sysRoleMenuExtendMapper.getSysALLRoleMenuExtend();
                if(roles!= null && roles.size() > 0){
                   for (Long roleId: roles){
                       SysRoleMenuExtend sysRoleMenuExtend2 = new SysRoleMenuExtend();
                       sysRoleMenuExtend2.setMenuId(sysMenuExtend.getMenuId());
                       sysRoleMenuExtend2.setRoleId(roleId);
                       sysRoleMenuExtendMapper.insert(sysRoleMenuExtend2);
                   }
                }
            }
        }
    }

    private void updateDepartmentMenu(AgentDepartment agentDepartment){
        sysMenuExtendMapper.updateMenuName(agentDepartment.getDepartmentName(), agentDepartment.getId());
    }

    private  void deleteDepartmentMenu(AgentDepartment agentDepartment){
        List<SysMenuExtend> sysMenuExtendList =  sysMenuExtendMapper.getSysMenuExtendByRefId(agentDepartment.getId());
        sysMenuExtendMapper.deleteMenu(agentDepartment.getId());
        for(SysMenuExtend sysMenuExtend :sysMenuExtendList){
            sysRoleMenuExtendMapper.deleteSysRoleMenuExtendByMenuId(sysMenuExtend.getMenuId());
        }
    }
}

