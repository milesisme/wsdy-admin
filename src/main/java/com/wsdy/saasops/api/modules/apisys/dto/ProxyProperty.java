package com.wsdy.saasops.api.modules.apisys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "代理属性设置")
public class ProxyProperty {
	@ApiModelProperty(value = "代理方式")
	private String proxyType;
	@ApiModelProperty(value = "代理ip")
	private String ip;
	@ApiModelProperty(value = "代理端口")
	private Integer port;
	@ApiModelProperty(value = "代理账号")
	private String user;
	@ApiModelProperty(value = "代理密码")
	private String password;
}
