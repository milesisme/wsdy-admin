package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "PaymentPayResponseDto", description = "payment支付代付/状态查询 返回结果")
public class PaymentPayResponseDto {
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
    @ApiModelProperty(value = "付款完成时间")
    private String finishTime;
    @ApiModelProperty(value = "事件单号")
    private String eventNumber;
    @ApiModelProperty(value = "银行汇款成功返回的单号")
    private String bankOrderNumber;
    @ApiModelProperty(value = "户名")
    private String payName;
    @ApiModelProperty(value = "付款状态：1为汇款成功，0为失败,")
    private Integer payStatus;
    @ApiModelProperty(value = "sign字段为公钥加密的字段，并不是签名。可以用私钥解密，解密后的报文和回调中的明文字段是一样的信息")
    private String sign;
}
