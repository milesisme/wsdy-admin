package com.wsdy.saasops.modules.sys.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SysWarningDealWithDto {

    /**
     * ID
     */
    private Long id;

    /**
     * 备注
     */
    private String memo;
}
