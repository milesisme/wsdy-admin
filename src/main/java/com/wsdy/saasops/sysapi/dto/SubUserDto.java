package com.wsdy.saasops.sysapi.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SubUserDto {
    /**
     * 子用户
     */
    private String subUserName;


    /**
     * 注册时间
     */
    private String registerTime;
}
