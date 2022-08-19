package com.wsdy.saasops.modules.agent.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.wsdy.saasops.modules.agent.dto.AgyChannelLogDto;
import com.wsdy.saasops.modules.agent.entity.AgyChannelLog;
import com.wsdy.saasops.modules.base.mapper.MyMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2021-11-25
 */
@Mapper
@Component
public interface AgyChannelLogMapper extends MyMapper<AgyChannelLog> {

	/**
	 * 		统计渠道注册数据
	 * 			
	 * 	
	 * @param agyChannelLogDto
	 * @return
	 */
	List<AgyChannelLogDto> list(AgyChannelLogDto agyChannelLogDto);
}
