package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;


@Setter
@Getter
@ApiModel(value = "OprActRule", description = "OprActRule")
@Table(name = "opr_act_rule")
public class OprActRule implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "规则字符串")
    private String rule;
    @ApiModelProperty(value = "time")
    private String time;
    @ApiModelProperty(value = "规则名称")
    private String ruleName;
    @ApiModelProperty(value = "活动模板Id")
    private Integer actTmplId;
    @ApiModelProperty(value = "1开启，0禁用")
    private Integer available;
    @ApiModelProperty(value = "1审核，0否")
    private Integer isAudit;
    @ApiModelProperty(value = "创建人")
    private String createUser;
    @ApiModelProperty(value = "modifyUser")
    private String modifyUser;
    @ApiModelProperty(value = "modifyTime")
    private String modifyTime;
    @ApiModelProperty(value = "1删除，0未删除")
    private Integer isDelete;
    @ApiModelProperty(value = "自助洗码状态 1开启，0关闭")
    private Integer isSelfHelp;
    @ApiModelProperty(value = "自助洗码限制 1开启，0关闭")
    private Integer isLimit;
    @ApiModelProperty(value = "自助申请状态 1开启，0关闭")
    private Integer isSelfHelpShow;
    @ApiModelProperty(value = "限制金额")
    private BigDecimal minAmount;

    @Transient
    @ApiModelProperty(value = "活动分类名称")
    private String tmplName;
    @Transient
    @ApiModelProperty(value = "活动分类名称代码")
    private String tmplCode;
    @Transient
    @ApiModelProperty(value = "活动id")
    private Integer activityId;
    @Transient
    @ApiModelProperty(value = "代理id")
    private Integer agentId;
    
    @Transient
    @ApiModelProperty(value = "配置时是否可选:活动类型 ，已经有对应的活动不可选")
    private Boolean isSelect;
}