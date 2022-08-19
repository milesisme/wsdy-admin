package com.wsdy.saasops.modules.analysis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.wsdy.saasops.modules.analysis.entity.RptBetRcdDayLog;
import com.wsdy.saasops.modules.base.mapper.MyMapper;

@Mapper
public interface RptBetRcdDayLogMapper extends MyMapper<RptBetRcdDayLog> {

	List<RptBetRcdDayLog> selectList(RptBetRcdDayLog rptBetRcdDayLog);

}
