package com.wsdy.saasops.modules.operate.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AppDownloadGiftDto", description = "App下载彩金")
public class AppDownloadGiftDto {

	@ApiModelProperty(value = "流水范围")
	private List<AuditCat> auditCats;
	
	@ApiModelProperty(value = "已填写真实姓名 true是 false 否")
	private Boolean isName;
	
	@ApiModelProperty(value = "已绑定银行卡 true是 false 否")
	private Boolean isBank;
	
	@ApiModelProperty(value = "已验证手机 true是 false 否")
	private Boolean isMobile;

	@ApiModelProperty(value = "会员范围 0全部会员 1层级会员")
	private Integer scope;
	
	@ApiModelProperty(value = "层级 活动规则")
	private List<MemDayRuleScopeDto> ruleScopeDtos;

}
