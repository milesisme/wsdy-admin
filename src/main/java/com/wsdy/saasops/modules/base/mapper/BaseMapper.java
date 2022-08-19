package com.wsdy.saasops.modules.base.mapper;

import java.util.List;


import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.wsdy.saasops.modules.base.entity.BaseArea;
import com.wsdy.saasops.modules.base.entity.TWinTop;
import com.wsdy.saasops.modules.base.entity.ToprAdv;

@Mapper
public interface BaseMapper {
	List<BaseArea> findBaseArea(BaseArea baseArea);

	List<TWinTop> findTopWinList(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("rows") Integer rows);

	List<ToprAdv> queryWebOprAdvList(ToprAdv oprAdv);

	List<BaseBank> findBankList(@Param("payId") Integer payId);

	List<String> getApiPrefixBySiteCode(@Param("siteCode") String siteCode);

	List<TGmDepot> findDepotCodesById(@Param("depotIds") List<Integer> depotIds);
}
