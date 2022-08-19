package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DPaySearchResponseDto {

    @ApiModelProperty(value = "商户网站订单号")
    private String outTradeNo;

    @ApiModelProperty(value = "0 失败 1 成功 2待处理 3 处理中")
    private Integer status;

    @ApiModelProperty(value = "公用回传参数")
    private String returnParams;

    @ApiModelProperty(value = "sign")
    private String sign;

    @ApiModelProperty(value = "备注")
    private String memo;
}
