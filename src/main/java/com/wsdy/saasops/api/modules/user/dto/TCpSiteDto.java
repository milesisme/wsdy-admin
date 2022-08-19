package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "站点信息", description = "站点信息")
public class TCpSiteDto{

	@ApiModelProperty(value = "站点代缩写代码")
	private String siteCode;

	@ApiModelProperty(value = "站点名称")
	private String siteName;

	@ApiModelProperty(value = "开始时间")
	private String startDate;

	@ApiModelProperty(value = "结束时间")
	private String endDate;

	@ApiModelProperty(value="备注")
	private String memo;
	
	@ApiModelProperty(value="站点URL")
	private String siteUrl;
	
	@ApiModelProperty(value="商户账号")
	private String companyUser;
}