package com.wsdy.saasops.modules.operate.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "JDepositSentDto", description = "投就送规则明细")
public class BettingGiftRuleDto {

    @ApiModelProperty(value = "最小投注额")
    private BigDecimal validBetMin;

    @ApiModelProperty(value = "最大投注额")
    private BigDecimal validBetMax;

    @ApiModelProperty(value = "赠送类型 0按比例 1按金额")
    private Integer donateType;

    @ApiModelProperty(value = "赠送金额")
    private BigDecimal donateAmount;

    @ApiModelProperty(value = "存款金额限制 0不限制 1限制最低要求")
    private Integer depositAmountType;

    @ApiModelProperty(value = "最小存款金额")
    private BigDecimal depositMin;

    @ApiModelProperty(value = "最大存款金额")
    private BigDecimal depositMax;

    @ApiModelProperty(value = "流水倍数")
    private Double multipleWater;

    @JsonIgnore
    @ApiModelProperty(value = "有效投注")
    private BigDecimal validBet;
    @JsonIgnore
    @ApiModelProperty(value = "存款金额")
    private BigDecimal depositAmount;


}
