package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgResDto {
@ApiModelProperty(value="0 成功，1失败(处理中),2因无效的转账金额引致的失败 等其它")
 private String info;//代码数值
 private String msg;//代码数值描述
}
