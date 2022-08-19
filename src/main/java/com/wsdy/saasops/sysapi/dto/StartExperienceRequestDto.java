package com.wsdy.saasops.sysapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Setter
@Getter
public class StartExperienceRequestDto {
    /**
     * 站点CODE
     */
    private String siteCode;

    /**
     * 上级用户
     */
    private String userName;


    /**
     * 子用户
     */
    private List<String> subUserNames;
}
