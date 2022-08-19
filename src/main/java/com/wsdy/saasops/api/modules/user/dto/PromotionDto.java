package com.wsdy.saasops.api.modules.user.dto;

import com.wsdy.saasops.modules.member.entity.MbrRebate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "好友推广", description = "好友推广")
public class PromotionDto {

    @ApiModelProperty(value = "已经推荐好友数")
    private Integer count;

    @ApiModelProperty(value = "返点说明")
    private MbrRebate rebate;

    @ApiModelProperty(value = "昨日返点")
    private BigDecimal yestodayRebates;

    @ApiModelProperty(value = "累计收益")
    private BigDecimal totalRebates;

    @ApiModelProperty(value = "当月好友总输赢")
    private BigDecimal totalResult;

    @ApiModelProperty(value = "是否可以点击生成链接 0否 1是")
    private Integer isClick;
}
