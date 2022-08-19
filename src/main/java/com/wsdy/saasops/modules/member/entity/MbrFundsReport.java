package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel(value = "MbrFundsReport", description = "会员存取款与优惠每日统计")
@Table(name = "mbr_funds_report")
public class MbrFundsReport implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "报表日期")
    private String reportDate;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "存款金额")
    private BigDecimal deposit;

    @ApiModelProperty(value = "实际到账存款金额")
    private BigDecimal actualDeposit;

    @ApiModelProperty(value = "取款金额")
    private BigDecimal withdraw;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal bonus;

    @ApiModelProperty(value = "线上优惠金额")
    private BigDecimal onlineBonus;

    @ApiModelProperty(value = "线下优惠金额")
    private BigDecimal offlineBonus;

    @ApiModelProperty(value = "任务优惠金额")
    private BigDecimal taskBonus;

    @ApiModelProperty(value = "最后更新时间")
    private String lastupdate;

    @ApiModelProperty(value = "审核日期 当审核日期与申请日期不同时，累加数据")
    @Transient
    private String auditDate;
}