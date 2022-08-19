package com.wsdy.saasops.modules.sys.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * 菜单管理
 */
@Component
@Mapper
public interface ColumnAuthProviderDao {
	
	/**
	 * 通过token查询userId
	 * @param token
	 * @return
	 */
	Long findUserIdByToken(@Param("token") String token);
	
}
