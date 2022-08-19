package com.wsdy.saasops.modules.member.dto;

import com.wsdy.saasops.modules.member.entity.MbrAuditBonus;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BounsValidBetDto {

    @ApiModelProperty(value = "剩余有效投注额，稽核方便使用")
    private BigDecimal sumValidBet = BigDecimal.ZERO;

    private List<MbrAuditBonus> auditBonuses;

    private List<Integer> depotIds = Lists.newArrayList();
}
