package com.wsdy.saasops.modules.agent.dto;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DepotCostDto {

    @ApiModelProperty(value = "代理")
    private String agyAccount;

    @ApiModelProperty(value = "输赢")
    private BigDecimal payout;

    @ApiModelProperty(value = "流水")
    private BigDecimal validbet;

    @ApiModelProperty(value = "平台费")
    private BigDecimal cost;

    @ApiModelProperty(value = "流水费")
    private BigDecimal waterCost;

    @ApiModelProperty(value = "平台费率")
    private BigDecimal rate;

    @ApiModelProperty(value = "流水费率")
    private BigDecimal waterrate;

    @ApiModelProperty(value = "会员账号")
    private String loginName;

    @ApiModelProperty(value = "场馆")
    private String depotname;

    @ApiModelProperty(value = "开始时间开始")
    private String startTime;

    @ApiModelProperty(value = "开始时间结束")
    private String endTime;

    @ApiModelProperty(value = "代理查直属会员")
    private String accountAgyAccount;

    @ApiModelProperty(value = "最外层查询代理")
    private String topAgyAccount;

    @ApiModelProperty(value = "直属代理id")
    private Integer cagencyId;
    
    @ApiModelProperty(value = "代理id")
    private Integer agyId;

    @ApiModelProperty(value = "分线id")
    private Integer subcagencyId;

    @ApiModelProperty(value = "分组属性")
    private Boolean groubyAgent = Boolean.FALSE;

    private String isSign;

    @ApiModelProperty(value = "结算费模式  1，平台费  2，服务费")
    private Integer feeModel;
}