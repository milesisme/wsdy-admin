package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "会员找回密码方式,0 邮箱,1短信", description = "会员找回密码方式,0 邮箱,1短信")
public class RetrvWayDto {
/*@ApiParam("会员找回密码方式,0 邮箱,1短信")
 private String validType;*/
}
