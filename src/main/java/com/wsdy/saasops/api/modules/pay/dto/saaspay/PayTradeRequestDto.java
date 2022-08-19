package com.wsdy.saasops.api.modules.pay.dto.saaspay;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
@ApiModel(value = "PayTradeRequest", description = "商户支付入参")
public class PayTradeRequestDto {

    @ApiModelProperty(value = "充值数值元")
    private BigDecimal amount;

    @ApiModelProperty(value = "银行代码")
    private String bankCode;

    @ApiModelProperty(value = "公用回传参数，不参与签名")
    private String returnParams;

    @ApiModelProperty(value = "回调url 请不要在链接后面加自定义参数")
    private String callbackUrl;

    @ApiModelProperty(value = "用户支付订单ip")
    private String ip;

    @ApiModelProperty(value = "商户网站订单号")
    private String outTradeNo;

    @ApiModelProperty(value = "商户号")
    private String merchantNo;

    @ApiModelProperty(value = "标识收款渠道")
    private String evbBankId;

    @ApiModelProperty(value = "第三方商户号 当customizeMerchantNo不为空 将优先使用")
    private String customizeMerchantNo;

    @ApiModelProperty(value = "第三方商户号密钥")
    private String customizeMerchantKey;

    @ApiModelProperty(value = "第三方收款账号 id")
    private String customizeMerchantBankId;

    @ApiModelProperty(value = "sign")
    private String sign;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;
    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "支付终端 PC：0， H5：3")
    private Byte terminal;

    @ApiModelProperty(value = "付款卡的网银登录名，可以为空字符串")
    private String payUserName;
}
