package com.wsdy.saasops.modules.sys.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.sys.dto.TreeMenuDto2;
import com.wsdy.saasops.modules.sys.entity.SysMenuExtend;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysMenuExtendMapper  extends MyMapper<SysMenuExtend> {

    List<SysMenuExtend> getSysMenuExtendByParentId(Long parentId);

    List<TreeMenuDto2> getSysMenuExtend(Long parentId, Long roleId);

    List<Long> getParentIdByType(Integer type);

    List<Long> getMenuIdByType(Integer type);


    int updateMenuName(String name, Integer refId);

    int deleteMenu(Integer refId);

    List<SysMenuExtend> getSysMenuExtendByRefId(Integer refId);

}
