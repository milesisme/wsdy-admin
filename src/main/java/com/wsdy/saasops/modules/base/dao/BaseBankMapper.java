package com.wsdy.saasops.modules.base.dao;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
@Mapper
public interface BaseBankMapper extends MyMapper<BaseBank>{
	
}