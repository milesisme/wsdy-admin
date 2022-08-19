package com.wsdy.saasops.modules.member.mapper;


import com.wsdy.saasops.modules.member.dto.AuditDetailDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAuditAccount;
import com.wsdy.saasops.modules.member.entity.MbrAuditBonus;
import com.wsdy.saasops.modules.member.entity.MbrAuditHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuditMapper {

    List<MbrAuditAccount> finAuditList(MbrAuditAccount mbrAccountAudit);

    List<MbrAuditBonus> finAuditBonusList(MbrAuditBonus auditBonus);

    List<AuditDetailDto> findAuditAccountList(
            @Param("accountId") Integer accountId,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("isDrawings") Integer isDrawings);

    List<Integer> findAuditAccountId(@Param("accountId") Integer accountId);

    List<MbrAuditHistory> findMbrAuditHistory(MbrAuditHistory auditHistory);

    List<MbrAuditBonus> fundAuditBonusByTime(MbrAuditBonus auditBonus);

    int findAuditAccountBounsCount(@Param("accountId") Integer accountId);

    int findAuditAccountPreferential(
            @Param("accountId") Integer accountId,
            @Param("tmplCode") String tmplCode);

    Integer findAuditAccountWithoutWater(@Param("accountId") Integer accountId);


    List<MbrAuditAccount> findAuditAccountPreferentialEx(
            @Param("accountId") Integer accountId,
            @Param("tmplCode") String tmplCode);

    MbrAccount findAccountByIdAndTime(
            @Param("accountId") Integer accountId,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    List<MbrAuditAccount> finAuditByLoginNameList(@Param("accountId") Integer accountId);
}
