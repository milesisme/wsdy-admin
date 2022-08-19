package com.wsdy.saasops.modules.base.dao;
import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.base.entity.TWinTop;
import com.wsdy.saasops.modules.base.mapper.MyMapper;

import tk.mybatis.mapper.common.IdsMapper;


@Mapper
public interface TWinTopMapper extends MyMapper<TWinTop>,IdsMapper<TWinTop> {

}
