package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TransferInOrOutDto {
    @ApiModelProperty(value = "错误代码")
    private String code;

    @ApiModelProperty(value = "错误消息")
    private String message;
}
