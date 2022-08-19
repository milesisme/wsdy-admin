package com.wsdy.saasops.modules.system.systemsetting.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@Table(name = "sms_config")
public class SmsConfig implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Long id;
	@ApiModelProperty(value = "平台ID 1梦网云  2Telesign  3启瑞云  4互亿无线")
	private Integer platformId;
	@ApiModelProperty(value = "平台名称")
	private String platformName;
	@ApiModelProperty(value = "是否开启 1开启 0关闭")
	private Integer available;
	@ApiModelProperty(value = "排序号")
	private Integer sort;
	@ApiModelProperty(value = "短信网关地址")
	private String 	getwayAddress;
	@ApiModelProperty(value = "短信用户接口名")
	private String interfaceName;
	@ApiModelProperty(value = "短信接口密码")
	private String interfacePassword;
	@ApiModelProperty(value = "发送方名称")
	private String sendName;
	@ApiModelProperty(value = "短信模板")
	private String template;
	@ApiModelProperty(value = "最后一次修改人的账号")
	private String modifyUser;
	@ApiModelProperty(value = "最后一次修改时间")
	private String modifyTime;
	@ApiModelProperty(value="区号支持，*86|*886 支持台湾和大陆；*86支持大陆")
	private String mobileAreaCode;

	@Transient
	@ApiModelProperty(value = "短信测试号码")
	private String  mobile;
}