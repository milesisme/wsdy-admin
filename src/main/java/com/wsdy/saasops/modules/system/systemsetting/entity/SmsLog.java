package com.wsdy.saasops.modules.system.systemsetting.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@Table(name = "sms_log")
public class SmsLog implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Long id;
	@ApiModelProperty(value = "0 单次告警记录  1短信平台错误")
	private Integer type;
	@ApiModelProperty(value = "0 未处理  1已处理")
	private Integer status;
	@ApiModelProperty(value = "平台ID 1梦网云  2Telesign  3启瑞云  4互亿无线")
	private Integer platformId;
	@ApiModelProperty(value = "平台名称")
	private String platformName;
	@ApiModelProperty(value = "平台错误信息")
	private String msg;
	@ApiModelProperty(value = "记录时间")
	private String createTime;
	@ApiModelProperty(value = "处理时间")
	private String dealTime;
	@ApiModelProperty(value = "短信内容")
	private String  content;
	@ApiModelProperty(value = "接收手机号")
	private String  mobile;
	@ApiModelProperty(value = "是否成功发送,原有数据默认false")
	private Boolean  isSuccess;
	@ApiModelProperty(value = "默认-1: 原有的错误记录数据，0:其他模块，1:注册 2：登录 3：找回密码 4：绑定银行卡 5：绑定手机号 6:好友转账")
	private Integer  module;
	


	@Transient
	@ApiModelProperty(value = "ids")
	private List<Long> ids;
	
	@Transient
    @ApiModelProperty(value = "开始时间")
    private String startTime;

	@Transient
    @ApiModelProperty(value = "开始时间")
    private String endTime;
	
	@Transient
    @ApiModelProperty(value = "pageNo")
    private Integer pageNo = 1;
    
    @Transient
    @ApiModelProperty(value = "pageSize")
    private Integer pageSize = 10;
}