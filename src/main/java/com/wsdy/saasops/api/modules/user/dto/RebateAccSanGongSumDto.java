package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "下级会员返利收益总览数据",description = "下级会员返利收益总览数据")
public class RebateAccSanGongSumDto {
	@ApiModelProperty(value = "上级会员id")
	private Integer parentId;
	@ApiModelProperty(value = "上级会员loginName")
	private String parentLoginName;

	@ApiModelProperty(value = "上级会员返利比例")
	private BigDecimal rebateRatio;
	@ApiModelProperty(value = "上级会员最后登录时间")
	private String loginTime;
	@ApiModelProperty(value = "上级会员注册时间")
	private String registerTime;
	@ApiModelProperty(value = "上级会员贡献上上级会员的累计返利")
	private BigDecimal totalAmountForParent;

	@ApiModelProperty(value = "下级会员数量合计")
	private Integer count;
	@ApiModelProperty(value = "下级会员总有效投注")
	private BigDecimal totalValidbet;
	@ApiModelProperty(value = "下级会员总返利收益")
	private BigDecimal totalAmount;

	// 查询条件
	@ApiModelProperty(value = "发放开始时间")
	private String startTime;
	@ApiModelProperty(value = "发放结束时间")
	private String endTime;
}
