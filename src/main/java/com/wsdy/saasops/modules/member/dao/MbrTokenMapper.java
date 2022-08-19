package com.wsdy.saasops.modules.member.dao;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrToken;


@Mapper
public interface MbrTokenMapper extends MyMapper<MbrToken> {

}
