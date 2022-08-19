package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDto{
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "")
    private String item;
    @ApiModelProperty(value = "代理")
    private String agyAccount;
    @ApiModelProperty(value = "创建时间")
    private String createtime;
}
