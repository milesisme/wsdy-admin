package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaiduResDto {
 private long log_id; // 请求标识码，随机数，唯一
 private Object result;  // 识别结果result对象
}
