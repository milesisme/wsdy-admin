package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class TaskActiveDayDto {

  @ApiModelProperty(value = "第多少天")
  private Integer day;

  @ApiModelProperty(value = "奖励金额")
  private BigDecimal bonusAmount;

}