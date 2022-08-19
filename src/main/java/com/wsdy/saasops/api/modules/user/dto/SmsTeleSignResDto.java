package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsTeleSignResDto {
	@ApiModelProperty(value="平台id")
	private String reference_id;
	@ApiModelProperty(value="拓展ID")
	private String external_id;

	@ApiModelProperty(value="status对象")
	private SmsTeleSignStatusDto status;
}
