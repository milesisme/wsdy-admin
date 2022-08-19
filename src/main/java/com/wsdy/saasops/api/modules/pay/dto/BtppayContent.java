package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class BtppayContent {

    @ApiModelProperty(value = "商户订单号")
    private String out_trade_no;

    @ApiModelProperty(value = "BTP订单号")
    private String trade_no;

    @ApiModelProperty(value = "订单金额")
    private Integer total_fee;

    @ApiModelProperty(value = "siteCode")
    private String subject;

    @ApiModelProperty(value = "seccess成功 fail失败")
    private String trade_status;

    @ApiModelProperty(value = "公用回传参数")
    private String extra;

    @ApiModelProperty(value = "订单创建时间")
    private String create_time;

    @ApiModelProperty(value = "订单匹配完成时间")
    private String pay_time;

    @ApiModelProperty(value = "错误信息")
    private String error;

    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "签名")
    private String url;
}
