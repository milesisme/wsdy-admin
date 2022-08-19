package com.wsdy.saasops.modules.base.entity;

import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Table(name="t_bs_bank")
@Getter
@Setter
public class BaseBank {
    /**
     * 
     * 表 : base_bank
     * 对应字段 : id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 
     * 表 : base_bank
     * 对应字段 : bankName
     */
    @Column
    private String bankName;

    /**
     * 
     * 表 : base_bank
     * 对应字段 : bankCode
     */
    @Column
    private String bankCode;

    /**
     * 
     * 表 : base_bank
     * 对应字段 : bankLog
     */
    @Column
    private String bankLog;
    /**
     * 是否支持取款(1,所有银行卡，0 支付分类 2同略云,4BTP,6财付宝，7东东支付，8个人二维码 9 加密货币)
     */
    @Column
    private Byte wDEnable;

    private String backBankImg;

    @Transient
    private int clientType;

    @ApiModelProperty(value = "加密货币：协议类型")
    private String category;
}