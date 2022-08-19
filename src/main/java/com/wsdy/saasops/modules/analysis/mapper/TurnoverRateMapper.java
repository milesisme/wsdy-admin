package com.wsdy.saasops.modules.analysis.mapper;

import com.wsdy.saasops.modules.analysis.dto.RptBetRcdDayDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface TurnoverRateMapper {

    List<RptBetRcdDayDto> findRptBetRcdDayRateList(@Param("num") Integer num);

    BigDecimal findSetGameRate(@Param("platform") String platform,
                               @Param("gametype") String gametype);

    BigDecimal findDepotRate(@Param("catid") Integer catid,
                             @Param("depotcode") String depotcode);

    int updateRptBetRcdDayCost(@Param("cost") BigDecimal cost,
                               @Param("israte") Integer israte,
                               @Param("waterrate") BigDecimal waterrate,
                               @Param("id") Integer id);
}
