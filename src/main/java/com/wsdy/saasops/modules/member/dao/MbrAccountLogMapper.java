package com.wsdy.saasops.modules.member.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccountLog;
import com.wsdy.saasops.modules.sys.dto.SysUserLastLoginTimeDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MbrAccountLogMapper extends MyMapper<MbrAccountLog>{

    List<SysUserLastLoginTimeDto> findUserLastLogTime(@Param("userNameList") List<String> userNameList);
}
