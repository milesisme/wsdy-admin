package com.wsdy.saasops.modules.log.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.log.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface OperationLogMapper extends MyMapper<OperationLog> {
}
