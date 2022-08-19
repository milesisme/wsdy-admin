package com.wsdy.saasops.api.modules.user.dto.SdyActivity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "生日",description = "生日")
public class SdyBirthdayDto {

	@ApiModelProperty(value = "true 可以申请 false不可以")
	private Boolean isBirthday;

	@ApiModelProperty(value = "活动id")
	private Integer activityId;

	@ApiModelProperty(value = "赠送金额")
	private BigDecimal donateAmount;

	@ApiModelProperty(value = "流水倍数")
	private Double multipleWater;
}
