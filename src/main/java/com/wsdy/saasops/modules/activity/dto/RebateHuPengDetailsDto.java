package com.wsdy.saasops.modules.activity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RebateHuPengDetailsDto {

    @ApiModelProperty(value = "统计时间")
    private String createTime;

    @ApiModelProperty(value = "发放时间")
    private String giveOutTime;

    @ApiModelProperty(value = "发放金额")
    private BigDecimal reward;

    @ApiModelProperty(value = "状态")
    private Integer status = 1;  // 1成功 0 失败
}
