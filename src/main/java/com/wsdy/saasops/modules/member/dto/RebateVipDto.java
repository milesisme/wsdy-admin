package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
    public class RebateVipDto {

    @ApiModelProperty(value = "VIP等级")
    private  Integer level ;

    @ApiModelProperty(value = "VIP等级奖励")
    private BigDecimal award;

}
