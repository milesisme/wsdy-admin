package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedisKey {
//站点前缀
 private String siteCode;
 //登陆名
 private String loginName;
}
