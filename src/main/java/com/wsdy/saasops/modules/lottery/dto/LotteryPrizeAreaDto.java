package com.wsdy.saasops.modules.lottery.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LotteryPrizeAreaDto {

    @ApiModelProperty(value = "奖品类型 1谢谢参与,2彩金,3实物奖品")
    private Integer prizeType;

    @ApiModelProperty(value = "奖品名称")
    private String prizeName;

    @ApiModelProperty(value = "彩金金额")
    private BigDecimal donateAmount;

    @ApiModelProperty(value = "流水倍数")
    private Double multipleWater;

    @ApiModelProperty(value = "中奖概率")
    private Integer probability;
}
