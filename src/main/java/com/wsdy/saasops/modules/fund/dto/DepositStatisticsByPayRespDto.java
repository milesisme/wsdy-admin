package com.wsdy.saasops.modules.fund.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@ApiModel(value = "DepositStatisticsByPayRespDto", description = "DTO对象")
public class DepositStatisticsByPayRespDto {
    @Transient
    @ApiModelProperty(value = "支付平台名称类型:支付宝1，微信2，网银支付3，极速支付4，其他支付5")
    private String type;

    @Transient
    @ApiModelProperty(value = "存款金额")
    private BigDecimal depositAmountSum;

    @Transient
    @ApiModelProperty(value = "支付线路存款统计list")
    List<DepositStatisticsByPayDto> list;
}
