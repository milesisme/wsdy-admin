package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class DPayResponseDto {

    @ApiModelProperty(value = "succeed")
    private Boolean succeed;

    @ApiModelProperty(value = "错误信息")
    private String error;

    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "订单号")
    private String orderNo;
}
