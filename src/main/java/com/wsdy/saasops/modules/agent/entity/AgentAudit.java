package com.wsdy.saasops.modules.agent.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Setter
@Getter
@Table(name = "agy_audit")
public class AgentAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "代理")
    private Integer agentId;

    @ApiModelProperty(value = "调整金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "创建人")
    private String modifyUser;

    @ApiModelProperty(value = "修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "状态 0 拒绝 1 通过 2待处理")
    private Integer status;

    @ApiModelProperty(value = "审核人")
    private String auditUser;

    @ApiModelProperty(value = "审核时间")
    private String auditTime;

    @ApiModelProperty(value = "调整类别code GA 人工增加 GM 人工减少")
    private String financialCode;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "转账记录id")
    private Integer billdetailId;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "钱包类型，0 佣金钱包1 代充钱包 2彩金钱包")
    private Integer walletType;

    @ApiModelProperty(value = "调整后主账户余额")
    private BigDecimal afterBalance;

    @ApiModelProperty(value = "调整前主账户余额")
    private BigDecimal beforeBalance;

    @Transient
    @ApiModelProperty(value = "开始时间开始")
    private String startTime;

    @Transient
    @ApiModelProperty(value = "开始时间结束")
    private String endTime;

    @Transient
    @ApiModelProperty(value = "钱包类型，0 佣金钱包1 代充钱包")
    private String walletTypeStr;
}