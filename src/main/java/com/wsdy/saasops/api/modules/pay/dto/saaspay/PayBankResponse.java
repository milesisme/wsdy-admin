package com.wsdy.saasops.api.modules.pay.dto.saaspay;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayBankResponse {

    @ApiModelProperty(value = "succeed")
    private Boolean succeed = Boolean.FALSE;

    @ApiModelProperty(value = "错误信息")
    private String error;

    @ApiModelProperty(value = "第三方返回报文")
    private String returnText;

    @ApiModelProperty(value = "返回数据")
    private String data;

    @ApiModelProperty(value = "支付方式 0银行卡转账 1二维码 2html 3url")
    private Integer urlMethod = 0;

    private String sign;
}