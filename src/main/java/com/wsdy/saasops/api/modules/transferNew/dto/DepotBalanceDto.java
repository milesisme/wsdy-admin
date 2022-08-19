package com.wsdy.saasops.api.modules.transferNew.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepotBalanceDto {

    @ApiModelProperty(value = "平台操作后余额")
    private BigDecimal depotAfterBalance;

    @ApiModelProperty(value = "平台操作前余额")
    private BigDecimal depotBeforeBalance;
}
