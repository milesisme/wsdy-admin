package com.wsdy.saasops.api.modules.transferNew.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GatewayResponseDto {

    @ApiModelProperty(value = "geteway返回code")
    private Boolean code;

    @ApiModelProperty(value = "geteway返回信息")
    private String message;

    @ApiModelProperty(value = "geteway返回status")
    private Boolean status;

    @ApiModelProperty(value = "msgCode")
    private String msgCode;
}
