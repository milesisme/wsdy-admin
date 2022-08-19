package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeTransferParamDto {

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "转账金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "支付密码")
    private String securepwd;

    @ApiModelProperty(value = "1 个人账户  2代存钱包")
    private Integer type;

    @ApiModelProperty(value = "稽核倍数:彩金钱包上分")
    private Integer auditMultiple;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @ApiModelProperty(value = "本金稽核倍数")
    private Integer moneyMultiple;

    @ApiModelProperty(value = "本金金额:彩金钱包上分")
    private BigDecimal money;

}
