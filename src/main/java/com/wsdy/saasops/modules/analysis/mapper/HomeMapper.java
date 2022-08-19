package com.wsdy.saasops.modules.analysis.mapper;

import com.wsdy.saasops.modules.analysis.entity.RptMemberModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface HomeMapper {

    List<RptMemberModel> getRptMemberListEx(@Param("formate") String formate, @Param("startTime") String startTime, @Param("endTime") String endTime);

    List<RptMemberModel> getRptMemberListExother(@Param("formate") String formate, @Param("startTime") String startTime, @Param("endTime") String endTime);

}
