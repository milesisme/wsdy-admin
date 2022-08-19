package com.wsdy.saasops.api.modules.user.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;



/**
 * 用户
 */
@Setter
@Getter
@ApiModel(value = "会员真实姓名", description = "")
public class RealNameDto implements Serializable {
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "会员真实姓名!")
	private String realName;
}
