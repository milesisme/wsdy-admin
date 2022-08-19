package com.wsdy.saasops.agapi.modulesV2.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class AgentV2ListDto {

    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "代理/会员id")
    private Integer accountId;
    @ApiModelProperty(value = "AGENT代理  ACCOUNT会员")
    private String userCode;
    @ApiModelProperty(value = "代理名/会员名")
    private String loginName;
    @ApiModelProperty(value = "别名")
    private String realName;
    @ApiModelProperty(value = "点数")
    private BigDecimal balance;
    @ApiModelProperty(value = "创建时间")
    private String createTime;
    @ApiModelProperty(value = "状态 1开启，0禁用")
    private Integer available;
    @ApiModelProperty(value = "投注状态 1开启，0关闭")
    private Integer bettingStatus;
    @ApiModelProperty(value = "真人分成")
    private BigDecimal realpeople;
    @ApiModelProperty(value = "电子分成")
    private BigDecimal electronic;
    @ApiModelProperty(value = "真人洗码佣金比例")
    private BigDecimal realpeoplewash;
    @ApiModelProperty(value = "电子洗码佣金比例")
    private BigDecimal electronicwash;
    @ApiModelProperty(value = "代理数")
    private Integer agentNum;
    @ApiModelProperty(value = "代理会员数")
    private Integer accountNum;

    @ApiModelProperty(value = "最后登录时间")
    private String loginTime;
    @ApiModelProperty(value = "代理类别： 0 公司(总代)/1股东/2总代/ >2 代理  -1会员  -2汇总")
    public Integer agentType;
}
