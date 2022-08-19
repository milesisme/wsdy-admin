package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class PayPictureData {

    @ApiModelProperty(value = "银行卡ID")
    private Integer bankCardId;

    @ApiModelProperty(value = "银行卡名称")
    private String bankName;

    @ApiModelProperty(value = "银行卡code")
    private String bankCode;

    @ApiModelProperty(value = "银行卡log")
    private String bankLog;

}
