package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    List<AgentAccount> agentShareholderList();

    List<AgentDepartment> departmentList(@Param("id") Integer id);
}
