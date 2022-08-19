package com.wsdy.saasops.api.modules.pay.dto.saaspay;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
@ApiModel(value = "PaySearchResponseDto", description = "查询订单返回数据")
public class PaySearchResponseDto {

    @ApiModelProperty(value = "商户网站订单号")
    private String outTradeNo;

    @ApiModelProperty(value = "0 失败 1 成功 2待处理")
    private Integer status;

    @ApiModelProperty(value = "公用回传参数")
    private String returnParams;

    @ApiModelProperty(value = "sign")
    private String sign;

    @ApiModelProperty(value = "实际到账: LBT需要使用该值处理实际到账")
    private BigDecimal actualarrival;

    @ApiModelProperty(value = "LBT需要返回remarks")
    private String memo;

}
