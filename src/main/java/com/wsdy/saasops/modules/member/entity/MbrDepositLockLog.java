package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@Setter
@Getter
@ApiModel(value = "MbrDepositLockLog", description = "好友转账")
@Table(name = "mbr_deposit_lock_log")
public class MbrDepositLockLog implements Serializable{
private static final long serialVersionUID=1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "锁定类型 0自动锁定 1手动锁定")
    private Integer lockType;

    @ApiModelProperty(value = "锁定人")
    private String lockUser;

    @ApiModelProperty(value = "锁定时间")
    private String locktime;

    @ApiModelProperty(value = "锁定时长")
    private Integer lockMinute;

    @ApiModelProperty(value = "解锁时间")
    private String unlocktime;

    @ApiModelProperty(value = "解锁人")
    private String unlockuser;

    @ApiModelProperty(value = "锁定备注")
    private String lockmemo;

    @ApiModelProperty(value = "解锁备注")
    private String unlockmemo;

    @ApiModelProperty(value = "会员账号")
    @Transient
    private String loginName;
    @ApiModelProperty(value = "真实姓名")
    @Transient
    private String realName;
    @ApiModelProperty(value = "VIP等级")
    @Transient
    private String tierName;
    @ApiModelProperty(value = "活动层级ID 查询使用")
    @Transient
    private List<String> actLevelIdList;
    @ApiModelProperty(value = "存款锁定状态  0正常 1冻结")
    @Transient
    private Integer depositLock;
    @ApiModelProperty(value = "自动锁定次数")
    @Transient
    private Integer autoLock;
    @ApiModelProperty(value = "手动锁定次数")
    @Transient
    private Integer manualLock;
    @ApiModelProperty(value = "锁定开始时间")
    @Transient
    private String startTime;
    @ApiModelProperty(value = "锁定结束时间")
    @Transient
    private String endTime;
    @ApiModelProperty(value = "是否发布私信 0否 1是")
    @Transient
    private Integer send;
    @ApiModelProperty(value = "私信内容")
    @Transient
    private String message;
}