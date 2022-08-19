package com.wsdy.saasops.modules.log.mapper;

import com.wsdy.saasops.modules.log.entity.LogMbrLogin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LogMapper {
	LogMbrLogin findMemberLoginLastOne(String loginName);
	
	int deleteMbrLoginBatch(@Param("idArr") String[] idArr);
	
	int deleteMbrRegBatch(@Param("idArr") String[] idArr);
	
	int deleteSystemBatch(@Param("idArr") String[] idArr);
	
	int updateLoginTime(@Param("id") Integer id);

	List<LogMbrLogin> queryLoginAreaIsNull();

	Integer loginDays(@Param("accountId") Integer accountId,@Param("startDay") String startDay,@Param("endDay") String endDay);

	LogMbrLogin getFirstLogin(@Param("accountId") Integer accountId);

}
