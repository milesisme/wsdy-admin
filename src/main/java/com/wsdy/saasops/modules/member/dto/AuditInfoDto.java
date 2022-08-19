package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "即时稽核信息")
public class AuditInfoDto {
    @ApiModelProperty(value = "总存款")
    private BigDecimal depositTotal;
    @ApiModelProperty(value = "存款所需流水")
    private BigDecimal depositValidBet;
    @ApiModelProperty(value = "存款当前流水")
    private BigDecimal currentDepositValidBet;
    @ApiModelProperty(value = "存款剩余流水")
    private BigDecimal depositResidueValidBet;
    @ApiModelProperty(value = "本金转入")
    private BigDecimal transferTotal;
    @ApiModelProperty(value = "赠送优惠额度")
    private BigDecimal bonusAmount;
    @ApiModelProperty(value = "优惠剩余流水")
    private BigDecimal bonusResidueValidBet;
    @ApiModelProperty(value = "转帐所需流水")
    private BigDecimal transferValidBet;
    @ApiModelProperty(value = "存款是否通过  true通过  false没有通过")
    private Boolean depositSucceed = Boolean.TRUE;
    @ApiModelProperty(value = "优惠是否通过 true通过  false没有通过")
    private Boolean dbounsSucceed = Boolean.TRUE;
    @ApiModelProperty(value = "转帐当前流水")
    private BigDecimal currentTransferValidBet;

    @ApiModelProperty(value = "稽核明细列表")
    private List<AuditDetailDto> auditDetailDtos;
}
