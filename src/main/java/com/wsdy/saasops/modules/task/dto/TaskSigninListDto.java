package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TaskSigninListDto {

  @ApiModelProperty(value = "天数")
  private int day;

  @ApiModelProperty(value = "奖励金额")
  private BigDecimal amount;

  @ApiModelProperty(value = "是否大礼包 1是 0否")
  private Integer oen;
}