package com.wsdy.saasops.modules.operate.entity;

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
@ApiModel(value = "OprMsgrecipient", description = "")
@Table(name = "opr_msgrecipient")
public class OprMsgrecipient implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "消息Id")
	private Integer msgId;

	@ApiModelProperty(value = "会员Id")
	private Integer memberId;

	@ApiModelProperty(value = "阅读时间")
	private String readTime;
	
	@ApiModelProperty(value = "0未读，1已读")
	private Byte state;
	
	@ApiModelProperty(value = "收件人登陆名")
	private String loginName;
}