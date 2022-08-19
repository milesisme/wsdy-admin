package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "PZPaymentResponseDto", description = "盘子支付代付响应参数")
public class PZPaymentResponseDto {

    @ApiModelProperty(value = "商户网站唯一订单号")
    private String out_trade_no;

    @ApiModelProperty(value = "盘子交易号，该交易在盘子支付的交易流水号")
    private String trade_no;

    @ApiModelProperty(value = "代付金额，单位分")
    private int total_fee;

    @ApiModelProperty(value = "交易状态，取值：SUCCESS 表示交易成功， FAIL表示交易失败， PROCESS表示交易处理中，REFUND表示退款，大写")
    private String trade_status;

    @ApiModelProperty(value = "公用回传参数")
    private String extra;

    @ApiModelProperty(value = "该笔交易创建的时间")
    private String create_time;

    @ApiModelProperty(value = "代付完成时间")
    private String completed_time;

    @ApiModelProperty(value = "代付退款时间")
    private String refund_time;

    @ApiModelProperty(value = "错误信息")
    private String error;

    @ApiModelProperty(value = "签名")
    private String sign;
}
