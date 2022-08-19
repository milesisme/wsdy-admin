package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskUpgradeDto {

  @ApiModelProperty(value = "流水倍数")
  private Integer multipleWater;

  @ApiModelProperty(value = "奖励规则 星级等级")
  private List<TaskUpgradeStarDto> starDtos;

  @ApiModelProperty(value = "奖励次数")
  private Integer num;
}