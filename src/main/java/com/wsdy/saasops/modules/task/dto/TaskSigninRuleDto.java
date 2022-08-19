package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskSigninRuleDto {

    @ApiModelProperty(value = "流水倍数")
    private Integer multipleWater;

    @ApiModelProperty(value = "天数")
    private List<TaskSigninListDto> signinDtos;

}