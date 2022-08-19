package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "PZQueryContentDto", description = "盘子支付代付查询返回业务参数参数")
public class PZQueryContentDto {

    @ApiModelProperty(value = "交易状态，取值： SUCCESS 表示交易成功， FAIL 表示交易失败， PROCESS表示交易处理中， REFUND 表示退款,大写")
    private String trade_status;

    @ApiModelProperty(value = "商户网站唯一订单号")
    private String out_trade_no;

    @ApiModelProperty(value = "盘子代付交易号")
    private String trade_no;

    @ApiModelProperty(value = "订单金额，单位分")
    private int total_fee;

    @ApiModelProperty(value = "服务费， 0 表示不收服务费")
    private int service_fee;

    @ApiModelProperty(value = "开户银行代码， 详见： 银行代码")
    private String bank_code;

    @ApiModelProperty(value = "开户名称")
    private String card_name;

    @ApiModelProperty(value = "开户卡号、存折号")
    private String card_no;

    @ApiModelProperty(value = "开户银行省份")
    private String card_province;

    @ApiModelProperty(value = "开户银行城市")
    private String card_city;

    @ApiModelProperty(value = "开户银行网点")
    private String card_branch;

    @ApiModelProperty(value = "账户类型，取值： 1 表示银行卡， 2 表示存折， 3 表示存折， 4 表示支付宝")
    private int card_type;

    @ApiModelProperty(value = "账户属性，取值： 1 表示个人账户， 2表示公司账户")
    private int card_prop;

    @ApiModelProperty(value = "开户银行卡的身份证号码")
    private String id_card_no;

    @ApiModelProperty(value = "商品名称，是请求时对应的参数，原样通知回来")
    private String summary;

    @ApiModelProperty(value = "该笔交易创建的时间")
    private String create_time;

    @ApiModelProperty(value = "第三方支付交易付款时间")
    private String completed_time;

    @ApiModelProperty(value = "代付退款时间")
    private String refund_time;

    @ApiModelProperty(value = "第三方支付平台的代付交易号")
    private String debit_no;

    @ApiModelProperty(value = "错误信息")
    private String debit_error;

    @ApiModelProperty(value = "公用回传参数")
    private String extra;

    @ApiModelProperty(value = "签名")
    private String sign;
}
