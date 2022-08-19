package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TaskTimeDto {

  @ApiModelProperty(value = "领取时间 分钟")
  private Long time;

  @ApiModelProperty(value = "奖励金额")
  private BigDecimal bonusAmount;

  @ApiModelProperty(value = "流水倍数")
  private Integer multipleWater;

  @ApiModelProperty(value = "第一次开始时间或者上一次领取时间")
  private String receiveTime;
}