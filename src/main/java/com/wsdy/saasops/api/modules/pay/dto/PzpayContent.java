package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class PzpayContent {

    @ApiModelProperty(value = "商户网站唯一订单号")
    private String out_trade_no;

    @ApiModelProperty(value = "盘子支付交易号")
    private String trade_no;

    @ApiModelProperty(value = "订单金额，单")
    private int total_fee;

    @ApiModelProperty(value = "获取二维码的状态，取值只有两个：SUCCESS（表示成功），FAIL（表示失败）")
    private String trade_status;

    @ApiModelProperty(value = "二维码的图片链接地址。UrlDecode 解码查看")
    private String code_url;

    @ApiModelProperty(value = "该笔交易创建的时间")
    private String create_time;

    @ApiModelProperty(value = "二维码失效时间，默认 15 分钟")
    private String expiredtim;

    @ApiModelProperty(value = "错误信息，发生错误时，返回第三方\n" +
            "支付或者盘子支付的错误信息。\n" +
            "UrlDecode 解码查看")
    private String erro;

    @ApiModelProperty(value = "sign")
    private String sign;
}
