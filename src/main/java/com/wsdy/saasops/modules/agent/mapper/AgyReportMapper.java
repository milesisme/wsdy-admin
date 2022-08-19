package com.wsdy.saasops.modules.agent.mapper;

import com.wsdy.saasops.modules.agent.entity.AgyBillDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgyReportMapper {

    List<AgyBillDetail> upperScoreRecord(AgyBillDetail detail);

    List<AgyBillDetail> agentAccountChange(AgyBillDetail detail);
}
