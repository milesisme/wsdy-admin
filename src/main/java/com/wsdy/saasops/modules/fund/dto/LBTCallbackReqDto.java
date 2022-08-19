package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "LBTCallbackReqDto", description = "回调请求参数")
public class LBTCallbackReqDto {
    @ApiModelProperty(value = "商户订单号")
    private String orderno;
    @ApiModelProperty(value = "通过 APPROVED  拒绝 REJECTED")
    private String status;
    @ApiModelProperty(value = "签名")
    private String sign;
    @ApiModelProperty(value = "备注")
    private String remarks;
    @ApiModelProperty(value = "实际入款金额")
    private String amount;
}
