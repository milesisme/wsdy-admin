package com.wsdy.saasops.api.modules.user.dto.SdyActivity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ActivityLevelDto {

	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "等级")
	private Integer accountLevel;

	@ApiModelProperty(value = "层级名称")
	private String tierName;

/*	@ApiModelProperty(value = "每日充许取款次数")
	private Integer withDrawalTimes;

	@ApiModelProperty(value = "每日取款限额")
	private BigDecimal withDrawalQuota;*/

	@ApiModelProperty(value = "0 累计投注 1 累计充值 2全部")
	private Integer promoteSign;

	@ApiModelProperty(value = "累计投注最小 大于")
	private BigDecimal validbetMin;

	@ApiModelProperty(value = "存款最小 大于")
	private BigDecimal depositMin;

	@ApiModelProperty(value = "降至上一等级条件期间累计投注小于")
	private BigDecimal downgradeBet;

	// 每日取款限额
	@ApiModelProperty(value = "是否取款限制：1 限制 0 不限制")
	private Byte feeAvailable;
	@ApiModelProperty(value = "每日充许取款次数")
	private Integer withDrawalTimes;
	@ApiModelProperty(value = "每日取款限额")
	private BigDecimal withDrawalQuota;

}
