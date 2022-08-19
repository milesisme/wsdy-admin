package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "下级会员信息返利明细",description = "下级会员信息返利明细")
public class RebateAccSanGongDetailDto {
	@ApiModelProperty(value="下级会员id")
	private Integer id;
	@ApiModelProperty(value="下级会员名")
	private String loginName;
	@ApiModelProperty(value = "下级会员返利收益")
	private BigDecimal amount;
	@ApiModelProperty(value = "下级会员有效投注")
	private BigDecimal validbet;
	@ApiModelProperty(value = "下级会员该返利时的分配返利比例")
	private BigDecimal rebateRatio;
	@ApiModelProperty(value = "下级会员该返利时的实际返利比例")
	private BigDecimal rebateRatioActual;
	@ApiModelProperty(value = "发放时间==审核时间")
	private String auditTime;

	// 查询参数
	@ApiModelProperty(value = "分页页码")
	private Integer pageNo;
	@ApiModelProperty(value = "分页大小")
	private Integer pageSize;
	@ApiModelProperty(value= "下级会员姓名")
	private String subLoginName;
	@ApiModelProperty(value = "发放开始时间")
	private String startTime;
	@ApiModelProperty(value = "发放结束时间")
	private String endTime;
	@ApiModelProperty(value = "上级会员id")
	private Integer parentId;
}
