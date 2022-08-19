package com.wsdy.saasops.modules.analysis.mapper;

import com.wsdy.saasops.modules.analysis.dto.WinLostReportDto;
import com.wsdy.saasops.modules.analysis.dto.WinLostReportModelDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WinLoseNewMapper {
    WinLostReportDto findWinLostReportView(WinLostReportModelDto model);
    List<WinLostReportDto> findWinLostReportViewAgent(WinLostReportModelDto model);
    WinLostReportDto findWinLostReportMbrView(WinLostReportModelDto model);
}
