package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "CountEntity", description = "统计功能返回实体")
public class CountEntity {
    @ApiModelProperty(value = "统计条数")
    private String counts;

//    @ApiModelProperty(value = "功能名称")
//    private String countName;

    @ApiModelProperty(value = "统计条件")
    private String conditions;
}
