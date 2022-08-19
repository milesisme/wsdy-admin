package com.wsdy.saasops.api.modules.pay.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CFBpayReponseDto {

    private String message;
    private Integer status;
    private Long time;
    private String orderId;
    private String qrCode;
    private String sign;
    private String matchcode;
    private CFBpayQueryReponse content;
}

