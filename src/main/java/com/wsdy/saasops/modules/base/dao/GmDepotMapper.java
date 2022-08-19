package com.wsdy.saasops.modules.base.dao;

import com.wsdy.saasops.modules.base.entity.GmDepot;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GmDepotMapper extends MyMapper<GmDepot>{

	int update(GmDepot gmDepot);
	
}