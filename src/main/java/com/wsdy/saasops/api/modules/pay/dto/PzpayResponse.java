package com.wsdy.saasops.api.modules.pay.dto;

import lombok.Data;


@Data
public class PzpayResponse {

    private Boolean success;

    private String message;

    private Object content;
}
