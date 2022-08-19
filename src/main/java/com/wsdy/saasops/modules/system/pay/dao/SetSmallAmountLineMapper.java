package com.wsdy.saasops.modules.system.pay.dao;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.system.pay.entity.SetSmallAmountLine;

/**
 * <p>
 * 小额客服线 Mapper 接口
 * </p>
 *
 * @author ${author}
 */
@Mapper
public interface SetSmallAmountLineMapper extends  MyMapper<SetSmallAmountLine> {

	/**
	 * 	根据会员组id查询是否存在
	 * 
	 * @param groupId
	 * @return
	 */
	SetSmallAmountLine selectByGroupId(Integer groupId);

}
