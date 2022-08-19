package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "下级会员信息",description = "下级会员信息")
public class RebateAccSanGongDto {
	@ApiModelProperty(value="下级会员id")
	private Integer id;
	@ApiModelProperty(value="下级会员名")
	private String loginName;
	@ApiModelProperty(value = "下级会员返利比例")
	private BigDecimal rebateRatio;
	@ApiModelProperty(value = "下级会员的下级会员数目")
	private Integer count;
	@ApiModelProperty(value = "下级会员最后登录时间")
	private String loginTime;
	@ApiModelProperty(value = "下级会员贡献上级总返利")
	private BigDecimal totalAmount;

	// 查询参数
	@ApiModelProperty(value = "分页页码")
	private Integer pageNo;
	@ApiModelProperty(value = "分页大小")
	private Integer pageSize;
	@ApiModelProperty(value= "父级会员id")
	private Integer parentAccId;
}
