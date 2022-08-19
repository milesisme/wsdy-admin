package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "PZQueryResponseDto", description = "盘子支付代付查询返回参数")
public class PZQueryResponseDto {

    @ApiModelProperty(value = "查询状态， true 表示成功， false 表示失败")
    private Boolean success;

    @ApiModelProperty(value = "成功或错误信息")
    private String message;

    @ApiModelProperty(value = "查询结果实际内容，详见： 业务参数")
    private Object content;

}
