package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "会员存取款与优惠每日统计查询", description = "")
public class MbrFundsReportDto implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "会员id")
    private Integer accountId;
    @ApiModelProperty(value = "会员账号")
    private String loginName;
    @ApiModelProperty(value = "报表日期")
    private String reportDate;
    @ApiModelProperty(value = "存款金额")
    private BigDecimal deposit;
    @ApiModelProperty(value = "取款金额")
    private BigDecimal withdraw;
    @ApiModelProperty(value = "优惠金额")
    private Integer bonus;

}
