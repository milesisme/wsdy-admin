package com.wsdy.saasops.modules.system.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "PayQuotaDto", description = "限额调整")
public class PayQuotaDto {

    @ApiModelProperty(value = "限额类型 1银行卡转账 2自动入款平台 3线上支付 4 普通扫码支付 5 数字货币")
    private Integer quotaType;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "每日最大存款限额")
    private BigDecimal dayMaxAmout;

    @ApiModelProperty(value = "单笔存款最小值")
    private BigDecimal minAmout;

    @ApiModelProperty(value = "单笔存款最大值")
    private BigDecimal maxAmout;

    @ApiModelProperty(value = "是否显示 0不显示 1显示")
    private Integer isShow;
}
