package com.wsdy.saasops.api.modules.user.dto.SdyActivity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ActivityLevelCatDto {

	@ApiModelProperty(value = "层级id")
	private Integer accountLevelId;

	@ApiModelProperty(value = "层级名称")
	private String tierName;

	@ApiModelProperty(value = "分类名称")
	private String catName;

	@ApiModelProperty(value = "返水比例")
	private BigDecimal donateRatio;
}
