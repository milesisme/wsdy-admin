package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@ApiModel(value = "ActivityWaterTotalDto", description = "游戏类别返水统计视图")
public class ActivityWaterCatDto {

    @ApiModelProperty(value = "红利id")
    private Integer bonusId;

    @ApiModelProperty(value = "跑批时间 yyyy-mm-dd,此处作为批次号使用")
    private String time;

    @ApiModelProperty(value = "平台名称")
    private String depotName;

    @ApiModelProperty(value = "类别id")
    private Integer catId;

    @ApiModelProperty(value = "平台")
    private Integer depotId;

    @ApiModelProperty(value = "有效投注")
    private BigDecimal validbet;

    @ApiModelProperty(value = "返水金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "catName")
    private String catName;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "申请时间")
    private String applicationTime;

    @ApiModelProperty(value = "审核时间")
    private String auditTime;
}
