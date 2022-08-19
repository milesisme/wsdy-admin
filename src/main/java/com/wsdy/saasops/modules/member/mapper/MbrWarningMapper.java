package com.wsdy.saasops.modules.member.mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.dto.MbrWarningDto;
import com.wsdy.saasops.modules.member.dto.MbrWarningQueryDto;
import com.wsdy.saasops.modules.member.entity.MbrWarning;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MbrWarningMapper  extends MyMapper<MbrWarning> {
    List<MbrWarningDto> list(MbrWarningQueryDto mbrWarningQueryDto);

    Integer getWarningInfoCount(String calcDay, Integer type, String exContent, String loginName, Integer days);


    Integer getMbrWarningCount(String loginName);
}
