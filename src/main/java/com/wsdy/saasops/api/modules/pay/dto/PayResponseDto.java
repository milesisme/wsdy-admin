package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class PayResponseDto {

    @ApiModelProperty(value = "支付方式 0 html 1二维码")
    private Integer urlMethod;

    @ApiModelProperty(value = "url 或者 html")
    private String url;

    @ApiModelProperty(value = "三方支付结果状态，默认：true")
    private Boolean status = Boolean.TRUE;

    @ApiModelProperty(value = "错误信息码")
    private String errMsg;

}
