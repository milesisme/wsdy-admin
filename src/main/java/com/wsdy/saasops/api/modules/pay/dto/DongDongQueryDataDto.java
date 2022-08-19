package com.wsdy.saasops.api.modules.pay.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DongDongQueryDataDto {
    private String money;
    private String moneyReceived;
    private Integer status;
    private Integer callBackStatus;
    private String updateTime;
}
