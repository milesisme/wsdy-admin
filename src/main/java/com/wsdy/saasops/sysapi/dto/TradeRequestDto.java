package com.wsdy.saasops.sysapi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@ApiModel(value = "参数", description = "参数")
public class TradeRequestDto {

    private String siteCode;

    @ApiModelProperty(value = "会员账号")
    private String loginName;

    @ApiModelProperty(value = "数据中心交易号")
    private String outTradeno;

    @ApiModelProperty(value = "操作类型，0 减钱，1 收入，加钱")
    private Integer opType;

    @ApiModelProperty(value = "操作金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "平台code")
    private String depotCode;

    @ApiModelProperty(value = "财务类别代码")
    private String financialcode;
}
