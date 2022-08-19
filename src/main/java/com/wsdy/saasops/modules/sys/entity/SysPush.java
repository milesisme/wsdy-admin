package com.wsdy.saasops.modules.sys.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter
@ApiModel(value = "sys_push", description = "sys_push")
@Table(name = "sys_push")
public class SysPush {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "secret")
    private String secret;

    @ApiModelProperty(value = "pushKey")
    private String pushKey;

    @ApiModelProperty(value = "type 1极光推送")
    private Integer type;

    @ApiModelProperty(value = "创建时间")
    private  String  createTime;

    @ApiModelProperty(value = "创建者")
    private  String  creator;

    @ApiModelProperty(value = "更新时间")
    private  String  updateTime;

    @ApiModelProperty(value = "更新者")
    private String updater;

}
