package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CalcRebateUpgradeVipDto {
    @ApiModelProperty(value = "子账号ID")
    private Integer  subAccountId;

    @ApiModelProperty(value = "子账号")
    private String subLoginName;

    @ApiModelProperty(value = "账号ID")
    private Integer accountId;

    @ApiModelProperty(value = "登录名")
    private String loginName;

    @ApiModelProperty(value = "活动等级ID")
    private Integer actLevelId;

    @ApiModelProperty(value = "VIP升级信息")
    private String vipUpgradeInfo;

    @ApiModelProperty(value = "模块名称")
    private String  modulename;
}
