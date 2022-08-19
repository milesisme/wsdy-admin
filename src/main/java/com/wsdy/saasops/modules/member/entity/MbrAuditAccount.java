package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;


@Setter
@Getter
@ApiModel(value = "MbrAuditAccount", description = "稽核表")
@Table(name = "mbr_audit_account")
public class MbrAuditAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "会员ID")
    private Integer accountId;
    @ApiModelProperty(value = "时间")
    private String time;
    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validBet;
    @ApiModelProperty(value = "存款金额")
    private BigDecimal depositAmount;
 
    @ApiModelProperty(value = "稽核点流水要求：稽核倍数")
    private BigDecimal depositAudit;
    @ApiModelProperty(value = "存款输光余额： 放宽额度")
    private BigDecimal depositOutBalance;
    @ApiModelProperty(value = "存款稽核：流水需求")
    private BigDecimal auditAmount;
    @ApiModelProperty(value = "存款余额：即派彩+存款，初始值为派彩")
    private BigDecimal depositBalance;
    @ApiModelProperty(value = "累计剩余有效投注额： 溢出投注")
    private BigDecimal remainValidBet;
    @ApiModelProperty(value = "0 不通过 1通过")
    private Integer status;
    @ApiModelProperty(value = "是否提款 0否 1是 2提款中")
    private Integer isDrawings;
    @ApiModelProperty(value = "是输光 0否 1是")
    private Integer isOut;
    @ApiModelProperty(value = "存款，充值ID")
    private Integer depositId;
    @ApiModelProperty(value = "memo")
    private String memo;
    @ApiModelProperty(value = "")
    private String modifyUser;
    @ApiModelProperty(value = "")
    private String modifyTime;
    @ApiModelProperty(value = "派彩")
    private BigDecimal payOut;
    @ApiModelProperty(value = "转账使用存款 方便显示")
    private BigDecimal discardAmount;
    @ApiModelProperty(value = "优惠溢出有效投注 ：废弃")
    private BigDecimal bonusRemainValidBet;
    @ApiModelProperty(value = "奖励红利")
    private BigDecimal discountAmount;
    @ApiModelProperty(value = "规则id")
    private Integer ruleId;
    @ApiModelProperty(value = "通过时间")
    private String passTime;
    @ApiModelProperty(value = "转账减少的")
    private BigDecimal reduceAuditAmount;

    @ApiModelProperty(value = "稽核类型")
    private Integer auditType;

    @Transient
    @ApiModelProperty(value = "是否正序排序 true 是")
    private Boolean sort;
    @ApiModelProperty(value = "所需流水")
    @Transient
    private BigDecimal waterRequired;
}