package com.wsdy.saasops.modules.member.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@ApiModel(value = "MbrMemo", description = "")
@Table(name = "mbr_memo")
public class MbrMemo implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员Id")
    private Integer accountId;

    @ApiModelProperty(value = "备注时间")
    private String memoTime;

    @ApiModelProperty(value = "操作人")
    private String oprUserName;

    @ApiModelProperty(value = "备注名称")
    private String markName;

    @ApiModelProperty(value = "备注内容")
    private String memo;

    @ApiModelProperty(value = "角色ID，冗余字段")
    private Integer roleId;

    @Transient
    private String roleName;

    @Transient
    @ApiModelProperty(value = "ids CRUD字段")
    private List<Integer> ids;
}