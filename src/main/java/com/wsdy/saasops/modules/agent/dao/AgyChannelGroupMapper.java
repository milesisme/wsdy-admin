package com.wsdy.saasops.modules.agent.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.wsdy.saasops.modules.agent.dto.AgyChannelGroupDto;
import com.wsdy.saasops.modules.agent.entity.AgyChannelGroup;
import com.wsdy.saasops.modules.base.mapper.MyMapper;

/**
 * <p>
 *  渠道分組Mapper 接口
 * </p>
 *
 * @since 2021-11-24
 */
@Mapper
@Component
public interface AgyChannelGroupMapper extends MyMapper<AgyChannelGroup> {

	List<AgyChannelGroup> list(AgyChannelGroupDto agyChannelGroup);

}
