package com.wsdy.saasops.sysapi.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApplySubUserNameDto {

    /**
     * 子用户
     */
    private String subUserName;


    /**
     * 申请状态 0成功
     */
    private Integer status;


}
