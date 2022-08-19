package com.wsdy.saasops.aff.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "会员投注查询",description = "会员投注查询")
public class AccountBetRequestDto {

	@ApiModelProperty(value = "会员名")
	private String membercode;

	@ApiModelProperty(value = "注册开始时间")
	private String startTime;

	@ApiModelProperty(value = "registerEndTime")
	private String endTime;

	private String siteCode;
}
