package com.wsdy.saasops.api.modules.transferNew.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GatewayRequestDto {

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "siteCode")
    private String siteCode;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "金额")
    private String amount;

    @ApiModelProperty(value = "请求时间戳秒")
    private String timeStamp;

}
