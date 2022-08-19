package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@ApiModel(value = "登陆次数限制", description = "登陆次数限制")
public class LoginVerifyDto {
    @ApiModelProperty(value = "当前密码错误次数")
    private Integer no;
    @ApiModelProperty(value = "密码错误限制失效时间")
    private Date expireTime;
}
