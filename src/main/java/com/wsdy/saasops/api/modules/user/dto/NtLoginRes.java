package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "登陆返回信息")
public class NtLoginRes {
	@ApiModelProperty(value = "返回状态")
	private String status;
	@ApiModelProperty(value = "登陆序列号")
	private Integer partyId;
	@ApiModelProperty(value = "登陆序SEESON ID")
	private String sessionKey;
}
