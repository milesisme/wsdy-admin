package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Setter
@Getter
@Table(name = "mbr_account_other")
public class MbrAccountOther {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员账号")
    private String loginName;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "真人洗码佣金比例")
    private BigDecimal realpeoplewash;

    @ApiModelProperty(value = "电子洗码佣金比例")
    private BigDecimal electronicwash;

    @ApiModelProperty(value = "投注状态 1开启，0关闭")
    private Integer bettingStatus;

    @ApiModelProperty(value = "投注比")
    private BigDecimal betPoint;
}