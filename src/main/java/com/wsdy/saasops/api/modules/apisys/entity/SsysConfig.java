package com.wsdy.saasops.api.modules.apisys.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.wsdy.saasops.api.modules.apisys.dto.ProxyProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "基础信息设置表", description = "")
public class SsysConfig {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;
	@ApiModelProperty(value = "组名")
	private String groups;
	@ApiModelProperty(value = "keys")
	private String keys;
	@ApiModelProperty(value = "值")
	private String values;
	@ApiModelProperty(value = "描述")
	private String description;
	@ApiModelProperty(value = "values 的Json字段")
	@Transient
	private ProxyProperty proxyProperty;
	
}
