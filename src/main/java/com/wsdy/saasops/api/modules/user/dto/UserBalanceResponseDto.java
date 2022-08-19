package com.wsdy.saasops.api.modules.user.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "会员平台余额DTO", description = "会员平台余额DTO")
public class UserBalanceResponseDto {

    @ApiModelProperty(value = "币别")
    private String Currency;

    @ApiModelProperty(value = "额度")
    private BigDecimal Balance;

    @ApiModelProperty(value = "平台id")
    private Integer depotId;
}
