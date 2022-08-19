package com.wsdy.saasops.modules.activity.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;

@Data
@Table(name = "mbr_rebate_first_charge_reward")
public class MbrRebateFirstChargeReward {

    private static final long serialVersionUID = 1L;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "会员ID")
    private Integer accountId;
    @ApiModelProperty(value = "下级会员ID")
    private Integer subAccountId;
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "存款ID")
    private Integer depositId;
    @ApiModelProperty(value = "活动ID")
    private Integer activityId;
    @ApiModelProperty(value = "活动规则ID")
    private Integer ruleId;
    @ApiModelProperty(value = "存款金额")
    private BigDecimal depositedAmount;
    @ApiModelProperty(value = "金融代码")
    private String financialCode;
    @ApiModelProperty(value = "发放时间")
    private String applicationTime;
    @ApiModelProperty(value = "奖励红利")
    private BigDecimal bonusAmount;
    @ApiModelProperty(value = "优惠流水倍数")
    private BigDecimal discountAudit;
    @ApiModelProperty(value = "IP")
    private String ip;
    @ApiModelProperty(value = "设备来源; PC:0 H5:3")
    private Byte devSource;
    @ApiModelProperty(value = "转账记录ID")
    private Integer billDetailId;
    @ApiModelProperty(value = "稽核ID")
    private Integer auditId;
    @ApiModelProperty(value = "活动等级")
    private Integer  actLevelId;


    // 查询参数
    @Transient
    @ApiModelProperty(value = "开始时间")
    private String startTime;
    @Transient
    @ApiModelProperty(value = "结束时间")
    private String endTime;


}
