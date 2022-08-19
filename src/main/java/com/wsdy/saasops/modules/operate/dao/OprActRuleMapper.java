package com.wsdy.saasops.modules.operate.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.operate.entity.OprActRule;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.IdsMapper;

@Mapper
public interface OprActRuleMapper extends MyMapper<OprActRule>, IdsMapper<OprActRule> {

}
