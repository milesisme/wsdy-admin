package com.wsdy.saasops.modules.member.mapper;

import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.dto.MbrWarningConditionDto;
import com.wsdy.saasops.modules.member.dto.MbrWarningDto;
import com.wsdy.saasops.modules.member.dto.MbrWarningQueryDto;
import com.wsdy.saasops.modules.member.entity.MbrWarning;
import com.wsdy.saasops.modules.member.entity.MbrWarningCondition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MbrWarningConditionMapper extends MyMapper<MbrWarningCondition> {
    List<MbrWarningConditionDto> list();
}
