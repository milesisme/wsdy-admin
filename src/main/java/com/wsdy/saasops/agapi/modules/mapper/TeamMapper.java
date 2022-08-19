package com.wsdy.saasops.agapi.modules.mapper;


import com.wsdy.saasops.agapi.modules.dto.DirectMemberDto;
import com.wsdy.saasops.agapi.modules.dto.DirectMemberParamDto;
import com.wsdy.saasops.agapi.modules.dto.SubAgentListDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Mapper
public interface TeamMapper {

    List<DirectMemberDto> directMember(DirectMemberParamDto paramDto);

    BigDecimal findBonusAmount(@Param("loginname") String loginname);

    BigDecimal findValidbet(@Param("loginname") String loginname);

    List<SubAgentListDto> subAgentList(DirectMemberParamDto paramDto);

    List<SubAgentListDto> superiorCloneList(DirectMemberParamDto paramDto);

}
