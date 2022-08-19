package com.wsdy.saasops.modules.mbrRebateAgent.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;


@Setter
@Getter
@ApiModel(value = "MbrRebateAgentBonus", description = "全民代理审核表")
@Table(name = "mbr_rebate_agent_bonus")
public class MbrRebateAgentBonus implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "会员ID")
    private Integer accountId;
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "创建人")
    private String createUser;
    @ApiModelProperty(value = "创建时间  精确到时分秒")
    private String createTime;
    @ApiModelProperty(value = "统计时间 yyyy-mm")
    private String createTimeEx;
    @ApiModelProperty(value = "订单前缀")
    private String orderPrefix;
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "订单号")
    private Long orderNo;
    @ApiModelProperty(value = "财务code")
    private String financialCode;
    @ApiModelProperty(value = "审核人")
    private String auditUser;
    @ApiModelProperty(value = "审核时间")
    private String auditTime;
    @ApiModelProperty(value = "审核状态 0 失败 1成功 2待审核 ")
    private Integer status;
    @ApiModelProperty(value = "自身实发 实发总额")
    private BigDecimal rebateTotal;
    @ApiModelProperty(value = "审核备注")
    private String memo;
    @ApiModelProperty(value = "代理会员级别id")
    private Integer agyLevelId;
    @ApiModelProperty(value = "活动规则ID")
    private Integer ruleId;
    @ApiModelProperty(value = "活动ID")
    private Integer activityId;
    @ApiModelProperty(value = "帐变表mbr_bill_detail id")
    private Integer billDetailId;
    @ApiModelProperty(value = "mbrRebateAgentMonth表id")
    private Integer mbrRebateAgentMonthId;
}