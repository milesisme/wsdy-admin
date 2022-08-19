package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "PaymentPayBalanceResponseDto", description = "商户提单返回")
public class PaymentPayExecuteResponseDto {
    @ApiModelProperty(value = "手续费")
    private BigDecimal server;
    @ApiModelProperty(value = "卡号")
    private String payCard;
    @ApiModelProperty(value = "商户发送来的订单号")
    private String merchantOrderNumber;
    @ApiModelProperty(value = "商户号(商户id)")
    private String merchantNumber;
    @ApiModelProperty(value = "金额 单位：元，精确到2位小数，非负数")
    private BigDecimal amount;
    @ApiModelProperty(value = "事件单号")
    private String eventNumber;
    @ApiModelProperty(value = "户名")
    private String payName;
}
