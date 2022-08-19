package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "PaymentPayRequestDto", description = "Payment提单请求dto")
public class PaymentPayRequestDto {
    @ApiModelProperty(value = "商户号(商户id)")
    private String merchantNumber;
    @ApiModelProperty(value = "帐户名 收款帐户的帐户名")
    private String receiveName;
    @ApiModelProperty(value = "银行卡号 收款帐户的卡号")
    private String receiveCard;
    @ApiModelProperty(value = "金额 单位：元，精确到2位小数，非负数")
    private BigDecimal amount;
    @ApiModelProperty(value = "商户订单号 商户订单号不可重复，最多40位")
    private String merchantOrderNumber;
    @ApiModelProperty(value = "回调url 回调地址，最多200位，付款成功后请求此地址通知结果")
    private String callBackUrl;
    @ApiModelProperty(value = "sign 用rsa算法对发送的数据做签名，防止篡改回放攻击，")
    private String sign;
}