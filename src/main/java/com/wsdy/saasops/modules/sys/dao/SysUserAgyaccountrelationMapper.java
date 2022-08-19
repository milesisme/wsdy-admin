package com.wsdy.saasops.modules.sys.dao;

import com.wsdy.saasops.modules.sys.entity.SysUserAgyaccountrelation;
import org.apache.ibatis.annotations.Mapper;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import org.springframework.stereotype.Component;


@Mapper
@Component
public interface SysUserAgyaccountrelationMapper extends MyMapper<SysUserAgyaccountrelation> {

	public void deleteBatchByUserId(Long userId);
}
