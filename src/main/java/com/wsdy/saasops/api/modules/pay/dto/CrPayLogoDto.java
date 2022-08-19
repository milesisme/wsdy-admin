package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "CrPayLogoDto", description = "钱包/交易所log")
public class CrPayLogoDto {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "模板Id, 1-12值")
    private Integer evebNum;
    @ApiModelProperty(value = "钱包/交易所名字")
    private String name;
    @ApiModelProperty(value = "展示客户端（0：pc，1：移动，2：移动pc均显示）")
    private Integer clientShow;
    @ApiModelProperty(value = "广告路径")
    private String picpcpath;
    @ApiModelProperty(value = "移动广告路径")
    private String picMbPath;
    @ApiModelProperty(value = "排序id")
    private Integer orderId;

}
