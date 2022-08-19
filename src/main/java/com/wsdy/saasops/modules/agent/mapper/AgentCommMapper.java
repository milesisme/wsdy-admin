package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.modules.agent.dto.AgentReportDto;
import com.wsdy.saasops.modules.agent.dto.CommDetailsDto;
import com.wsdy.saasops.modules.agent.dto.DepotCostDto;
import com.wsdy.saasops.modules.agent.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AgentCommMapper {

	List<AgyDomain> findAgyDomainList(AgyDomain agyDomain);

	int findCountDomain(@Param("domainUrl") String domainUrl, @Param("id") Integer id);

	List<AgentReportDto> findReportList(@Param("startTime") String startTime, @Param("endTime") String endTime,
			@Param("id") Integer id);

	List<AgyCommission> commissionReviewList(AgyCommission commission);

	/**
	 * 代理报表
	 * 
	 * @param commission
	 * @return
	 */
	List<AgyCommission> commissionReport(AgyCommission commission);

	List<DepotCostDto> depotCostList(DepotCostDto depotCostDto);

	DepotCostDto sumDepotCost(DepotCostDto depotCostDto);

	List<DepotCostDto> depotCostDetail(DepotCostDto depotCostDto);

	List<CommDetailsDto> findCommissionDetails(@Param("time") String time,
			@Param("subAgyaccount") String subAgyaccount);

	BigDecimal sumCommissionReport(AgyCommission commission);

	List<AgyCommissionDepot> findCommissionDepot(@Param("orderNo") String orderNo);

	List<AgyCommission> commissionAllSubList(AgyCommission commission);
}
