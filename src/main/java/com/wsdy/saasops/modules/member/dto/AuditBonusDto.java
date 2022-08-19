package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "转账优惠稽核违规or不通过")
public class AuditBonusDto {

    @ApiModelProperty(value = "是否违规 true没有违规 false违规")
    private Boolean isFraud = Boolean.TRUE;

    @ApiModelProperty(value = "是否通过 true是 false没有通过")
    private Boolean isSucceed = Boolean.TRUE;

    @ApiModelProperty(value = "有效投注总流水")
    private BigDecimal totalValidBet = BigDecimal.ZERO;

    @ApiModelProperty(value = "剩余有效投注流水")
    private BigDecimal residueValidBet = BigDecimal.ZERO;

}
