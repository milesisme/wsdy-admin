package com.wsdy.saasops.modules.fund.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Transient;
import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "DepositStatisticsByPayDto", description = "DTO对象")
public class DepositStatisticsByPayDto {

    @Transient
    @ApiModelProperty(value = "")
    private Integer id;

    @Transient
    @ApiModelProperty(value = "支付平台名称")
    private String payname;

    @Transient
    @ApiModelProperty(value = "存款金额")
    private BigDecimal depositAmountTotal;

    @Transient
    @ApiModelProperty(value = "支付平台名称类型:支付宝1，微信2，网银支付3，极速支付4，其他支付5")
    private String type;

    @Transient
    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @Transient
    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @Transient
    @ApiModelProperty(value = "代理 0正式 1测试")
    private Integer isTest;

    @Transient
    @ApiModelProperty(value = "代理名称")
    private String agyAccountStr;

    @Transient
    @ApiModelProperty(value = "代理ID: 代理后台使用的查询条件")
    private Integer agyId;
}
