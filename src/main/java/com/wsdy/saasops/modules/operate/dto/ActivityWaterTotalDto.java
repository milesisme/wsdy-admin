package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@ApiModel(value = "ActivityWaterTotalDto", description = "返水统计视图")
public class ActivityWaterTotalDto {

    @ApiModelProperty(value = "活动id")
    private Integer activityId;

    @ApiModelProperty(value = "活动名称")
    private String activityName;

    @ApiModelProperty(value = "申请时间 yyyy-mm-dd,此处作为批次号使用")
    private String applicationTime;

    @ApiModelProperty(value = "批次活动应发放总额")
    private String totalBonus;

    @ApiModelProperty(value = "批次活动实发放总额")
    private String totalBonusAudit;

    @ApiModelProperty(value = "返水统计区间 start")
    private String waterStart;

    @ApiModelProperty(value = "返水统计区间 end")
    private String waterEnd;

    @ApiModelProperty(value = "发放统计-待审核")
    private Integer statisticsPending;

    @ApiModelProperty(value = "发放统计-通过")
    private Integer statisticsApproved;

    @ApiModelProperty(value = "发放统计-拒绝")
    private Integer statisticsRejection;

    @ApiModelProperty(value = "操作标记")
    private Integer flag;

    @ApiModelProperty(value = "waterdateid")
    private Integer id;
}
