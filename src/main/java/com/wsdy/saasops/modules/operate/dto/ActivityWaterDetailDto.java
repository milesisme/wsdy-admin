package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@ApiModel(value = "ActivityWaterTotalDto", description = "返水统计审计视图")
public class ActivityWaterDetailDto {
    @ApiModelProperty(value = "返水审核表id")
    private Integer id;

    @ApiModelProperty(value = "活动id")
    private Integer activityId;

    @ApiModelProperty(value = "申请时间 yyyy-mm-dd,此处作为批次号使用")
    private String applicationTime;

    @ApiModelProperty(value = "会员id")
    private String accountId;

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "直属代理id")
    private Integer cagentcyId;

    @ApiModelProperty(value = "直属代理名称")
    private String agyAccount;

    @ApiModelProperty(value = "总有效投注")
    private String totalValidbet;

    @ApiModelProperty(value = "总返水金额")
    private String totalAmount;

    @ApiModelProperty(value = "状态 0 拒绝 1通过 2待审核")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "审核人")
    private String auditUser;

    @ApiModelProperty(value = "审核时间")
    private String auditTime;

    @ApiModelProperty(value = "活动名称")
    private String activityName;

    @ApiModelProperty(value = "活动分类编号")
    private String tmplCode;

    @ApiModelProperty(value = "活动类型")
    private String tmplName;

    @ApiModelProperty(value = "会员等级")
    private String tierName;

    @ApiModelProperty(value = "游戏类别返水统计数组")
    private List<ActivityWaterCatDto> statisticsByCat;

    @ApiModelProperty(value = "游戏类别返水平台统计数组")
    private List<ActivityWaterCatDto> statisticsByDepot;
}
