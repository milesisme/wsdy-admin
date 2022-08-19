package com.wsdy.saasops.agapi.modules.dto;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DirectMemberParamDto {


    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;
    
    @ApiModelProperty(value = "代理id")
    private Integer agyId;

    @ApiModelProperty(value = "编号")
    private String numbering;

    @ApiModelProperty(value = "开始时间开始")
    private String startTime;

    @ApiModelProperty(value = "开始时间结束")
    private String endTime;

    @ApiModelProperty(value = "直属代理 direct")
    private Integer cagencyId;

    @ApiModelProperty(value = "分线代理id")
    private Integer subCagencyId;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "直属代理 direct")
    private String subAgyAccount;

    @ApiModelProperty(value = "转账金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "排序")
    private String orderBy;

    @ApiModelProperty(value = "直属代理 direct")
    private Integer subAgentId;
    
    @ApiModelProperty(value = "倒序排序：true,false")
    private Boolean desc;

    @ApiModelProperty(value = "分线id")
    private Integer subcagencyId;
    @ApiModelProperty(value = "分组属性")
    private Boolean groubyAgent = Boolean.FALSE;

    @ApiModelProperty(value = "佣金时间")
    private String time;
}
