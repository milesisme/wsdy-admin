package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.agapi.modules.dto.DataTrendDto;
import com.wsdy.saasops.agapi.modules.dto.DataTrendParamDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DataTrendMapper {

    List<DataTrendDto> findNetwinLoseList(DataTrendParamDto dto);

    List<DataTrendDto> findDepositList(DataTrendParamDto dto);

    List<DataTrendDto> findFirstDepositList(DataTrendParamDto dto);

    List<DataTrendDto> findValidbetList(DataTrendParamDto dto);

    List<DataTrendDto> findWithdrawList(DataTrendParamDto dto);

    List<DataTrendDto> findRegisterList(DataTrendParamDto dto);

    List<DataTrendDto> findFirstDepositNumList(DataTrendParamDto dto);

    List<DataTrendDto> findDepositNumList(DataTrendParamDto dto);

    List<DataTrendDto> findWithdrawNumList(DataTrendParamDto dto);

    List<DataTrendDto> findValidbetNumList(DataTrendParamDto dto);
}
