package com.wsdy.saasops.modules.log.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "LogSystem", description = "")
@Table(name = "log_system")
public class LogSystem implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "用户ID")
	private Integer userId;

	@ApiModelProperty(value = "模块名")
	private String module;

	@ApiModelProperty(value = "成功失败")
	private Byte flag;

	@ApiModelProperty(value = "备注")
	private String remark;

	@ApiModelProperty(value = "")
	private String createTime;
}