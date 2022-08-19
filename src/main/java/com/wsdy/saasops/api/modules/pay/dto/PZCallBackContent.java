package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PZCallBackContent {

    @ApiModelProperty(value = "商户网站唯一订单号。")
    private String out_trade_no;

    @ApiModelProperty(value = "盘子支付交易号，该交易在盘子支付的交易流水号")
    private String trade_no;

    @ApiModelProperty(value = "订单金额，单位分")
    private int total_fee;

    @ApiModelProperty(value = "商品名称")
    private String subject;

    @ApiModelProperty(value = "交易状态，取值只有两个：SUCCESS\n" +
            "（表示交易成功），FAIL（表示交易\n" +
            "失败），大写")
    private String trade_status;

    @ApiModelProperty(value = "公用回传参数")
    private String extra;

    @ApiModelProperty(value = "该笔交易创建的时间")
    private String create_time;

    @ApiModelProperty(value = "第三方支付交易付款时间")
    private String pay_time;

    @ApiModelProperty(value = "错误信息")
    private String error;

    @ApiModelProperty(value = "sign")
    private String sign;

}
