package com.wsdy.saasops.modules.agent.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.wsdy.saasops.modules.agent.dto.AgyChannelDto;
import com.wsdy.saasops.modules.agent.dto.AgyChannelForApiDto;
import com.wsdy.saasops.modules.agent.entity.AgyChannel;
import com.wsdy.saasops.modules.base.mapper.MyMapper;

/**
 * <p>
 *  渠道Mapper 接口
 * </p>
 *
 * @since 2021-11-25
 */
@Mapper
@Component
public interface AgyChannelMapper extends MyMapper<AgyChannel> {

	List<AgyChannel> list(AgyChannelDto agyChannelDto);

	/**
	 * 
	 * 根据主号查询
	 * @param masterNum
	 * @return
	 */
	AgyChannelForApiDto selectByMasterNum(String masterNum);

	/**
	 * 
	 * 根据号码（主号或者副号 ）返回当前渠道对象
	 * @param num
	 * @return
	 */
	AgyChannel getByNum(String num);
	
	/**
	 * 
	 * 	更新时 根据号码（主号或者副号 ）查询其他渠道相同名字的
	 * @param num
	 * @return
	 */
	AgyChannel getByNumAndId(Integer id, String num);

	/**
	 * 	根据渠道组id查询下面的渠道数量
	 * @param id
	 * @return
	 */
	int getCountByGroupId(Integer groupId);

}
