package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "OnePayReqEncryptDto", description = "遨游支付代付返回结果")
public class OnePayResponseDto {

    private String mer_id;
    private String status;
    private String msg;
    private String ordersid;
    private String money;

    private String card_num;

    private String card_name;

    private String bank_code;

    private String order_status;

    private String time_stamp;

    private String signature;

    private String mer_ordersid;

}
