package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "PaymentPayBaseResponseDto", description = "Payment代付响应base dto")
public class LBTReponseDto {
    @ApiModelProperty(value = "状态码 200 请求成功")
    private Integer code;
    @ApiModelProperty(value = "返回信息 success提单成功 Forbidden 决绝 如白名单")
    private String msg;
}
