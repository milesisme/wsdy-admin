package com.wsdy.saasops.api.modules.user.mapper;


import com.wsdy.saasops.aff.dto.*;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SdyDataMapper {

	List<MbrAccount> findAcocountList(
			AccountListRequestDto dto);

	Integer findAccountIpCount(
			@Param("accountId") Integer accountId,
			@Param("registerip") String registerip);

	List<AccountDepositResponseDto> depositAndWithdrawalList(
			AccountDepositRequestDto dto);

	List<AuditBonusResponseDto> auditAndBonusList(
			AccountDepositRequestDto dto);

	List<AccountBetResponseDto> findAccountBet(
			AccountBetRequestDto dto);

	String maxDpAudittime(@Param("membercode") String membercode);

	String maxWdAudittime(@Param("membercode") String membercode);

	AccountBetResponseDto maxRptTime(@Param("membercode") String membercode);

	List<BonusWriteOffDto> findAdjustBonus(
			@Param("membercode") String membercode,
			@Param("startTime") String startTime,
			@Param("endTime") String endTime);

}
