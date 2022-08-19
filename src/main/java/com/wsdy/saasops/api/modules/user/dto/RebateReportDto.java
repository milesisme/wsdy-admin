package com.wsdy.saasops.api.modules.user.dto;

import com.wsdy.saasops.modules.member.entity.MbrRebateReport;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(value = "返点统计",description = "返点统计")
public class RebateReportDto {

	@ApiModelProperty(value="活动ID号")
	private String reportTime;

	private List<MbrRebateReport> reportList;
}
