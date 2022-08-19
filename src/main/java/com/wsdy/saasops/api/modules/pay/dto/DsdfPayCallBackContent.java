package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DsdfPayCallBackContent {

    @ApiModelProperty(value = "接入商网站提交的订单号")
    private String order_id;

    @ApiModelProperty(value = "充值 or 提现")
    private String direction;

    @ApiModelProperty(value = "金额，单位 分，没有小数点")
    private Integer amount;

    @ApiModelProperty(value = "接入商网站的会员ID(提单API传递过来的)")
    private String customer_name;

    @ApiModelProperty(value = "siteCode")
    private String extend;

    @ApiModelProperty(value = "订单完成时间，UNIX时间戳秒值")
    private String verified_time;

    @ApiModelProperty(value = "订单创建时间，UNIX时间戳秒值")
    private String created_time;

    @ApiModelProperty(value = "订单状态：verified / timeout / revoked")
    private String status;

    @ApiModelProperty(value = "订单状态：order_success order_revoked order_timeout")
    private String cmd;

    @ApiModelProperty(value = "会员存款方式 remit: 银行卡转账 qrcode: 二维码存款 online: 在线网银 quick: 快捷支付")
    private String type;

    @ApiModelProperty(value = "收款卡银行(充值有此参数)")
    private String to_bankflag;

    @ApiModelProperty(value = "收款卡号(充值有此参数)")
    private String to_cardnumber;

    @ApiModelProperty(value = "收款卡姓名(充值有此参数)")
    private String to_username;

    @ApiModelProperty(value = "会员存款卡类型")
    private String customer_bankflag;

    @ApiModelProperty(value = "0 / 1  会员是否手机提单 (充值有此参数)")
    private String is_mobile;

    @ApiModelProperty(value = " 0 / 1  订单是否分配的WAP收款卡 (充值有此参数)")
    private String is_wap;

    @ApiModelProperty(value = "会员提现，系统使用的出款卡  (提现有此参数)")
    private String out_cardnumber;

    @ApiModelProperty(value = "会员提现，此笔订单的转账佣金 (提现有此参数)  单位 分")
    private Integer trans_fee;

    @ApiModelProperty(value = "简易签名，签名内容")
    private String qsign;
}
