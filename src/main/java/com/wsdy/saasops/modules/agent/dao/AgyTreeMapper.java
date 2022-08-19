package com.wsdy.saasops.modules.agent.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.wsdy.saasops.modules.agent.entity.AgyTree;
import com.wsdy.saasops.modules.base.mapper.MyMapper;

/**
 * <p>
 *  代理tree
 * </p>
 *
 * @since 2021-11-25
 */
@Mapper
@Component
public interface AgyTreeMapper extends MyMapper<AgyTree> {

	/**
	 * 	根据代理id，查询代理的级别
	 * @param agyId
	 * @return
	 */
	Integer getAgentTypeByAgyId(Integer agyId);

}
