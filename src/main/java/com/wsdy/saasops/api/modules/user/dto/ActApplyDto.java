package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "活动申请DTO",description = "活动申请DTO")
public class ActApplyDto {

	@ApiModelProperty(value="活动ID号")
	private int activityId;

	@ApiModelProperty(value="是否现在领取")
	private int isPickNow;

	@ApiModelProperty(value="分类id")
	private Integer catId;

	@ApiModelProperty(value = "混合活动子规则code 存就送AQ0000003 投就送AQ0000012 救援金AQ0000004 其他AQ0000015")
	private String subRuleTmplCode;
}
