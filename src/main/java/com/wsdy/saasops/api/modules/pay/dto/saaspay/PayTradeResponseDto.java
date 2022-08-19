package com.wsdy.saasops.api.modules.pay.dto.saaspay;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value = "PayTradeResponseDto", description = "返回商户系统参数")
public class PayTradeResponseDto {

    @ApiModelProperty(value = "支付方式 支付方式 0 html 1二维码  2银行卡转账")
    private Integer urlMethod;

    @ApiModelProperty(value = "url")
    private String url;

    @ApiModelProperty(value = "succeed")
    private Boolean succeed;

    @ApiModelProperty(value = "错误信息")
    private String error;

    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "订单号")
    private String orderNo;


}
