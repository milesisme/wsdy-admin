package com.wsdy.saasops.modules.agent.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


@Setter
@Getter
@Table(name = "agy_domain")
public class AgyDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "自增长Id")
    private Integer id;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "代理Id")
    private Integer accountId;

    @ApiModelProperty(value = "1开启，0禁用")
    private Integer available;
    
    @Transient
    @ApiModelProperty(value = "1开启，0禁用")
    private String availableStr;

    @ApiModelProperty(value = "0拒绝，1成功 2待处理")
    private Integer status;

    @ApiModelProperty(value = "域名")
    private String domainUrl;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "修改人")
    private String modifyUser;

    @ApiModelProperty(value = "修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "是否可以删除 0否 1是")
    private Integer isDel;
    
    @ApiModelProperty(value = "到期时间")
    private String expireDate;
    
    @Transient
    @ApiModelProperty(value = "到期开始时间")
    private String expireDateStart;
    
    @Transient
    @ApiModelProperty(value = "到期结束时间")
    private String expireDateEnd;
    
    @Transient
    @ApiModelProperty(value = "是否到期")
    private String expireStatusStr;

    @Transient
    @ApiModelProperty(value = "代理推广代码必须唯一")
    private String spreadCode;

    @Transient
    @ApiModelProperty(value = "注册人数")
    private Integer accountNum;

    @Transient
    private String createTimeFrom;

    @Transient
    private String createTimeTo;

    @Transient
    private List<String> domainUrlList;

    @Transient
    private List<Integer> ids;

    @Transient
    @ApiModelProperty(value = "0 拒绝，1 成功 2 待处理")
    private String statusStr;
}