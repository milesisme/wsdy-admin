package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel(value = "MbrRebateReport", description = "返利修改到活动范畴后，该类只做数据封装（便于与前台兼容以前的字段），不做数据库映射")
@Table(name = "mbr_rebate_report")
public class MbrRebateReport implements Serializable {

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
    private Integer catId;

    @ApiModelProperty(value = "本人返点")
    private BigDecimal rebateAmount;

    @ApiModelProperty(value = "贡献上级(好友)佣金")
    private BigDecimal contributeAmount;

    @ApiModelProperty(value = "下级返点金额")
    private BigDecimal subordinateAmount;

    @ApiModelProperty(value = "有效投注")
    private BigDecimal validbet;

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
}