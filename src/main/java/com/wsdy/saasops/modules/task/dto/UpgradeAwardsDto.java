package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpgradeAwardsDto {

  @ApiModelProperty(value = "领取的历史等级")
  private List<Integer> drawAccountLevels;

  @ApiModelProperty(value = "会员当前等级")
  private Integer accountLevel;
}