package com.wsdy.saasops.modules.fund.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnePayReqBaseDto {

    private String mer_id;

    private String mer_ordersid;

    private String time_stamp;

    private String signature;
}
