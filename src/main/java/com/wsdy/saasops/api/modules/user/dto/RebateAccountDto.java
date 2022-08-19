package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "推荐用户",description = "推荐用户")
public class RebateAccountDto {

	@ApiModelProperty(value="会员id")
	private Integer accountId;

	@ApiModelProperty(value="会员名")
	private String loginName;

	@ApiModelProperty(value="注册时间")
	private String registerTime;

	@ApiModelProperty(value="登录时间")
	private String loginTime;

	@ApiModelProperty(value="返点数")
	private BigDecimal rebate;

	@ApiModelProperty(value="开始时间")
	private String startTime;

	@ApiModelProperty(value="结束时间")
	private String endTime;

	@ApiModelProperty
	private Integer depth;
}
