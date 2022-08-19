package com.wsdy.saasops.api.modules.pay.dto.evellet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonEvelletResponse<T> {

    private Integer code;
    private String msg;
    private T data;
    @ApiModelProperty(value = "汇率")
    private String rate;
    @ApiModelProperty(value = "入款地址")
    private String address;
    @ApiModelProperty(value = "二维码 base64")
    private String qrCode;
}
