package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LBTQueryReponseDto {
    @ApiModelProperty(value = "状态码 200 请求成功")
    private Integer code;
    @ApiModelProperty(value = "状态信息 success 请求成功")
    private String msg;
    @ApiModelProperty(value = "商户订单号")
    private String orderno;
    @ApiModelProperty(value = "通过 APPROVED  拒绝 REJECTED")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remarks;
    @ApiModelProperty(value = "实际入款金额")
    private String amount;
}
