package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "JContentDto", description = "活动内容")
public class JContentDto {

    @ApiModelProperty(value = "活动范围")
    private ActivityScopeDto scopeDto;

}
