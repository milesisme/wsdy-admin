package com.wsdy.saasops.modules.sys.dao;

import com.wsdy.saasops.modules.sys.entity.SysUserTokenEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * 系统用户Token
 */
@Component
@Mapper
public interface SysUserTokenDao extends BaseDao<SysUserTokenEntity> {
    
    SysUserTokenEntity queryByUserId(Long userId);

    SysUserTokenEntity queryByToken(String token);

    @Override
    int update(SysUserTokenEntity token);
}
