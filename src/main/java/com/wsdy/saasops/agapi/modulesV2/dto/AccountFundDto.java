package com.wsdy.saasops.agapi.modulesV2.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class AccountFundDto {

    @ApiModelProperty(value = "用户id")
    private Integer accountId;

    @ApiModelProperty(value = "金额")
    private BigDecimal balance;

    @ApiModelProperty(value = "1增加 0减少")
    private Integer isBalance;
}
