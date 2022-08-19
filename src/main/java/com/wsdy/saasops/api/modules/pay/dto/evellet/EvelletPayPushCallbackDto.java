package com.wsdy.saasops.api.modules.pay.dto.evellet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EvelletPayPushCallbackDto {
    @ApiModelProperty(value = "币")
    private Long amount;
    @ApiModelProperty(value = "hash")
    private String hash;
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "创建时间")
    private String createTime;
    @ApiModelProperty(value = "汇率")
    private BigDecimal exchangeRate;
    @ApiModelProperty(value = "签名")
    private String sign;
    @ApiModelProperty(value = "类型 TRC,ERC")
    private String type;
    @ApiModelProperty(value = "代理：AGENT 会员 ACCOUNT")
    private String userType;

    // TODO
    @ApiModelProperty(value = "货币类型 USDT")
    private String currencyCode;
    @ApiModelProperty(value = "协议类型 ERC20 TRC20")
    private String currencyProtocol;
}
