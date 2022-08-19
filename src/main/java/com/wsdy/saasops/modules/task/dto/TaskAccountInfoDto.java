package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TaskAccountInfoDto {

  @ApiModelProperty(value = "false否 true是")
  private Boolean isName;

  @ApiModelProperty(value = "false否 true是")
  private Boolean isBank;

  @ApiModelProperty(value = "false否 true是")
  private Boolean isMobile;

  @ApiModelProperty(value = "false否 true是 是否领取完善资料红利")
  private Boolean isBonus;

  @ApiModelProperty(value = "流水倍数")
  private Integer multipleWater;

  @ApiModelProperty(value = "奖励金额")
  private BigDecimal bonusAmount;

  @ApiModelProperty(value = "存款需求")
  private BigDecimal minAmount;

  @ApiModelProperty(value = "false否 true是 是否满足存款需求")
  private Boolean isMinAmount;
}