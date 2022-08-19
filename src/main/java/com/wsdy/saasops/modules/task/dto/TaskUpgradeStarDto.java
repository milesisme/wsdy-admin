package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TaskUpgradeStarDto {

  @ApiModelProperty(value = "等级id")
  private Integer accountLevelId;

  @ApiModelProperty(value = "奖励金额")
  private BigDecimal amount;

  @ApiModelProperty(value = "星级")
  private Integer accountLevel;
}