package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "CrPayDto", description = "前端展示加密货币信息")
public class CrPayDto {

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "钱包地址")
    private String walletAddress;

    @ApiModelProperty(value = "订单生成时间")
    private String createTime;

    @ApiModelProperty(value = "申请存款金额")
    private BigDecimal depositAmount;
    @ApiModelProperty(value = "预计到账金额")
    private BigDecimal depositAmountCNY;
    @ApiModelProperty(value = "二维码 base64")
    private String qrCode;

}
