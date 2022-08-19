package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Table;
import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "MbrVerifyDeposit", description = "")
@Table(name = "verify_deposit")
public class VerifyDeposit {

	@ApiModelProperty(value = "Id")
	private Integer id;

	@ApiModelProperty(value = "会员id")
	private Integer accountId;

	private Integer mark;

	private String orderNo;

	private BigDecimal depositAmount;

	private String depositCreatetime;

	private String createtime;

	private String datasecret;

	private Integer depositId;

	private String siteCode;

}
