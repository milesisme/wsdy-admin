package com.wsdy.saasops.api.modules.pay.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CFBpayQueryReponse{
    private String orderId;
    private Integer amount;
    private Integer payStatus;
    private Integer payType;
    private Integer payMethod;
    private String payTime;
    private String createTime;
    private String mcnBackUrl;
    private Integer mcnNoticeStatus;
    private Integer mcnNoticeNum;
    private Double mcnRate;
    private String sign;
}
