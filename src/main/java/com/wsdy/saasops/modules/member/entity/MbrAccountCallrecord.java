package com.wsdy.saasops.modules.member.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "MbrAccountCallrecord", description = "")
@Table(name = "mbr_account_callrecord")
@ToString
public class MbrAccountCallrecord implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "用户id")
    private Long userId;
    @ApiModelProperty(value = "用户姓名")
    private String userName;
    @ApiModelProperty(value = "会员id")
    private Integer accountId;
    @ApiModelProperty(value = "会员姓名")
    private String loginName;
    @ApiModelProperty(value = "回调函数业务id")
    private String refId;

    @ApiModelProperty(value = "通话开始时间，yyyy-MM-dd HH:mm:ss")
    private String beginTime;
}