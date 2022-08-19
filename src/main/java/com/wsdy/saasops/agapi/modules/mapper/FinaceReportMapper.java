package com.wsdy.saasops.agapi.modules.mapper;

import com.wsdy.saasops.agapi.modules.dto.ReportParamsDto;
import com.wsdy.saasops.agapi.modules.dto.ReportResultDto;
import com.wsdy.saasops.modules.agent.dto.DepotCostDto;

import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface FinaceReportMapper {

    List<ReportResultDto> agentFinanceReportList(ReportParamsDto paramDto);

	/**
	 * 查询 列表总计
	 * @param dto
	 * @return
	 */
	ReportResultDto agentFinanceReportTotal(ReportParamsDto dto);

	/**
	 * 查询平台费详情list
	 * @param dto
	 * @return
	 */
	List<DepotCostDto> depotCostDetailList(ReportParamsDto dto);

	/**
	 * 	查询代理下级所有平台费详情总计
	 * @param dto
	 * @return
	 */
	BigDecimal depotCostTotal(ReportParamsDto dto);
	
	
	/**
	 * 	查询单个代理平台费详情总计
	 * @param dto
	 * @return
	 */
	BigDecimal depotCostTotalForSingle(ReportParamsDto dto);

}
