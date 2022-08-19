package com.wsdy.saasops.modules.member.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "MbrAccountTime", description = "会员在线时长")
@Table(name = "mbr_account_time")
public class MbrAccountTime implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "登入时间")
    private String login;

    @ApiModelProperty(value = "登出时间")
    private String logout;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "会员登陆名称")
    private String loginName;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "本次时长单位秒")
    private Long duration;
}