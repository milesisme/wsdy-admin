package com.wsdy.saasops.modules.agent.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Setter
@Getter
@Table(name = "agy_wallet")
public class AgyWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "Id")
    private Integer id;

    @ApiModelProperty(value = "代理Id")
    private Integer accountId;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "佣金余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "代充钱包")
    private BigDecimal rechargeWallet;

    @ApiModelProperty(value = "彩金钱包")
    private BigDecimal payoffWallet;

    @Transient
    private String financialCode;

    @Transient
    private String memo;

    @Transient
    private Integer agentId;

    @Transient
    private Integer merAccountId;

    @Transient
    private String orderNo;

    @Transient
    private String createuser;


    @ApiModelProperty(value = "钱包类型，0 佣金钱包1 代充钱包2彩金钱包")
    private Integer walletType;

    @Transient
    @ApiModelProperty(value = "提现中金额")
    private BigDecimal withdramAmount;


}