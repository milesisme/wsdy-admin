package com.wsdy.saasops.modules.fund.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "QuickFunction", description = "快捷功能")
public class QuickFunction {
    @ApiModelProperty(value = "统计条数")
    private Integer counts;

    @ApiModelProperty(value = "快捷功能名称")
    private String quickName;

    @ApiModelProperty(value = "数据id")
    private String ids;

    @ApiModelProperty(value = "是否开启：true，开启；false，关闭")
    private boolean isOpen = true;

}
