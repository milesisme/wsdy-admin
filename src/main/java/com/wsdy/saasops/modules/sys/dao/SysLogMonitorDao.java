package com.wsdy.saasops.modules.sys.dao;

import com.wsdy.saasops.modules.sys.entity.SysLogMonitorEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 操作日志
 */
@Component
@Mapper
public interface SysLogMonitorDao extends BaseDao<SysLogMonitorEntity> {

	/**
	 * 分页查询操作日志
	 * @param entity
	 */
	List<SysLogMonitorEntity> queryList(SysLogMonitorEntity entity);

}
