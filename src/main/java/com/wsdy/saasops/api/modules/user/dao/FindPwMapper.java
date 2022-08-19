package com.wsdy.saasops.api.modules.user.dao;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.api.modules.user.entity.FindPwEntity;
import com.wsdy.saasops.modules.base.mapper.MyMapper;


@Mapper
public interface FindPwMapper extends MyMapper<FindPwEntity>{

}
