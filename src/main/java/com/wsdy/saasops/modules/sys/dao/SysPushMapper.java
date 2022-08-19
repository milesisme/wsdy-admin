package com.wsdy.saasops.modules.sys.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.sys.entity.SysPush;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface SysPushMapper extends MyMapper<SysPush> {


}
