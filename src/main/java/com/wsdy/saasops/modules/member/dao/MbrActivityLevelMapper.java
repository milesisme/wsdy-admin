package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrActivityLevel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MbrActivityLevelMapper extends MyMapper<MbrActivityLevel>{

    int updateMbrActivityLevel(MbrActivityLevel level);
}
