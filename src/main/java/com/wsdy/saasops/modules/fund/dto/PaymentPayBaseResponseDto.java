package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "PaymentPayBaseResponseDto", description = "Payment代付响应base dto")
public class PaymentPayBaseResponseDto {
    @ApiModelProperty(value = "状态码:汇款结果的状态，1为汇款成成功 ")
    private Integer status;
    @ApiModelProperty(value = "状态描述")
    private String msg;
    @ApiModelProperty(value = "时间")
    private String responseTime;

    @ApiModelProperty(value = "查询数据体： 包含各种参数，详细见示例回应数据体")
    private Object data;
}
