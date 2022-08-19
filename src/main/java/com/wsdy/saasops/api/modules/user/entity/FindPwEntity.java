package com.wsdy.saasops.api.modules.user.entity;

import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name="mbr_retrvpw")
public class FindPwEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String loginName;//账号
	private String vaildCode;//验证码
	private Long expire;//失效时间
	private Integer vaildTimes;//验证次数
	private String applyTime;//申请时间
	private Byte vaildType;//验证方式

	@Transient
	@ApiModelProperty(value="手机区号：86中国，886台湾")
	private String mobileAreaCode;
	
	@ApiModelProperty(value="账户类型 1:会员 2:代理")
	private Integer accountType;
}
