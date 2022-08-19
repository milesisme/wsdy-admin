package com.wsdy.saasops.aff.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountDepositResponseDto {

    @ApiModelProperty(value = "会员名")
    private String membercode;

    @ApiModelProperty(value = "收到日期")
    private String receiveddate;

    private String type;

    private BigDecimal amount;

    private Integer id;

    @ApiModelProperty(value = "0 失败 1 成功 2待处理")
    private Integer status;

    @ApiModelProperty(value = "修改日期")
    private String updatedDate;
}
