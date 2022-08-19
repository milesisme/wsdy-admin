package com.wsdy.saasops.modules.analysis.mapper;

import com.wsdy.saasops.modules.analysis.dto.WinLostReportDto;
import com.wsdy.saasops.modules.analysis.dto.WinLostReportModelDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface WinLoseMapper {

    // 代理-类别表头-列表
    List<WinLostReportDto> findWinLostReportList(WinLostReportModelDto model);
    // 代理-类别表头-列表sum
    WinLostReportDto findWinLostSum(WinLostReportModelDto model);
    // 代理-直属会员列表
    List<WinLostReportDto> findAgyAccountLevelLoginName(WinLostReportModelDto model);

    // 会员-类别表头-列表-自己+下级
    List<WinLostReportDto> findWinLostReportListByLoginName(WinLostReportModelDto model);
    // 会员-类别表头-列表sum-自己+下级
    WinLostReportDto findWinLostListSumByLoginName(WinLostReportModelDto model);
    // 会员-下级会员(不含自己)
    List<WinLostReportDto> findWinLostListLevelLoginName(WinLostReportModelDto model);
    // 会员-只查询自己(会员列表)
    List<WinLostReportDto> findWinLostLoginName(WinLostReportModelDto model);

    // 会员-详情-类别统计表头-只查询自己
    List<WinLostReportDto> findWinLostAccount(WinLostReportModelDto model);
    // 会员-详情-类别统计表-会员自己sum
    WinLostReportDto findWinLostListSumLoginName(WinLostReportModelDto model);

    List<Map<String, Object>> findMbrWinLoseList(WinLostReportModelDto model);

    String findAgyAccountDepth(@Param("agyAccount") String agyAccount);
}
