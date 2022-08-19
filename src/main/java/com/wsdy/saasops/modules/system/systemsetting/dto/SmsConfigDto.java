package com.wsdy.saasops.modules.system.systemsetting.dto;

import com.wsdy.saasops.modules.system.systemsetting.entity.SmsConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SmsConfigDto {
	@ApiModelProperty(value = "短信配置数组")
	List<SmsConfig> smsConfigs;
}
