package com.wsdy.saasops.aff.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDepositRequestDto {

    @ApiModelProperty(value = "会员名")
    private String membercode;

    @ApiModelProperty(value = "注册开始时间")
    private String startTime;

    @ApiModelProperty(value = "registerEndTime")
    private String endTime;

    private String siteCode;
}
