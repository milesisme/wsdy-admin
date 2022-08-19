package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Table;

@Getter
@Setter
@ApiModel(value = "VerifyWarn", description = "")
@Table(name = "verify_warn")
public class VerifyWarn {

	@ApiModelProperty(value = "Id")
	private Integer id;

	private String createtime;

	private Integer fromaccountid;

	private Integer toaccountid;

	private Integer verifydepositid;

	private Integer depositid;

	private String siteCode;
}
