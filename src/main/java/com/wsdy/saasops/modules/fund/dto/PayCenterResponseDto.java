package com.wsdy.saasops.modules.fund.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PayCenterResponseDto {

    /**
     * 错误代码
     */
    private String code;

    /**
     * 消息
     */
    private String msg;

    /**
     * 数据
     */
    private String data;

}
