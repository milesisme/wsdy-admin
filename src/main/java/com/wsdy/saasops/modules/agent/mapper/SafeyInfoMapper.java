package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.modules.agent.entity.AgentSubAccount;
import com.wsdy.saasops.modules.agent.entity.AgySubMenu;
import com.wsdy.saasops.modules.sys.entity.SysMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SafeyInfoMapper {


   List<AgentSubAccount> fundSubAccountList(@Param("agentId") Integer agentId);

   List<AgySubMenu> fundAgySubMenu(@Param("subagentid") Integer subagentid);


   List<SysMenuEntity> fundSubAccountMenu();

   int deleteSubMenu(@Param("agyaccount") String agyaccount);

   int findSubAccountCount(@Param("id") Integer id,
                           @Param("agyaccount") String agyaccount);

   int addSubMenu(@Param("agyaccount") String agyaccount,
                  @Param("subagentid") Integer subagentid,
                  @Param("menu_id") Long menu_id);

}
