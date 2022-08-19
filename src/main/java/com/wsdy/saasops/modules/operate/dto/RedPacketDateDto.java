package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "RedPacketDateDto", description = "发放时间段")
public class RedPacketDateDto {

    @ApiModelProperty(value = "时间开始")
    private String startTime;

    @ApiModelProperty(value = "结束开始")
    private String endTime;
}
