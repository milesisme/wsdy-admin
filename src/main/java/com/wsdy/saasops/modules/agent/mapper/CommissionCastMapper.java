package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.agapi.modules.dto.ReportParamsDto;
import com.wsdy.saasops.modules.agent.dto.CommissionCastDto;
import com.wsdy.saasops.modules.agent.dto.GroupDepotDto;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyTree;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CommissionCastMapper {

	List<AgentAccount> findAgentCommission();

	CommissionCastDto sumValidbet(@Param("startTime") String startTime, @Param("endTime") String endTime,
			@Param("cagencyid") Integer cagencyid, @Param("subcagencyId") Integer subcagencyId);

	CommissionCastDto sumPayoutBonusamount(@Param("startTime") String startTime, @Param("endTime") String endTime,
			@Param("cagencyid") Integer cagencyid, @Param("subcagencyId") Integer subcagencyId);

	List<GroupDepotDto> findGroupDepotPayout(@Param("startTime") String startTime, @Param("endTime") String endTime,
			@Param("cagencyid") Integer cagencyid, @Param("subcagencyId") Integer subcagencyId);

	BigDecimal findDepotRate(@Param("depotId") Integer depotId, @Param("catId") Integer catId);

	List<AgyTree> findAgyTree(@Param("childnodeid") Integer childnodeid);

	BigDecimal findAccountWater(@Param("cagencyid") Integer cagencyid, @Param("startTime") String startTime,
			@Param("endTime") String endTime);

	Integer agyCommissionProfitCount(@Param("agentid") Integer agentid, @Param("time") String time);

	/**
	 * 查询代理服务费，可以精确到单个会员
	 * @return
	 */
	BigDecimal findChargCost(ReportParamsDto reportParamsDto);

	/**
	 * 	当前代理的平台费， 不包括下级代理的
	 * 
	 * @param id
	 * @param object
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	BigDecimal findChargCostForSingle(@Param("cagencyid") Integer cagencyid, @Param("username") String username, @Param("agyAccount") String agyAccount,
			@Param("startTime") String startTime, @Param("endTime") String endTime);

	/**
	 * 	当前代理下的直属会员资金调整
	 * 
	 * @param agentid
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	BigDecimal getCalculateProfitOfAgent(@Param("agentid") Integer agentid, @Param("startTime") String startTime, @Param("endTime") String endTime);
}
