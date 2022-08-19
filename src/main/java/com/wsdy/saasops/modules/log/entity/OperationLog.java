package com.wsdy.saasops.modules.log.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;



@Data
@ApiModel(value = "OperationLog", description = "")
@Table(name = "operation_log")
public class OperationLog implements Serializable{
private static final long serialVersionUID=1L;
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@ApiModelProperty(value = "id")
private Integer id;

@ApiModelProperty(value = "操作用户")
@Column(name="ops_user")
private String opsUser;

@ApiModelProperty(value = "操作时间")
@Column(name="ops_time")
private String opsTime;

@ApiModelProperty(value = "操作类")
@Column(name="ops_class")
private String opsClass;

@ApiModelProperty(value = "操作方法")
@Column(name="ops_method")
private String opsMethod;

@ApiModelProperty(value = "方法参数")
@Column(name="ops_parameters")
private String opsParameters;

@ApiModelProperty(value = "操作状态")
@Column(name="ops_status")
private String opsStatus;

@ApiModelProperty(value = "操作IP")
@Column(name="ops_ip")
private String opsIp;

@ApiModelProperty(value = "操作设备")
@Column(name="tml_device)")
private String tmlDevice;

@ApiModelProperty(value = "异常信息")
@Column(name="exception_info)")
private String exceptionInfo;

}