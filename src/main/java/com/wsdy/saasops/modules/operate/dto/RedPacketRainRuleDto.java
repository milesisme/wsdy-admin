package com.wsdy.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "RedPacketRainRuleDto", description = "红包雨次数档位规则dto")
public class RedPacketRainRuleDto {

    @ApiModelProperty(value = "该档的最低当日存款 >=关系")
    private BigDecimal minAmount;
    @ApiModelProperty(value = "该档奖励的红包次数")
    private Integer number;
    @ApiModelProperty(value = "是否需要最近3天连续登陆 true是 false否")
    private Boolean isAward;

    @ApiModelProperty(value = "该档条件是否满足 true满足 false不满足")
    private Boolean isValid;
}
