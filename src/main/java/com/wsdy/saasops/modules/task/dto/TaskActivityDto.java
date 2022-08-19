package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskActivityDto {

  @ApiModelProperty(value = "活动id")
  private Integer activityId;

  @ApiModelProperty(value = "分类ID")
  private Integer catId;

  @ApiModelProperty(value = "活动名称")
  private String activityName;

  @ApiModelProperty(value = "排序")
  private Integer sort;

  @ApiModelProperty(value = "图片地址")
  private String mblogourl;
}