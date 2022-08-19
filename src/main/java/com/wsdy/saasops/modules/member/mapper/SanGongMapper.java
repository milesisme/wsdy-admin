package com.wsdy.saasops.modules.member.mapper;

import com.wsdy.saasops.modules.member.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface SanGongMapper {

    List<MbrAccount> findAccountListByCagencyid(
            @Param("cagencyid") Integer cagencyid);

    List<MbrAccount> findSuperiorAccountList(
            @Param("id") Integer id);

    int updateBatchRebateReport(
            @Param("auditId") Integer auditId,
            @Param("reportIds") List<Integer> reportIds);

}
