package com.wsdy.saasops.modules.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrActivityLevelHighest;

import tk.mybatis.mapper.common.IdsMapper;

/**
 * <p>
 * 	用户历史最高等级记录表 Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2022-04-13
 */
@Component
@Mapper
public interface MbrActivityLevelHighestMapper extends MyMapper<MbrActivityLevelHighest>,IdsMapper<MbrActivityLevelHighest> {

	/**
	 * 	更新会员最高等级
	 * 
	 * @param loginName
	 * @param accountLevel
	 * @return
	 */
	int updateLevelHighest(String loginName, Integer accountLevel);

	/**
	 * 	更新最新的恢复等级时间
	 * 
	 * @param loginName
	 */
	int updateRecoverTime(String loginName);

}
