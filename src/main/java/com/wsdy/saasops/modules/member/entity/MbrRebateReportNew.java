package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel(value = "MbrRebateReportNew", description = "好友返利新类")
@Table(name = "mbr_rebate_report_new")
public class MbrRebateReportNew implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "时间")
    private String reportTime;

    @ApiModelProperty(value = "会员帐号")
    private String loginName;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "分类id")
    private Integer catId;;

    @ApiModelProperty(value = "下级返点金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "有效投注")
    private BigDecimal validbet;

    @ApiModelProperty(value = "下级会员id")
    private Integer subAccountId;

    @ApiModelProperty(value = "下级会员名")
    private String subLoginName;

    @ApiModelProperty(value = "下级会员层级")
    private Integer depth;

    @ApiModelProperty(value = "")
    private Integer auditId;

    @Transient
    private String startTime;

    @Transient
    private String endTime;

    @Transient
    private Integer pageNo;

    @Transient
    private Integer pageSize;

    @Transient
    private String order;

    @Transient
    private Integer lowDepth;

    @Transient
    private Integer highDepth;

    @ApiModelProperty(value = "分配比例")
    private BigDecimal rebateRatio;
    @ApiModelProperty(value = "返利比例")
    private BigDecimal rebateRatioActual;
}