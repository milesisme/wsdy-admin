package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.agapi.modules.dto.DataTrendDto;
import com.wsdy.saasops.agapi.modules.dto.DataTrendParamDto;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AgentHomeMapper {

    BigDecimal getHighestPayout(DataTrendParamDto dto);

    List<DataTrendDto> findNetwinLoseList(DataTrendParamDto dto);
}
