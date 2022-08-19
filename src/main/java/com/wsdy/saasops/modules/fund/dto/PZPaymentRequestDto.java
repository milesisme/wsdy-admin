package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "PZPaymentRequestDto", description = "盘子支付代付参数")
public class PZPaymentRequestDto {

    @ApiModelProperty(value = "商户 ID，盘子支付提供")
    private String partner_id;

    @ApiModelProperty(value = "收款账号 id，可在商户系统中获取。注意不是银行编码")
    private String bank_id;

    @ApiModelProperty(value = "开户银行代码， 详见： 银行代码")
    private String bank_code;

    @ApiModelProperty(value = "开户名称不参与签名")
    private String card_name;

    @ApiModelProperty(value = "开户卡号、存折号")
    private String card_no;

    @ApiModelProperty(value = "账户类型，取值： 1 表示银行卡， 2 表示存折")
    private String card_type;

    @ApiModelProperty(value = "账户属性，取值： 1 表示个人账户， 2表示公司账户")
    private String card_prop;

    @ApiModelProperty(value = "合作商户网站唯一订单号")
    private String out_trade_no;

    @ApiModelProperty(value = "订单金额，单位： 分，仅支持人民币")
    private String total_fee;

    @ApiModelProperty(value = "异步通知 url")
    private String notify_url;

    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "开户银行省份")
    private String card_province;

    @ApiModelProperty(value = "开户银行城市")
    private String card_city;

    @ApiModelProperty(value = "开户银行网点 分行")
    private String card_branch;

}
