package com.wsdy.saasops.api.modules.pay.dto.saaspay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonPayResponse<T> {

    private Integer code;
    private String msg;
    private T data;

}
