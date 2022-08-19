package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsTeleSignStatusDto {
	@ApiModelProperty(value="状态码")
	private Integer code;
	@ApiModelProperty(value="状态描述")
	private String description;
	@ApiModelProperty(value="时间")
	private String updated_on;
}
