package com.wsdy.saasops.modules.log.dao;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.log.entity.LogMbrLogin;


@Mapper
public interface LogMbrloginMapper extends MyMapper<LogMbrLogin> {

}
