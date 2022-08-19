package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.TempAccountId;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface TempAccountMapper extends MyMapper<TempAccountId>{

}
