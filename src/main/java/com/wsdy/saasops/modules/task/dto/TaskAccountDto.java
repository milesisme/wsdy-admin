package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TaskAccountDto {

  @ApiModelProperty(value = "完善项目：是否真实姓名  false否 true是")
  private Boolean isName;

  @ApiModelProperty(value = "完善项目：已绑定银行卡  false否 true是")
  private Boolean isBank;

  @ApiModelProperty(value = "完善项目：已验证手机  false否 true是")
  private Boolean isMobile;

  @ApiModelProperty(value = "奖励金额")
  private BigDecimal bonusAmount;

  @ApiModelProperty(value = "流水倍数")
  private Integer multipleWater;

  @ApiModelProperty(value = "存款需求")
  private BigDecimal minAmount;
}