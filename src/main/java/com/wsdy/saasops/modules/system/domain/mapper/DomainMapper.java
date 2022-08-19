package com.wsdy.saasops.modules.system.domain.mapper;

import com.wsdy.saasops.modules.system.domain.entity.SystemDomain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by William on 2017/11/1.
 */
@Mapper
@Component
public interface DomainMapper {
    List<SystemDomain> queryByConditions(SystemDomain domain);
    void delByIds(@Param("ids") String ids);
    int multiInsert(List<SystemDomain> domains);
}
