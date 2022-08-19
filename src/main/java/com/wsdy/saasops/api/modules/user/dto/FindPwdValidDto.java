package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "会员找回密码-提交选择找回方式", description = "会员找回密码-提交选择找回方式")
public class FindPwdValidDto {
  @ApiModelProperty(value = "验证方式,0邮件 1手机!")
  Integer ValidateType;
  @ApiModelProperty(value = "会员账号!")
  String userName;
}
