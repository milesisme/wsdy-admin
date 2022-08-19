package com.wsdy.saasops.modules.member.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@ApiModel(value = "MbrSysWarning", description = "玩家系统预警表")
@Table(name = "mbr_sys_warning")
public class MbrSysWarning {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "玩家ID")
    private Integer accountId;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @ApiModelProperty(value = "充值锁定，1手动，0自动")
    private Integer chargeLock;

    @ApiModelProperty(value = "创建时间")
    private String createTime;


}
