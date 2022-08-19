package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


@Getter
@Setter
public class TaskActiveFilesDto {

  @ApiModelProperty(value = "第几档")
  private Integer num;

  @ApiModelProperty(value = "领取要求 当日存款不小于")
  private BigDecimal depositAmount;

  @ApiModelProperty(value = "当日投注不小于")
  private BigDecimal validBet;

  @ApiModelProperty(value = " 奖励规则")
  private List<TaskActiveDayDto> dayDtoList;

}