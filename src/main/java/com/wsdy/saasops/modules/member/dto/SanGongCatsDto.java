package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "SanGongCatsDto", description = "SanGongCatsDto")
public class SanGongCatsDto {

    @ApiModelProperty(value = "accountId")
    private Integer accountId;

    @ApiModelProperty(value = "rebateId")
    private Integer rebateId;

    @ApiModelProperty(value = "amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "isSelf")
    private Boolean isSelf;

    @ApiModelProperty(value = "上级")
    private Boolean isSuperior;
}
