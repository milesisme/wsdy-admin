package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "PZQueryRequestDto", description = "盘子支付代付查询参数")
public class PZQueryRequestDto {

    @ApiModelProperty(value = "商户 ID，盘子支付提供")
    private String partner_id;

    @ApiModelProperty(value = "商户网站唯一订单号")
    private String out_trade_no;

    @ApiModelProperty(value = "盘子代付交易号，该交易在盘子的代付交易流水号")
    private String trade_no;

    @ApiModelProperty(value = "签名")
    private String sign;
}
