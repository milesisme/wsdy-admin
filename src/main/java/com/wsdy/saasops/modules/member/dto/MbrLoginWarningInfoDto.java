package com.wsdy.saasops.modules.member.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MbrLoginWarningInfoDto {

    /**
     * 会员名称
     */
    private String loginName;

    /**
     * IP
     */
    private String exContent;
}
