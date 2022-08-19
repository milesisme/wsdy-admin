package com.wsdy.saasops.modules.lottery.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LotteryDepositDto {

    @ApiModelProperty(value = "isSelected 0未选中  1选中")
    private Integer isSelected;

    @ApiModelProperty(value = "0累计充值 1首次单笔充值 2有效投注")
    private Integer sign;

    @ApiModelProperty(value = "0累计充值 期间累计充值不少于  1期间首次单笔充值不少于  2期间有效投注不少于")
    private BigDecimal amountConditions;

    @ApiModelProperty(value = "赠送次数")
    private Integer num;
}
