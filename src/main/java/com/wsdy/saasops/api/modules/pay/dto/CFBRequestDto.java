package com.wsdy.saasops.api.modules.pay.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CFBRequestDto {
    private String payTime;
    private String orderId;
    private Integer payStatus;
    private Integer amount;
    private String sign;

}
