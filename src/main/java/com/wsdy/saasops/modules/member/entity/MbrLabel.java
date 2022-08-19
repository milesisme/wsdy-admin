package com.wsdy.saasops.modules.member.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 会员标签表
 * </p>
 *
 * @author ${author}
 * @since 2022-05-06
 */
@Data
@ApiModel(value = "MbrLabel", description = "会员标签")
@Table(name = "mbr_label")
public class MbrLabel implements Serializable {

    private static final long serialVersionUID=1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    /**
     * 会员组名
     */
    private String name;

    /**
     * 是否开启1开启，0禁用
     */
    private Boolean isAvailable;

    /**
     * 是否设定规则 1是 0否
     */
    private Boolean isSetRule;

    /**
     * 出款方式 -支付宝
     */
    private BigDecimal aliPayWithdrawal;

    /**
     * 出款方式 -银行卡
     */
    private BigDecimal bankWithdrawal;

    /**
     * 是否免审alipay
     */
    private Boolean isExemptAliPay;

    /**
     * 是否免审银行卡
     */
    private Boolean isExemptBank;

    /**
     * 备注
     */
    private String memo;

}
