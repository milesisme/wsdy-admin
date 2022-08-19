package com.wsdy.saasops.modules.log.dao;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.log.entity.LogSystem;


@Mapper
public interface LogSystemMapper extends MyMapper<LogSystem> {

}
