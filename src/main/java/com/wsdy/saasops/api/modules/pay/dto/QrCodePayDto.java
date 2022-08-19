package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "QrCodePayDto", description = "前端展示个人二维码信息")
public class QrCodePayDto {

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "支付方式 0 银行卡转账 1二维码")
    private Integer urlMethod = 0;

    @ApiModelProperty(value = "二维码url")
    private String url;

    @ApiModelProperty(value = "订单生成时间")
    private String createTime;

    @ApiModelProperty(value = "附言单号")
    private String depositPostscript;

    @ApiModelProperty(value = "申请存款金额")
    private BigDecimal depositAmount;

}
