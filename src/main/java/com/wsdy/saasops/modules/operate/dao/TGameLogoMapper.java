package com.wsdy.saasops.modules.operate.dao;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.operate.entity.TGameLogo;

import tk.mybatis.mapper.common.IdsMapper;

@Mapper
public interface TGameLogoMapper extends MyMapper<TGameLogo>, IdsMapper<TGameLogo> {

	List<TGameLogo> selectByIdList(@Param("gameLogoIds") Set<Integer> gameLogoIds);

}
