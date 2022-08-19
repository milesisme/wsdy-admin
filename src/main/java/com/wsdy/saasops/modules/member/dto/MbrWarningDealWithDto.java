package com.wsdy.saasops.modules.member.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MbrWarningDealWithDto {

    /**
     * ID
     */
    private Long id;

    /**
     * 备注
     */
    private String memo;
}
