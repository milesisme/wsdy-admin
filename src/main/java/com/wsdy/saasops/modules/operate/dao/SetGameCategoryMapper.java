package com.wsdy.saasops.modules.operate.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.wsdy.saasops.api.modules.user.dto.ElecGameDto;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.operate.entity.OprGame;
import com.wsdy.saasops.modules.operate.entity.SetGameCategory;

import tk.mybatis.mapper.common.IdsMapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2021-12-27
 */
@Mapper
public interface SetGameCategoryMapper extends MyMapper<SetGameCategory>, IdsMapper<SetGameCategory> {

	List<SetGameCategory> list(SetGameCategory setGameCategory);

	List<SetGameCategory> getLotteryCategory(@Param("gamelogoid") Integer gamelogoid, String siteCode);

	/**
	 * 用户端根据分类查询彩票游戏，isTGmCatId=true查询默认游戏
	 * 
	 * @param elecGameDto
	 * @return
	 */
	List<OprGame> getGameByCategory(ElecGameDto elecGameDto);

	int insertSetGameCategory(SetGameCategory setGameCategory);

}
