package com.wsdy.saasops.modules.task.dto;

import com.wsdy.saasops.modules.task.entity.TaskConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class TaskCenterDto {

  @ApiModelProperty(value = "任务中心总的今日收益")
  private BigDecimal dayAmount;

  @ApiModelProperty(value = "任务中心总的累计收益")
  private BigDecimal sumAmount;

  @ApiModelProperty(value = "日常任务")
  private List<TaskConfig> taskConfigs;

}