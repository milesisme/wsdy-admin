package com.wsdy.saasops.modules.sys.dao;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.sys.dto.SysWarningDto;
import com.wsdy.saasops.modules.sys.dto.SysWarningQueryDto;
import com.wsdy.saasops.modules.sys.entity.SysWarning;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysWarningMapper extends MyMapper<SysWarning> {
    List<SysWarningDto> list(SysWarningQueryDto sysWarningQueryDto);

    SysWarningDto getSysWarningByLoginNameAndType(String loginName, Integer type);
}
