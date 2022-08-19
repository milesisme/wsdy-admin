package com.wsdy.saasops.agapi.modules.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.agapi.modules.dto.ReportParamsDto;
import com.wsdy.saasops.agapi.modules.dto.ReportResultDto;
import com.wsdy.saasops.agapi.modules.mapper.FinaceReportMapper;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.agent.dto.CostTotalDto;
import com.wsdy.saasops.modules.agent.dto.DepotCostDto;
import com.wsdy.saasops.modules.agent.mapper.CommissionCastMapper;

@Service
public class AgentFinaceReportService {

	@Autowired
	private FinaceReportMapper finaceReportMapper;
	@Autowired
	private CommissionCastMapper commissionCastMapper;

	public PageUtils agentFinanceReportList(ReportParamsDto dto, Integer pageNo, Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<ReportResultDto> dtos = finaceReportMapper.agentFinanceReportList(dto);
		// 不为空：不是分线代理，查询平台费服务费
		if (dto.getCagencyId() != null) {
			ReportParamsDto dtoSingDay = new ReportParamsDto();
			dtoSingDay.setAgyId(dto.getCagencyId());
			dtoSingDay.setAgyAccount(dto.getAgyAccount());
			for (ReportResultDto reportResultDto : dtos) {
				dtoSingDay.setStartTime(reportResultDto.getTime());
				dtoSingDay.setEndTime(reportResultDto.getTime());
				CostTotalDto cost = this.depotCostTotalForSingle(dtoSingDay);
				reportResultDto.setCost(cost.getCost());
				reportResultDto.setServiceCost(cost.getServiceCost());
				// 设置净输赢 减去平台费 服务费
				BigDecimal totalProfit = reportResultDto.getTotalProfit();
				reportResultDto.setTotalProfit(totalProfit.subtract(cost.getCost()).subtract(cost.getServiceCost()));
			}
		}
		
		return BeanUtil.toPagedResult(dtos);
	}

	public ReportResultDto agentFinanceReportTotal(ReportParamsDto dto) {
		return finaceReportMapper.agentFinanceReportTotal(dto);

	}

	public PageUtils depotCostDetailList(ReportParamsDto dto, Integer pageNo, Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<DepotCostDto> depotCostDetailList = finaceReportMapper.depotCostDetailList(dto);
		return BeanUtil.toPagedResult(depotCostDetailList);
	}

	/**
	 * 获取代理的平台费/服务费，根据代理的FeeModel计算 包括所有下级代理的会员
	 * 
	 * @return
	 */
	public CostTotalDto depotCostTotal(ReportParamsDto dto) {
		CostTotalDto costTotalDto = new CostTotalDto();
		// 1：平台费
		BigDecimal depotCostTotal = finaceReportMapper.depotCostTotal(dto);
		// 平台总费 = 平台费 +（平台费 * 额外的平台费率）
		costTotalDto.setCost(depotCostTotal);
		// 2：服务费
		costTotalDto.setServiceCost(commissionCastMapper.findChargCost(dto));
		return costTotalDto;
	}

	/**
	 * 获取代理的平台费/服务费，根据代理的FeeModel计算
	 * 	不会查询代理下级代理的会员
	 * 
	 * @return
	 */
	public CostTotalDto depotCostTotalForSingle(ReportParamsDto dto) {
		CostTotalDto costTotalDto = new CostTotalDto();
		// 1：平台费
		BigDecimal depotCostTotal = finaceReportMapper.depotCostTotalForSingle(dto);
		// 平台总费 = 平台费 +（平台费 * 额外的平台费率）
		costTotalDto.setCost(depotCostTotal);
		// 2：服务费
		costTotalDto.setServiceCost(commissionCastMapper.findChargCostForSingle(dto.getAgyId(), dto.getUsername(), dto.getAgyAccount(), dto.getStartTime(), dto.getEndTime()));
		return costTotalDto;
	}

}
