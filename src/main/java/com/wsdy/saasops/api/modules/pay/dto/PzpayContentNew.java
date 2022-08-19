package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class PzpayContentNew {

    @ApiModelProperty(value = "支付信息类型。取值：HTML, QR")
    private String type;

    @ApiModelProperty(value = "支付信息内容，使用时需 decode。\n" +
            "type=HTML 表示 html 代码，该代码会\n" +
            "自动提交。\n" +
            "type=QR 表示二维码 url 地址")
    private String explain;

    @ApiModelProperty(value = "扫码方式的标题。\n" +
            "type=QR 时，为实际扫码方式标题。\n" +
            "不参与签名")
    private String qr_title;

    @ApiModelProperty(value = "sign")
    private String sign;
}
