package com.wsdy.saasops.modules.mbrRebateAgent.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "MbrRebateAgentRuleLevelSelfDto", description = "自身有效输赢提成比例")
public class MbrRebateAgentRuleLevelSelfDto {
    @ApiModelProperty(value = "累计实际有效输赢")
    private BigDecimal amountMin;
    @ApiModelProperty(value = "累计实际有效输赢")
    private BigDecimal amountMax;
    @ApiModelProperty(value = "提成比例")
    private BigDecimal donateAmount;
}
