package com.wsdy.saasops.modules.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskActiveInfoDto {

  @ApiModelProperty(value = "已填写真实姓名 true是 false 否")
  private Boolean isName;

  @ApiModelProperty(value = "已绑定银行卡 true是 false 否")
  private Boolean isBank;

  @ApiModelProperty(value = "已验证手机 true是 false 否")
  private Boolean isMobile;

  @ApiModelProperty(value = "已验证邮箱 true是 false 否")
  private Boolean isMail;

}