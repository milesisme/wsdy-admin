package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "RedPacketDateDto", description = "红包雨活动规则dto")
public class RedPacketRainDto {

    @ApiModelProperty(value = "当日开始时间")
    private String startTime;
    @ApiModelProperty(value = "当日结束时间")
    private String endTime;
    @ApiModelProperty(value = "活动日期 按周1-7 ,如1,5,7  周一周五周日 ")
    private List<Integer> validDates;
    @ApiModelProperty(value = "红包随机金额")
    private List<BigDecimal> randomAmount;
    @ApiModelProperty(value = "奖励红利")
    private BigDecimal bonusAmount;
    @ApiModelProperty(value = "流水倍数")
    private Double multipleWater;

    @ApiModelProperty(value = "红包雨次数档位规则")
    private List<RedPacketRainRuleDto> redPacketRainRuleDtos;
}
