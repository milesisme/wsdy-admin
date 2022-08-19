package com.wsdy.saasops.api.modules.pay.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DongDongCallbackDto {

    private String MerchantCode;
    private String OrderId;
    private String OrderDate;
    private String Amount;
    private String OutTradeNo;
    private String BankCode;
    private String Time;
    private String Remark;
    private String Status;
    private String Sign;

}
