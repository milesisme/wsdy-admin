package com.wsdy.saasops.modules.agent.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Setter
@Getter
@Table(name = "agy_commission_depot")
public class AgyCommissionDepot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "代理id")
    private Integer agentId;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "平台id")
    private Integer depotId;

    @ApiModelProperty(value = "输赢")
    private BigDecimal payout;

    @ApiModelProperty(value = "费用")
    private BigDecimal cost;

    @ApiModelProperty(value = "分类id")
    private Integer catId;

    @ApiModelProperty(value = "比例")
    private BigDecimal rate;

    private BigDecimal waterCost;

    private BigDecimal validbet;

    private BigDecimal waterrate;


    @Transient
    @ApiModelProperty(value = "场馆")
    private String depotname;
}