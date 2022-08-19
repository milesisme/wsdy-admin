package com.wsdy.saasops.modules.member.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Table(name = "mbr_wallet")
public class MbrWallet implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "会员账号")
    private String loginName;

    @ApiModelProperty(value = "主账户余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "呼朋余额")
    private BigDecimal huPengBalance;
    
    @ApiModelProperty(value = "核对调整 + bonusAmounts + bonusAmountTotal + amounts + offlinedepositAmounts + onlinedepositAmounts + payout - withdrawdrawingAmounts = 账目核对")
    private BigDecimal adjustment;

    @Transient
    @ApiModelProperty(value = "免转钱包开关：0关;1开")
    private Integer freeWalletSwitch;

    @Transient
    @ApiModelProperty(value = "平台余额")
    private BigDecimal depotBeforeBalance;

    @Transient
    @ApiModelProperty(value = "订单号")
    private Long orderNumber;

    @Transient
    @ApiModelProperty(value = "回收平台账号余额")
    private Integer[] depotIds;
    
    @Transient
    @ApiModelProperty(value = "优惠金额")
    private BigDecimal bonusAmount = BigDecimal.ZERO;
}