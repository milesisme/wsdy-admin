package com.wsdy.saasops.sysapi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "登陆信息", description = "登陆信息")
public class LoginDto {
    @ApiModelProperty(value = "会员名")
    private String userName;
    @ApiModelProperty(value = "密码")
    private String password;
}
