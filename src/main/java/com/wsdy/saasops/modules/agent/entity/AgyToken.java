package com.wsdy.saasops.modules.agent.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Table(name = "agy_token")
@ApiModel(value = "AgyToken", description = "AgyToken")
public class AgyToken {


    @ApiModelProperty(value = "代理ID")
    private Integer accountId;

    @ApiModelProperty(value = "token")
    private String token;

    @ApiModelProperty(value = "过期时间")
    private String expireTime;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

}