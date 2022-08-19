package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class FriendRebateSummaryDto {
    @ApiModelProperty(value = "昨日收益")
    private BigDecimal ydActualReward;

    @ApiModelProperty(value = "昨日首充返利")
    private BigDecimal ydFirstChargeReward;

    @ApiModelProperty(value = "昨日有效投注返利")
    private BigDecimal ydValidBetReward;

    @ApiModelProperty(value = "累计收益")
    private BigDecimal sumActualReward;

    @ApiModelProperty(value = "累计首充返利")
    private BigDecimal sumFirstChargeReward;

    @ApiModelProperty(value = "累计有效投注")
    private BigDecimal sumValidBetReward;

    @ApiModelProperty(value = "邀请总量")
    private Integer num;
}
