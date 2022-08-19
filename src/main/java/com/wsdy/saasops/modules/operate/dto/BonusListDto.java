package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class BonusListDto {

    @ApiModelProperty(value = "优惠id")
    private Integer id;

    @ApiModelProperty(value = "0 拒绝 1已使用 2待处理 3 可使用 4已失效")
    private Integer status;

    @ApiModelProperty(value = "有效期")
    private String useEnd;

    @ApiModelProperty(value = "活动名称")
    private String activityName;

    @ApiModelProperty(value = "活动类型")
    private String tmplName;

    @ApiModelProperty(value = "奖励红利")
    private BigDecimal bonusAmount;

    @ApiModelProperty(value = "ruleId", hidden = true)
    private Integer ruleId;

    @ApiModelProperty(value = "tmplCode", hidden = true)
    private String tmplCode;

    @ApiModelProperty(value = "活动id", hidden = true)
    private Integer activityId;

    @ApiModelProperty(value = "适用范围")
    private List<BonusCatDto> catDtoList;

    @ApiModelProperty(value = "最少转账金额")
    private BigDecimal minAmount;

    @ApiModelProperty(value = "钱包余额")
    private BigDecimal walletBalance;

    @ApiModelProperty(value = "稽核点流水")
    private double discountAudit;

    @ApiModelProperty(value = "活动层级id")
    private Integer actLevelId;

}
