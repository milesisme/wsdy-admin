package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;

@Setter
@Getter
@ApiModel(value = "MbrUseDevice", description = "")
@Table(name = "mbr_use_device")
public class MbrUseDevice implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "会员名")
	private String loginName;

	@ApiModelProperty(value = "设备uuid")
	private String deviceUuid;

	@ApiModelProperty(value = "到期时间")
	private String exptime;


	@ApiModelProperty(value = "验证次数")
	private Integer valiTimes;


}