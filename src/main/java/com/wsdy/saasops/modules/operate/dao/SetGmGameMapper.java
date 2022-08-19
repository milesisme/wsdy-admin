package com.wsdy.saasops.modules.operate.dao;

import com.wsdy.saasops.modules.operate.dto.GameDepotNameDto;
import com.wsdy.saasops.modules.operate.entity.SetGmGame;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import tk.mybatis.mapper.common.IdsMapper;

@Mapper
public interface SetGmGameMapper extends MyMapper<SetGmGame>, IdsMapper<SetGmGame> {

	List<SetGmGame> selectByGmDepotIds(@Param("depotIds") Set<Integer> depotIds);

	List<GameDepotNameDto> selectSetDepotname();
	
	/**
	 *  只查询体育的配置名
	 * @return
	 */
	List<GameDepotNameDto> selectSportSetDepotname();

}
