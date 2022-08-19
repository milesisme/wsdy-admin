package com.wsdy.saasops.modules.fund.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommonDPayResponseDto<T> {

    private Integer code;
    private String msg;
    private T data;

}
