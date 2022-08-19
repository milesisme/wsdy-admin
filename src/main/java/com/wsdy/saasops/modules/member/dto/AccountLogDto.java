package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "会员操作内容")
public class AccountLogDto {

    @ApiModelProperty(value = "变更项目")
    private String item;

    @ApiModelProperty(value = "变更前")
    private String beforeChange;

    @ApiModelProperty(value = "变更后")
    private String afterChange;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "操作人")
    private String operatorUser;

    @ApiModelProperty(value = "操作人类型 1本人 2后台操作")
    private int operatorType;
}
