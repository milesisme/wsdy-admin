package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class BtppayReturnContent {

    @ApiModelProperty(value = "本次请求标识符，用于后续【订单状态检 测】，【订单确认】接口的请求参数")
    private String ruid;

    @ApiModelProperty(value = "商户号")
    private String partner_id;

    @ApiModelProperty(value = "订单号")
    private String out_trade_no;

    @ApiModelProperty(value = "BTP订单号")
    private String trade_no;

    @ApiModelProperty(value = "订单金额")
    private Integer total_fee;

    @ApiModelProperty(value = "收款银行编码")
    private String bank_code;

    @ApiModelProperty(value = "收款银行名称")
    private String bank_name;

    @ApiModelProperty(value = "收款银行账号")
    private String bank_account;

    @ApiModelProperty(value = "收款银行开户名称")
    private String bank_account_name;

    @ApiModelProperty(value = "收款附言")
    private String bank_comment;

    @ApiModelProperty(value = "收款银行账号的开户支行")
    private String bank_branch;

    @ApiModelProperty(value = "支付宝扫码链接")
    private String alipay_url;

    @ApiModelProperty(value = "创建时间")
    private String created_time;

    @ApiModelProperty(value = "签名")
    private String sign;
}
