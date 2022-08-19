package com.wsdy.saasops.modules.system.systemsetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SmsResultDto {
	@ApiModelProperty(value = "是否发送成功 true 成功 false 失败")
	private boolean success;

	@ApiModelProperty(value = "失败原因")
	private String message;
}
