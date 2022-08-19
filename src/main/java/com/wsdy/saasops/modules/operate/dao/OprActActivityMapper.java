package com.wsdy.saasops.modules.operate.dao;

import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import org.apache.ibatis.annotations.Mapper;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import tk.mybatis.mapper.common.IdsMapper;


@Mapper
public interface OprActActivityMapper extends MyMapper<OprActActivity>, IdsMapper<OprActActivity> {

}
