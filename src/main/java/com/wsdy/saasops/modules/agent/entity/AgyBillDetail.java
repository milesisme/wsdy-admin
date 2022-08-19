package com.wsdy.saasops.modules.agent.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;


@Setter
@Getter
@ApiModel(value = "AgyBillDetail", description = "AgyBillDetail")
@Table(name = "agy_bill_detail")
public class AgyBillDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "产生交易记录order")
    private String orderNo;

    @ApiModelProperty(value = "代理名称")
    private String agyAccount;

    @ApiModelProperty(value = "代理ID")
    private Integer accountId;

    @ApiModelProperty(value = "财务类别代码")
    private String financialCode;

    @ApiModelProperty(value = "操作金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "操作后余额")
    private BigDecimal afterBalance;

    @ApiModelProperty(value = "操作前的余额")
    private BigDecimal beforeBalance;

    @ApiModelProperty(value = "操作类型，0 支出1 收入")
    private Integer opType;

    @ApiModelProperty(value = "生成订单时间")
    private String orderTime;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "转出上级代理or接收人代理id")
    private Integer agentId;

    @ApiModelProperty(value = "接收人会员")
    private Integer merAccountid;

    @ApiModelProperty(value = "创建人")
    private String createuser;

    @ApiModelProperty(value = "钱包类型，0 佣金钱包1 代充钱包2彩金钱包")
    private Integer walletType;

    @ApiModelProperty(value = "佣金余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "代充钱包")
    private BigDecimal rechargeWallet;
    @ApiModelProperty(value = "彩金钱包")
    private BigDecimal payoffWallet;
    @Transient
    @ApiModelProperty(value = "类型名称")
    private String codeName;

    @Transient
    @ApiModelProperty(value = "会员名")
    private String loginName;

    @Transient
    @ApiModelProperty(value = "查询时间开始")
    private String startTime;

    @Transient
    @ApiModelProperty(value = "查询时间结束")
    private String endTime;

    @Transient
    @ApiModelProperty(value = "查询时间开始")
    private String createTimeFrom;

    @Transient
    @ApiModelProperty(value = "查询时间结束")
    private String createTimeTo;

}