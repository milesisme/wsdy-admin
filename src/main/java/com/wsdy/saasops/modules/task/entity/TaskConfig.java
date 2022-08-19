package com.wsdy.saasops.modules.task.entity;

import com.wsdy.saasops.modules.task.dto.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "TaskConfig", description = "")
@Table(name = "task_config")
public class TaskConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "财务类别代码 QD 签到，XS 限时活动，TJ 好友推荐 SJ 升级  ZL完善资料")
    private String financialCode;

    @ApiModelProperty(value = "modifyUser")
    private String modifyUser;

    @ApiModelProperty(value = "modifyTime")
    private String modifyTime;

    @ApiModelProperty(value = "1开启，0禁用")
    private Integer available;

    @ApiModelProperty(value = "规则字符串")
    private String rule;

    @ApiModelProperty(value = "开启时间")
    private String openingtime;

    @Transient
    @ApiModelProperty(value = "code 名称")
    private String financialCodeName;

    @Transient
    @ApiModelProperty(value = "签到是否黑名单 1是 0否")
    private Integer isBlacklist;

    @Transient
    @ApiModelProperty(value = "限时活动")
    private List<TaskActivityDto> activityDtos;

    @Transient
    @ApiModelProperty(value = "签到最后一次时间")
    private String qdTime;

    @Transient
    @ApiModelProperty(value = "签到连续签到次数")
    private Integer qdNumber;

    @Transient
    @ApiModelProperty(value = "升级奖励")
    private UpgradeAwardsDto upgradeAwards;

    @Transient
    @ApiModelProperty(value = "完善资料")
    private TaskAccountInfoDto accountInfoDto;

    @Transient
    @ApiModelProperty(value = "定时器")
    private TaskTimeDto taskTimeDto;

    @Transient
    @ApiModelProperty(value = "活跃奖励")
    private TaskActiveRewardDto rewardDto;
}