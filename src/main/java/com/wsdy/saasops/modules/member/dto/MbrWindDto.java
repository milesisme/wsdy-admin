package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class MbrWindDto {
    @ApiModelProperty(value = "会员id")
    private String accountId;
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "代理")
    private String agyAccount;
    @ApiModelProperty(value = "IP地址")
    private String loginIp;
    @ApiModelProperty(value = "设备uudi uuid")
    private String deviceUuid;
    @ApiModelProperty(value = "设备类型：android,ios,H5,PC")
    private String deviceType;
    @ApiModelProperty(value = "领取优惠数")
    private Integer totalBonusAmountNum;
    @ApiModelProperty(value = "活动名称")
    private String activityName;
    @ApiModelProperty(value = " 活动类别")
    private String catName;
    @ApiModelProperty(value = "登录时间")
    private Date loginTime;
    @ApiModelProperty(value = "领取时间")
    private Date appTime;
    @ApiModelProperty(value = "领取优惠金额")
    private BigDecimal totalBonusAmount;
}
