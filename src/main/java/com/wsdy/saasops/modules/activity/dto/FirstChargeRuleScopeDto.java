package com.wsdy.saasops.modules.activity.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "活动规则 跟范围", description = "首存送-返上级")
public class FirstChargeRuleScopeDto {


    @ApiModelProperty(value = "层级id")
    private Integer actLevelId;

    @ApiModelProperty(value = "已填写真实姓名 true是 false 否")
    private Boolean isName;

    @ApiModelProperty(value = "已绑定银行卡 true是 false 否")
    private Boolean isBank;

    @ApiModelProperty(value = "已验证手机 true是 false 否")
    private Boolean isMobile;

    @ApiModelProperty(value = "已验证邮箱 true是 false 否")
    private Boolean isMail;

    @ApiModelProperty(value = "App注册 true是 false 否")
    private Boolean isApp;

    @ApiModelProperty(value = "申请金额 大于")
    private BigDecimal appliedAmount;

    @ApiModelProperty(value = "首存类型 0常规 1每日首存 2每周首存 3限时")
    private Integer depositType;

    @ApiModelProperty(value = "首存有效期（天）当且仅当depositType = 0 时有值 ")
    private Integer validDate;

    @ApiModelProperty(value ="首存天内可领取，过期不可领取")
    private Integer day;

    @ApiModelProperty(value = "活动规则")
    private List<FirstChargeActivityRuleDto> activityRuleDtos;

    @ApiModelProperty(value = "等级前端页面使用")
    private Integer accountLevel;
    @ApiModelProperty(value = "等级前端页面使用")
    private String tierName;
}
