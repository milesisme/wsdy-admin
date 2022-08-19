package com.wsdy.saasops.api.modules.pay.dto.evellet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class EvelletPayTradeRequestDto {

    @ApiModelProperty(value = "货币")
    private Long amount;

    @ApiModelProperty(value = "商户号")
    private String merchantNo;

    @ApiModelProperty(value = "提交时间")
    private String applyDate;

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "类型 TRC,ERC")
    private String type;

    @ApiModelProperty(value = "代理：AGENT 会员 ACCOUNT")
    private String userType;

    @ApiModelProperty(value = "签名")
    private String sign;
}
