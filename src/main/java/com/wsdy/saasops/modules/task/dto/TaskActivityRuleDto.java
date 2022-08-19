package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskActivityRuleDto {

  @ApiModelProperty(value = "活动ID")
  private Integer activityId;

  @ApiModelProperty(value = "排序")
  private Integer sort;
}