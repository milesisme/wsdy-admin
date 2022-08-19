package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "返回推广链接",description = "返回推广链接")
public class PromotionUrlDto {

	@ApiModelProperty(value="推广链接")
	private String promotionUrl;

	@ApiModelProperty(value="H5生成二维码链接")
	private String promotionH5Url;
}
