package com.wsdy.saasops.modules.operate.dao;

import com.wsdy.saasops.modules.operate.dto.SetGameCategoryDto;
import com.wsdy.saasops.modules.operate.entity.TGmCat;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import com.wsdy.saasops.modules.base.mapper.MyMapper;


@Mapper
public interface TGmCatMapper extends MyMapper<TGmCat> {

	/**
	 * 	三方游戏下的真人分类,根据游戏平台id查询
	 * @param gamelogoid
	 * @return
	 */
	List<SetGameCategoryDto> getTrunmanShowCategory(Integer gamelogoid);
}
