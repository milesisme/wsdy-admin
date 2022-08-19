package com.wsdy.saasops.api.modules.pay.dto.evellet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvelletPayTransferCallbackDto {
    @ApiModelProperty(value = "币")
    private Long amount;
    @ApiModelProperty(value = "系统订单号")
    private String orderNo;
    @ApiModelProperty(value = "商户订单号")
    private String outTradeno;
    @ApiModelProperty(value = "updateTime")
    private String updateTime;
    @ApiModelProperty(value = "1处理中 2已打款 3失败")
    private Integer status;
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "备注")
    private String memo;
    @ApiModelProperty(value = "签名")
    private String sign;
    @ApiModelProperty(value = "hash")
    private String hash;
    @ApiModelProperty(value = "类型 TRC,ERC")
    private String type;
    @ApiModelProperty(value = "代理：AGENT 会员 ACCOUNT")
    private String userType;

}
