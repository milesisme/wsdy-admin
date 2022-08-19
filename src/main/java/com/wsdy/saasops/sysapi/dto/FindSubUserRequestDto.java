package com.wsdy.saasops.sysapi.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FindSubUserRequestDto {


    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 父级用户
     */
    private String userName;

    /**
     * 子用户
     */
    private String subUserName;


    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 页大小
     */
    private Integer pageSize;

    /**
     * z
     */
    private String siteCode;
}
