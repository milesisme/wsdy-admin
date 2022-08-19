package com.wsdy.saasops.api.modules.apisys.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;



@Setter
@Getter
@ApiModel(value = "TCpCompany", description = "")
public class TCpCompany implements Serializable{
private static final long serialVersionUID=1L;
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@ApiModelProperty(value = "id")
private Integer id;

@ApiModelProperty(value = "")
private String companyCode;

@ApiModelProperty(value = "商户简称")
private String companySname;

@ApiModelProperty(value = "商户全称")
private String companyFname;

@ApiModelProperty(value = "开始时间")
private String startDate;

@ApiModelProperty(value = "结束时间")
private String endDate;

@ApiModelProperty(value = "地址")
private String address;

@ApiModelProperty(value = "联系人")
private String contact;

@ApiModelProperty(value = "邮箱地址")
private String email;

@ApiModelProperty(value = "国家代码")
private String countryCode;

@ApiModelProperty(value = "电话号码")
private String phoneNumber;

@ApiModelProperty(value = "商户LOGO url")
private String cpLogo;

@ApiModelProperty(value = "上级组织")
private String organize;

@ApiModelProperty(value = "状态　1开启，0禁用")
private Byte available;
}