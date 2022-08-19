package com.wsdy.saasops.modules.agent.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Setter
@Getter
@Table(name = "agy_account_other")
public class AgentAccountOther {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "代理id")
    private Integer agentId;

    @ApiModelProperty(value = "投注状态 1开启，0关闭")
    private Integer bettingStatus;

    @ApiModelProperty(value = "1开启，0关闭")
    private Integer vipStatus;

    @ApiModelProperty(value = "真人分成")
    private BigDecimal realpeople;

    @ApiModelProperty(value = "电子分成")
    private BigDecimal electronic;

    @ApiModelProperty(value = "真人洗码佣金比例")
    private BigDecimal realpeoplewash;

    @ApiModelProperty(value = "电子洗码佣金比例")
    private BigDecimal electronicwash;

}