package com.wsdy.saasops.api.modules.transfer.mapper;


import com.wsdy.saasops.api.modules.user.dto.TransferRequestDto;
import com.wsdy.saasops.api.modules.user.dto.TransferResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AccountDepotMapper {

    int findAccountBalance(
            @Param("accountId") Integer accountId,
            @Param("balance") BigDecimal balance,
            @Param("depotId") Integer depotId,
            @Param("bonusAmount") BigDecimal bonusAmount);

    List<TransferResponseDto> findTransferList(TransferRequestDto dto);
}
