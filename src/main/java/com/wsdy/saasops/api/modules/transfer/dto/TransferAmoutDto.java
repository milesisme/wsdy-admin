package com.wsdy.saasops.api.modules.transfer.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class TransferAmoutDto {

    @ApiModelProperty(value = "是否转出（是否提示语） true 是  false 否")
    private Boolean isShow = Boolean.FALSE;

    @ApiModelProperty(value = "平台需要转出的余额")
    private BigDecimal amount;

    @ApiModelProperty(value = "平台name")
    private String depotName;

    @ApiModelProperty(value = "转账金额")
    private BigDecimal transferAmount;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal bounsAmount;
}
