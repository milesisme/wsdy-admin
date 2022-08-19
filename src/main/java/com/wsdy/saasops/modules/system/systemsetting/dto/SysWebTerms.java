package com.wsdy.saasops.modules.system.systemsetting.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;



@Setter
@Getter
@ApiModel(value = "会员注册协议", description = "会员注册协议")
public class SysWebTerms implements Serializable{
private static final long serialVersionUID=1L;

//是否强制显示网站服务条款，1是，0否
@ApiModelProperty(value="是否强制显示网站服务条款，1是，0否")
private String display;

//网站服务条款
@ApiModelProperty(value="网站服务条款内容")
private String serviceTerms;

}