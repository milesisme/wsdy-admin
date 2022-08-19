package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.VerifyWarn;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface VerifyWarnMapper extends MyMapper<VerifyWarn> {

}
