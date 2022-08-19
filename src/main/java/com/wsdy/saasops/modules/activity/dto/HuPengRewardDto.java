package com.wsdy.saasops.modules.activity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Setter
@Getter
public class HuPengRewardDto {
    @ApiModelProperty(value = "好友账号")
    private String subLoginName;

    @ApiModelProperty(value = "好友账号ID")
    private Integer subAccountId;

    @ApiModelProperty(value = "父级账号")
    private String loginName;

    @ApiModelProperty(value = "注单数量")
    private Integer betNum;

    @ApiModelProperty(value = "中奖金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "比率")
    private BigDecimal rate;

    @ApiModelProperty(value = "奖励")
    private BigDecimal reward;

    @ApiModelProperty(value = "存款")
    private BigDecimal deposit = BigDecimal.ZERO ;

    @ApiModelProperty(value = "提款")
    private BigDecimal withdrawal = BigDecimal.ZERO;

    @ApiModelProperty(value = "优惠")
    private BigDecimal discount = BigDecimal.ZERO;

    @ApiModelProperty(value = "资金调整")
    private BigDecimal fundAdjust = BigDecimal.ZERO;


}
